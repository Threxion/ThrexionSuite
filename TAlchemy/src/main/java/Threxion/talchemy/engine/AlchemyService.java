package Threxion.talchemy.engine;

import Threxion.talchemy.definition.AlchemyDefinition;
import Threxion.talchemy.definition.ProcessDefinition;
import Threxion.talchemy.definition.CatalystDefinition;
import Threxion.talchemy.definition.StationDefinition;
import Threxion.talchemy.definition.AlchemyEffectDefinition;
import Threxion.talchemy.definition.ProcessTierDefinition;
import Threxion.threxioncore.CoreRegistries;
import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.data.Definition;
import Threxion.threxioncore.api.modifier.Modifier;
import Threxion.threxioncore.api.modifier.ModifierContext;
import Threxion.threxioncore.api.modifier.ModifierPipeline;
import Threxion.threxioncore.api.property.ItemStackPropertyContainer;
import Threxion.threxioncore.api.finalize.PropertyFinalization;
import Threxion.threxioncore.api.property.PropertyValue;
import Threxion.threxioncore.api.transfer.Transfer;
import Threxion.threxioncore.api.event.ItemProductionEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import Threxion.talchemy.api.IAlchemyStation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TAlchemy 的领域执行器。
 * Process/Recipe/Modifier 全部来自 JSON；Core 只提供 Definition、Property、Modifier、Transfer。
 */
public final class AlchemyService {
    private static final Map<Long, AlchemyJob> JOBS = new ConcurrentHashMap<>();

    private AlchemyService() {}

    public static boolean start(ServerPlayer player, IAlchemyStation station) {
        if (player == null || station == null || !station.isUsable()) return false;
        if (JOBS.containsKey(station.pos().asLong())) return false;
        ItemStack input = station.getInput();
        if (input.isEmpty()) return false;
        for (AlchemyDefinition recipe : AlchemyDefinitionIndex.recipesForInput(input)) {
            if (!matches(recipe, input, station, station.getCatalyst(), station.getContainerInput())) continue;
            ProcessDefinition process = getProcess(recipe.process());
            if (process == null) continue;
            StationDefinition stationDef = getStationForId(station.stationId());
            if (stationDef == null || !recipe.process().equals(stationDef.process())) continue;
            ProcessTierDefinition tier = (recipe.medicine() && stationDef.tierEnabled()) ? getTier(stationDef.tier()) : null;
            if (tier != null && !tier.recipeAllowed()) continue;
            Item item = ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(recipe.output()));
            if (item == null) continue;
            int batchCount = (recipe.medicine() && tier != null) ? tier.batchCount() : 1;
            int outputCount = Math.min(Math.max(1, recipe.outputCount()) * batchCount, item.getMaxStackSize());
            ItemStack output = new ItemStack(item, outputCount);
            boolean inputWasExpired = AlchemyExpirationService.inspect(input, player.level()).expired();
            applyTransfers(recipe,new ItemStackPropertyContainer(input),new ItemStackPropertyContainer(output));
            if (recipe.medicine() && recipe.qualityEffectsEnabled()) {
                applyMedicineQualityStructure(recipe, new ItemStackPropertyContainer(input), output, item.getMaxStackSize());
            }
            JOBS.put(station.pos().asLong(),new AlchemyJob(player,station,recipe,process,station.getCatalyst(),output,inputWasExpired)); return true;
        } return false;
    }

    /**
     * 计算最终加工时间：Recipe 基准时间（若指定）→ Station 固定时间（若提供）→ Process 时间，
     * 然后同时乘以 Recipe 与 Station 的时间倍率。
     */
    private static int processingTimeTicks(AlchemyJob job) {
        int base = job.recipe().processingTimeTicks();
        if (base < 0) base = job.station().processingTimeTicks();
        if (base < 0) base = job.process().totalDuration();
        StationDefinition sd = getStationForId(job.stationId());
        double stationMultiplier = sd == null ? 1.0 : sd.timeMultiplier();
        double externalMultiplier = job.station().processingTimeMultiplier();
        double result = base * job.recipe().timeMultiplier() * stationMultiplier * externalMultiplier;
        return Math.max(1, (int)Math.ceil(result));
    }

    private static double timeMultiplier(AlchemyJob job) {
        StationDefinition sd = getStationForId(job.stationId());
        double stationMultiplier = sd == null ? 1.0 : sd.timeMultiplier();
        return job.recipe().timeMultiplier() * stationMultiplier * job.station().processingTimeMultiplier();
    }

    private static int stageDurationTicks(AlchemyJob job, ProcessDefinition.Stage stage) {
        return Math.max(1, (int)Math.ceil(stage.durationTicks() * timeMultiplier(job)));
    }

    private static boolean hasExplicitTotalTime(AlchemyJob job) {
        return job.recipe().processingTimeTicks() >= 0 || job.station().processingTimeTicks() >= 0;
    }

    /** 在服务器 Tick 中推进所有炼金作业。 */
    public static void tickAll() {
        Iterator<Map.Entry<Long, AlchemyJob>> it = JOBS.entrySet().iterator();

        while (it.hasNext()) {
            AlchemyJob job = it.next().getValue();
            ServerPlayer player = job.player();

            if (!player.isAlive() || player.distanceToSqr(
                    job.station().pos().getX() + 0.5,
                    job.station().pos().getY() + 0.5,
                    job.station().pos().getZ() + 0.5) > 64.0) {
                it.remove();
                continue;
            }

            if (!job.station().isUsable()) { it.remove(); continue; }
            ItemStack live = job.station().getInput();
            Item expected = ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(job.recipe().input()));
            if (live.isEmpty() || expected == null || !live.is(expected) || live.getCount() < job.recipe().inputCount()) { it.remove(); continue; }

            job.tick();

            int totalTime = processingTimeTicks(job);
            // Recipe/Station 显式指定总时长时，像熔炉一样等待整个进度条完成。
            if (hasExplicitTotalTime(job)) {
                if (job.elapsedTicks() < totalTime) continue;
                for (ProcessDefinition.Stage stageDef : job.process().stages()) {
                    resolveStage(job, stageDef);
                    if (job.failed()) break;
                }
                finish(job);
                it.remove();
                continue;
            }

            // 没有显式总时长时，Process 的各阶段按 Recipe/Station 的倍率逐阶段推进。
            if (job.process().stages().isEmpty()) {
                if (job.elapsedTicks() < totalTime) continue;
                finish(job);
                it.remove();
                continue;
            }

            ProcessDefinition.Stage stage = currentStage(job);
            if (stage == null) {
                finish(job);
                it.remove();
                continue;
            }

            int stageStart = completedDuration(job, job.process(), job.stageIndex());
            int stageElapsed = job.elapsedTicks() - stageStart;

            if (stageElapsed < stageDurationTicks(job, stage)) continue;

            resolveStage(job, stage);

            if (job.failed()) {
                finish(job);
                it.remove();
                continue;
            }

            if (job.stageIndex() + 1 >= job.process().stages().size()) {
                finish(job);
                it.remove();
            } else {
                job.stageIndex(job.stageIndex() + 1);
            }
        }
    }

    private static ProcessDefinition.Stage currentStage(AlchemyJob job) {
        if (job.process().stages().isEmpty()) {
            if (job.elapsedTicks() >= job.process().time()) return null;
            return new ProcessDefinition.Stage("default", "处理", job.process().time(), 0.0, List.of());
        }
        return job.stageIndex() < job.process().stages().size()
                ? job.process().stages().get(job.stageIndex()) : null;
    }

    private static int completedDuration(AlchemyJob job, ProcessDefinition p, int completedStages) {
        int total = 0;
        for (int i = 0; i < completedStages && i < p.stages().size(); i++)
            total += stageDurationTicks(job, p.stages().get(i));
        return total;
    }

    private static void resolveStage(AlchemyJob job, ProcessDefinition.Stage stage) {
        if (job.failed()) return;

        if (Math.random() < effectiveFailureChance(job, stage.failureChance())) {
            job.markFailed();
            return;
        }

        applyStageModifiers(job.outputProperties(), stage.modifiers(),
                new Random(), "talchemy:stage/" + stage.id());
    }

    private static void finish(AlchemyJob job) {
        ServerPlayer player = job.player();
        ItemStack input = job.station().getInput();
        Item expected = ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(job.recipe().input()));
        if (input.isEmpty() || expected == null || !input.is(expected) || input.getCount() < job.recipe().inputCount()) return;
        input.shrink(job.recipe().inputCount());
        job.station().setInput(input);

        if (!job.failed() && Math.random() < effectiveFailureChance(job, job.process().failure().chance())) {
            job.markFailed();
        }

        // 流程失败：材料已经消耗；如果流程定义提供 failure.output，则生成副产物/失败产物。
        if (job.failed()) {
            ProcessDefinition.Failure failure = job.process().failure();
            if (!failure.output().isBlank()) {
                Item failureItem = ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(failure.output()));
                if (failureItem != null) {
                    ItemStack failureStack = new ItemStack(failureItem, failure.outputCount());
                    MinecraftForge.EVENT_BUS.post(new ItemProductionEvent(
                            failureStack, "talchemy:failure/" + job.process().getId().getPath(), player));
                    ItemStack fo=job.station().getOutput(); if(fo.isEmpty()) job.station().setOutput(failureStack); else if(!player.getInventory().add(failureStack)) player.drop(failureStack,false);
                }
            }
            return;
        }

        // Final Property modifier order: Recipe → Catalyst → Station(Tier).
        // Quality is already materialized as an ordinary Property before this calculation.
        applyRecipeModifiers(job.outputProperties(), job.recipe().modifiers(), new Random());
        CatalystDefinition catalyst = getCatalyst(job.recipe().catalyst());
        if (catalyst != null) {
            applyCatalystModifiers(job.outputProperties(), catalyst.modifiers(), new Random());
        }
        ProcessTierDefinition tier = stationTier(job);
        if (tier != null) applyTierModifiers(job.outputProperties(), tier.modifiers(), new Random());
        applyEffects(job.workingOutput(), job.recipe().effects(), new Random());
        // 将工艺等级作为持久化属性写入成品，供 /scan、TQuality 及其他整合 Mod 读取。
        if (job.recipe().medicine()) {
            StationDefinition stationDefinition = getStationForId(job.stationId());
            if (stationDefinition != null && stationDefinition.tierEnabled()) {
                job.outputProperties().set(PropertyId.parse("talchemy:process_tier"),
                        PropertyValue.ofString(stationDefinition.tier()));
            }
        }

        // Finalization cleanup happens only after TAlchemy has completed all of its
        // calculations. This lets process stages read source properties when needed.
        PropertyFinalization.consume(job.outputProperties(), job.recipe().removeProperties());

        if (!job.recipe().containerInput().isBlank()) {
            ItemStack container = job.station().getContainerInput();
            Item expectedContainer = ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(job.recipe().containerInput()));
            if (expectedContainer != null && !container.isEmpty() && container.is(expectedContainer)
                    && container.getCount() >= job.recipe().containerInputCount()) {
                container.shrink(job.recipe().containerInputCount());
                job.station().setContainerInput(container);
            }
        }

        CatalystDefinition catalystDefinition = getCatalyst(job.recipe().catalyst());
        boolean consumeCatalyst = catalystDefinition != null
                ? catalystDefinition.consumed() : job.recipe().catalystConsumed();
        if (consumeCatalyst && !job.catalyst().isEmpty()) { ItemStack cat=job.station().getCatalyst(); if(!cat.isEmpty() && cat.is(job.catalyst().getItem())){cat.shrink(1);job.station().setCatalyst(cat);} }
        ItemStack out=job.workingOutput(); writeAlchemyMetadata(out,input,job);
        MinecraftForge.EVENT_BUS.post(new ItemProductionEvent(out,"talchemy:"+job.recipe().getId().getPath(),player));
        insertOrDrop(job, out, false);

        if (!job.recipe().byproduct().isBlank() && job.recipe().byproductCount() > 0) {
            Item byproductItem = ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(job.recipe().byproduct()));
            if (byproductItem != null) {
                ItemStack byproduct = new ItemStack(byproductItem, Math.min(job.recipe().byproductCount(), byproductItem.getMaxStackSize()));
                if (!job.recipe().byproductPotion().isBlank() && byproductItem == ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft", "potion"))) {
                    byproduct.getOrCreateTag().putString("Potion", job.recipe().byproductPotion());
                }
                MinecraftForge.EVENT_BUS.post(new ItemProductionEvent(byproduct,
                        "talchemy:byproduct/" + job.recipe().getId().getPath(), player));
                insertOrDrop(job, byproduct, true);
            }
        }
    }

    private static void insertOrDrop(AlchemyJob job, ItemStack stack, boolean byproduct) {
        ServerPlayer player = job.player();
        ItemStack existing = byproduct ? job.station().getByproduct() : job.station().getOutput();
        if (existing.isEmpty()) {
            if (byproduct) job.station().setByproduct(stack); else job.station().setOutput(stack);
        } else if (ItemStack.isSameItemSameTags(existing, stack)
                && existing.getCount() + stack.getCount() <= existing.getMaxStackSize()) {
            existing.grow(stack.getCount());
            if (byproduct) job.station().setByproduct(existing); else job.station().setOutput(existing);
        } else if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    private static final String ALCHEMY_META = AlchemyExpirationService.META;
    private static final String CREATION_DAY = AlchemyExpirationService.CREATION_DAY;
    private static final String EXPIRATION_DAYS = AlchemyExpirationService.EXPIRATION_DAYS;
    private static final String EXPIRED_BEHAVIOR = AlchemyExpirationService.EXPIRED_BEHAVIOR;
    private static final String EXPIRED_PROPERTY = AlchemyExpirationService.EXPIRED_PROPERTY;
    private static final String EXPIRED_AMOUNT = AlchemyExpirationService.EXPIRED_AMOUNT;
    private static final String SHOW_EXPIRATION = "ShowExpirationInLore";
    private static final String PROCESS_HISTORY = "ProcessHistory";

    /** 写入制作日、保质期策略和加工流程。检测过期不会由 Tick 驱动，而只在引用时执行。 */
    private static void writeAlchemyMetadata(ItemStack output, ItemStack input, AlchemyJob job) {
        CompoundTag tag = output.getOrCreateTag();
        CompoundTag meta = tag.getCompound(ALCHEMY_META);
        // A new recipe product gets a new expiration policy. Do not inherit the
        // input item's spoilage date, even when the input has ThrexionAlchemy metadata.
        // Process history is the one metadata channel intentionally inherited below.
        long day = job.player().serverLevel().getDayTime() / 24000L;
        meta.remove(CREATION_DAY);
        meta.remove(EXPIRATION_DAYS);
        meta.remove(EXPIRED_BEHAVIOR);
        meta.remove(AlchemyExpirationService.EXPIRED_HANDLED);
        meta.remove(EXPIRED_PROPERTY);
        meta.remove(EXPIRED_AMOUNT);
        meta.remove(SHOW_EXPIRATION);
        int expirationDays = job.recipe().expirationDays();
        if (job.inputWasExpired() && job.player().serverLevel().getGameRules()
                .getBoolean(TAlchemyMod.EXPIRED_INPUT_IMMEDIATE_EXPIRATION)) {
            // Gamerule 开启时：使用已过期输入参与制作，成品立即过期。
            expirationDays = 0;
        }
        if (expirationDays >= -1) {
            meta.putLong(CREATION_DAY, day);
            meta.putInt(EXPIRATION_DAYS, expirationDays);
            meta.putString(EXPIRED_BEHAVIOR, job.recipe().expiredBehavior());
            if (job.recipe().expiredPotencyProperty() != null)
                meta.putString(EXPIRED_PROPERTY, job.recipe().expiredPotencyProperty().toString());
            meta.putDouble(EXPIRED_AMOUNT, job.recipe().expiredPotencyAmount());
            meta.putBoolean(SHOW_EXPIRATION, job.recipe().showExpirationInLore());
        }

        ListTag history = meta.contains(PROCESS_HISTORY, 9) ? meta.getList(PROCESS_HISTORY, 8) : new ListTag();
        String stationName = resolveStationDisplayName(job.stationId());
        if (job.process().stages().isEmpty()) {
            history.add(StringTag.valueOf(stationName + " " + job.process().displayName()));
        } else {
            for (ProcessDefinition.Stage stage : job.process().stages()) {
                String stageName = stage.displayName().isBlank() ? job.process().displayName() : stage.displayName();
                history.add(StringTag.valueOf(stationName + " " + stageName));
            }
        }
        meta.put(PROCESS_HISTORY, history);
        tag.put(ALCHEMY_META, meta);
        output.setTag(tag);
        writeProcessLore(output, meta);
    }

    private static String resolveStationDisplayName(String blockId) {
        if (blockId == null || blockId.isBlank()) return "未知工作方块";
        StationDefinition station = getStationForBlock(blockId);
        if (station != null && !station.displayName().isBlank()) return station.displayName();
        return blockId;
    }

    /** Lore 是展示层；真正的数据仍保存在 ThrexionAlchemy NBT 中。 */
    private static void writeProcessLore(ItemStack stack, CompoundTag meta) {
        if (!meta.contains(PROCESS_HISTORY, 9)) return;
        ListTag lore = new ListTag();
        ListTag history = meta.getList(PROCESS_HISTORY, 8);
        for (int i = 0; i < history.size(); i++) {
            lore.add(StringTag.valueOf(Component.Serializer.toJson(Component.literal("加工：" + history.getString(i)))));
        }
        int expirationDays = meta.getInt(EXPIRATION_DAYS);
        if (meta.getBoolean(SHOW_EXPIRATION) && expirationDays >= 0) {
            long creation = meta.getLong(CREATION_DAY);
            lore.add(StringTag.valueOf(Component.Serializer.toJson(
                    Component.literal("保质期：第 " + (creation + expirationDays) + " 日"))));
        }
        CompoundTag display = stack.getOrCreateTagElement("display");
        display.put("Lore", lore);
    }

    /** 公开给 Scan/其他 T Mod：检查是否过期，但不会 Tick。 */
    public static ExpirationStatus inspectExpiration(ItemStack stack, net.minecraft.world.level.Level level) {
        return AlchemyExpirationService.inspect(stack, level);
    }

    /** 当前 Station 的加工进度（tick）。用于附属工作方块 GUI 的进度动画。 */
    public static int getProgress(BlockPos pos) {
        AlchemyJob job = JOBS.get(pos.asLong());
        return job == null ? 0 : job.elapsedTicks();
    }

    /** 当前 Station 的总加工时间（tick）。无作业时返回 0。 */
    public static int getProgressTotal(BlockPos pos) {
        AlchemyJob job = JOBS.get(pos.asLong());
        if (job == null) return 0;
        return processingTimeTicks(job);
    }

    public static boolean isProcessing(BlockPos pos) {
        return JOBS.containsKey(pos.asLong());
    }

    /**
     * Recipe 级属性传递策略：
     * all = 传递所有 persistent 属性；
     * specific = 只传递 transfer_properties；
     * none = 不传递。
     *
     * remove_properties 在 TAlchemy 最终计算完成后执行，因此可以表达“成药/精炼后剔除源属性”。
     */
    /**
     * Medicine-only quality layer. Ordinary recipes never enter this path, so their
     * tquality:grade property follows the normal Transfer rules unchanged.
     */
    private static void applyMedicineQualityStructure(AlchemyDefinition recipe,
                                                      ItemStackPropertyContainer input,
                                                      ItemStack output,
                                                      int maxStackSize) {
        String grade = input.get(PropertyId.parse("tquality:grade"))
                .filter(PropertyValue::isString).map(PropertyValue::getString).orElse("");
        if (grade.isBlank()) return;
        AlchemyDefinition.QualityEffect rule = findQualityRule(recipe, grade);
        if (rule == null) return;

        int count = Math.max(1, Math.min(maxStackSize, output.getCount() + rule.outputCountDelta()));
        output.setCount(count);

        int index = recipe.qualityGradeOrder().indexOf(grade);
        if (index < 0) return;
        int targetIndex = Math.max(0, Math.min(recipe.qualityGradeOrder().size() - 1, index + rule.gradeDelta()));
        String targetGrade = recipe.qualityGradeOrder().get(targetIndex);
        output.set(PropertyId.parse("tquality:grade"), PropertyValue.ofString(targetGrade));
    }

    private static AlchemyDefinition.QualityEffect findQualityRule(AlchemyDefinition recipe, String grade) {
        for (var rule : recipe.qualityEffects()) if (rule.inputGrade().equals(grade)) return rule;
        return null;
    }

    private static void applyTransfers(AlchemyDefinition recipe,
                                       ItemStackPropertyContainer source,
                                       ItemStackPropertyContainer target) {
        switch (recipe.transferMode()) {
            case "none" -> {
                // intentionally empty
            }
            case "specific" -> {
                for (PropertyId id : recipe.transferProperties()) {
                    if (!Transfer.canTransfer(id)) continue;
                    source.get(id).ifPresent(v -> target.set(id, v));
                }
            }
            default -> Transfer.transferPersistentProperties(source, target);
        }
    }

    private static void applyStageModifiers(ItemStackPropertyContainer target,
                                             List<ProcessDefinition.StageModifier> mods,
                                             Random random, String prefix) {
        for (var m : mods) {
            PropertyValue current = target.get(m.property()).orElse(PropertyValue.ofNumber(0.0));
            if (!current.isNumeric()) continue;

            Modifier modifier = Modifier.builder(prefix + "/" + m.property().getString(),
                            m.property(), m.operation())
                    .amount(m.amount()).range(m.min(), m.max()).build();

            writeProperty(target, m.property(),
                    ModifierPipeline.apply(current, List.of(modifier),
                            new ModifierContext(target, random, null)));
        }
    }

    /** 执行 Recipe 引用的可复用效果定义。
     * property_modifiers 作用于 Core Property；potion_effects 仅在 PotionItem 上写入原版药水效果。
     */
    private static void applyEffects(ItemStack output, List<PropertyId> effectIds, Random random) {
        ItemStackPropertyContainer target = new ItemStackPropertyContainer(output);
        List<MobEffectInstance> potionEffects = new ArrayList<>();
        for (PropertyId id : effectIds) {
            Definition definition = CoreRegistries.DEFINITIONS.get(id).orElse(null);
            if (!(definition instanceof AlchemyEffectDefinition effect)) continue;

            for (var modifier : effect.propertyModifiers()) {
                PropertyValue current = target.get(modifier.property()).orElse(PropertyValue.ofNumber(0.0));
                if (!current.isNumeric()) continue;
                Modifier m = Modifier.builder("talchemy:effect/" + id.getString() + "/" + modifier.property().getString(),
                                modifier.property(), modifier.operation())
                        .amount(modifier.amount()).range(modifier.min(), modifier.max()).build();
                writeProperty(target, modifier.property(), ModifierPipeline.apply(current, List.of(m),
                        new ModifierContext(target, random, null)));
            }

            // 用一个布尔属性记录已施加的逻辑效果，方便 /scan 与其他 T 系列 Mod 读取。
            target.set(PropertyId.parse("talchemy:effect." + id.getPath()), PropertyValue.ofBoolean(true));

            for (var potion : effect.potionEffects()) {
                ResourceLocation rl = ResourceLocation.tryParse(potion.effect());
                if (rl == null) continue;
                MobEffect mobEffect = ForgeRegistries.MOB_EFFECTS.getValue(rl);
                if (mobEffect == null) continue;
                potionEffects.add(new MobEffectInstance(mobEffect, potion.durationTicks(), potion.amplifier(),
                        potion.ambient(), potion.showParticles(), potion.showIcon()));
            }
        }
        if (!potionEffects.isEmpty() && output.getItem() instanceof net.minecraft.world.item.PotionItem) {
            PotionUtils.setCustomEffects(output, potionEffects);
        }
    }

    private static void applyRecipeModifiers(ItemStackPropertyContainer target,
                                              List<AlchemyDefinition.PropertyModifier> mods,
                                              Random random) {
        for (var m : mods) {
            PropertyValue current = target.get(m.property()).orElse(PropertyValue.ofNumber(0.0));
            if (!current.isNumeric()) continue;

            Modifier modifier = Modifier.builder("talchemy:recipe/" + m.property().getString(),
                            m.property(), m.operation())
                    .amount(m.amount()).range(m.min(), m.max()).build();

            writeProperty(target, m.property(),
                    ModifierPipeline.apply(current, List.of(modifier),
                            new ModifierContext(target, random, null)));
        }
    }


    /**
     * 写入 TAlchemy 属性时尊重 Core PropertySchema 的 min/max。
     * 这使药力通道可以拥有独立的可配置上限，例如 heal.max=500。
     */
    private static void writeProperty(ItemStackPropertyContainer target, PropertyId id, PropertyValue value) {
        if (value == null || !value.isNumeric()) {
            target.set(id, value);
            return;
        }
        double v = value.asDouble();
        var schema = CoreRegistries.PROPERTIES.schema(id).orElse(null);
        if (schema != null) {
            v = Math.max(schema.getMin(), Math.min(schema.getMax(), v));
        }
        target.set(id, PropertyValue.ofNumber(v));
    }

    private static ProcessTierDefinition stationTier(AlchemyJob job) {
        if (!job.recipe().medicine()) return null;
        StationDefinition stationDefinition = getStationForId(job.stationId());
        if (stationDefinition == null || !stationDefinition.tierEnabled()) return null;
        return getTier(stationDefinition.tier());
    }

    private static double effectiveFailureChance(AlchemyJob job, double base) {
        ProcessTierDefinition tier = stationTier(job);
        double multiplier = tier == null ? 1.0 : tier.failureChanceMultiplier();
        return Math.max(0.0, Math.min(1.0, base * multiplier));
    }

    private static void applyTierModifiers(ItemStackPropertyContainer target,
                                            List<ProcessTierDefinition.PropertyModifier> mods, Random random) {
        for (var m : mods) {
            PropertyValue current = target.get(m.property()).orElse(PropertyValue.ofNumber(0.0));
            if (!current.isNumeric()) continue;
            Modifier modifier = Modifier.builder("talchemy:tier/" + m.property().getString(),
                            m.property(), m.operation())
                    .amount(m.amount()).range(m.min(), m.max()).build();
            writeProperty(target, m.property(), ModifierPipeline.apply(current, List.of(modifier),
                    new ModifierContext(target, random, null)));
        }
    }

    private static CatalystDefinition getCatalyst(String id) {
        if (id == null || id.isBlank()) return null;
        Definition d = CoreRegistries.DEFINITIONS.get(PropertyId.parse(id)).orElse(null);
        return d instanceof CatalystDefinition c ? c : null;
    }

    private static void applyCatalystModifiers(ItemStackPropertyContainer target,
                                                List<CatalystDefinition.Modifier> mods,
                                                Random random) {
        for (var m : mods) {
            PropertyValue current = target.get(m.property()).orElse(PropertyValue.ofNumber(0.0));
            if (!current.isNumeric()) continue;
            Modifier modifier = Modifier.builder("talchemy:catalyst/" + m.property().getString(),
                            m.property(), m.operation())
                    .amount(m.amount()).range(m.min(), m.max()).build();
            writeProperty(target, m.property(), ModifierPipeline.apply(current, List.of(modifier),
                    new ModifierContext(target, random, null)));
        }
    }


    private static StationDefinition getStationForBlock(String blockId) {
        if (blockId == null || blockId.isBlank()) return null;
        return AlchemyDefinitionIndex.stationForBlock(blockId);
    }

    private static StationDefinition getStationForId(String id) {
        if (id == null || id.isBlank()) return null;
        Definition d=CoreRegistries.DEFINITIONS.get(PropertyId.parse(id)).orElse(null);
        return d instanceof StationDefinition s ? s : null;
    }

    private static String resolveStationBlock(String stationId) {
        if (stationId == null || stationId.isBlank()) return "";
        Definition d = CoreRegistries.DEFINITIONS.get(PropertyId.parse(stationId)).orElse(null);
        if (d instanceof StationDefinition station) return station.block();
        return stationId;
    }

    private static ProcessTierDefinition getTier(String id) {
        if (id == null || id.isBlank()) return null;
        Definition d = CoreRegistries.DEFINITIONS.get(PropertyId.parse("talchemy:" + id.replace("talchemy:", ""))).orElse(null);
        if (d instanceof ProcessTierDefinition t) return t;
        return CoreRegistries.DEFINITIONS.get(PropertyId.parse(id)).filter(ProcessTierDefinition.class::isInstance)
                .map(ProcessTierDefinition.class::cast).orElse(null);
    }

    private static ProcessDefinition getProcess(String id) {
        Definition d = CoreRegistries.DEFINITIONS.get(PropertyId.parse(id)).orElse(null);
        return d instanceof ProcessDefinition p ? p : null;
    }

    private static boolean matches(AlchemyDefinition r, ItemStack input, IAlchemyStation station, ItemStack catalyst, ItemStack container) {
        Item in=ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(r.input()));
        if(in==null || input.getCount()<r.inputCount() || !input.is(in)) return false;
        StationDefinition sd=getStationForId(station.stationId());
        if(sd==null || !r.process().equals(sd.process())) return false;
        if (!r.containerInput().isBlank()) {
            Item containerItem = ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(r.containerInput()));
            if (containerItem == null || container == null || container.isEmpty()
                    || !container.is(containerItem) || container.getCount() < r.containerInputCount()) return false;
        }
        if(r.catalyst().isBlank()) return true;
        CatalystDefinition definition=getCatalyst(r.catalyst()); String itemId=definition==null?r.catalyst():definition.item();
        Item cat=ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(itemId));
        return cat!=null && catalyst!=null && !catalyst.isEmpty() && catalyst.is(cat);
    }
}
