package Threxion.tallergen.state;

import Threxion.tallergen.config.AllergenConfig;
import Threxion.tallergen.config.ConfigManager;
import Threxion.tallergen.config.ReactionAttribute;
import Threxion.tallergen.config.ReactionConfig;
import Threxion.tallergen.config.ReactionEffect;
import Threxion.tallergen.config.SeverityLevel;
import Threxion.tallergen.database.FoodAllergenDatabase;
import Threxion.tallergen.network.ModNetwork;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 服务端过敏状态机核心。
 *
 * 设计要点：
 *  - 多个过敏原同时命中时分别独立处理，各自维护 Exposure 与 Reaction。
 *  - Exposure 独立于 Reaction 状态机，始终按 interval/amount 衰减，最低 0。
 *  - 潜伏期随机生成；期间再摄入只加 Exposure 并可能缩短剩余潜伏期，不新建 Reaction。
 *  - Severity 从 severity_start 开始，升级由概率决定，Exposure 只影响概率，最高 severity_max。
 *  - 属性修改用独立 AttributeModifier（固定 UUID），仅移除 Tallergen 自己的 Modifier。
 *  - Recovery 结束移除效果与 Modifier、Reaction 归零，Exposure 保留并继续衰减。
 */
public final class AllergyStateManager {
    private static final int DEFAULT_RECOVERY_DURATION_TICKS = 200;

    private AllergyStateManager() {
    }

    /**
     * 玩家实际吃到含过敏原的食物时调用（服务端）。食物来源无关，统一走 Food Allergen Database。
     * 只有玩家确实被分配了该过敏原（playerAllergens 含该过敏原）时才触发反应状态机。
     */
    public static void handleIngest(ServerPlayer player, ItemStack eaten) {
        if (eaten.isEmpty()) {
            return;
        }
        // 新版优先读取 ItemStack 上的 Core Property；没有 Property 时回退到扫描数据库。
        Map<String, Double> exposureMap = Threxion.tallergen.property.AllergenPropertyService.getExposures(eaten);
        if (exposureMap.isEmpty()) {
            for (String allergenId : FoodAllergenDatabase.get().getAllergens(eaten.getItem())) {
                exposureMap.put(allergenId, 1.0);
            }
        }
        if (exposureMap.isEmpty()) {
            return;
        }
        player.getCapability(ModCapabilities.PLAYER_ALLERGY).ifPresent(data -> {
            for (Map.Entry<String, Double> entry : exposureMap.entrySet()) {
                String allergenId = entry.getKey();
                double exposureAmount = entry.getValue();
                if (data.hasAllergen(allergenId) && exposureAmount > 0.0) {
                    ingest(player, data.getOrCreate(allergenId), allergenId, exposureAmount);
                }
            }
            data.markChanged();
            // 摄入是即时事件，直接同步，不受冷却节流影响
            ModNetwork.sendAllergySync(player, data);
        });
    }

    /**
     * 通过命令给玩家分配一个过敏原（服务端）。过敏原需已在配置中存在。
     */
    public static void addAllergen(ServerPlayer player, String allergenId) {
        if (ConfigManager.get().getAllergen(allergenId) == null) {
            return;
        }
        player.getCapability(ModCapabilities.PLAYER_ALLERGY).ifPresent(data -> {
            data.addPlayerAllergen(allergenId);
            data.markChanged();
            ModNetwork.sendAllergySync(player, data);
        });
    }

    /**
     * 通过命令移除玩家的一个过敏原（服务端）。若该过敏原当前有反应（ACTIVE 等），
     * 一并清除其效果/属性 Modifier 并删除其状态条目。
     */
    public static void removeAllergen(ServerPlayer player, String allergenId) {
        player.getCapability(ModCapabilities.PLAYER_ALLERGY).ifPresent(data -> {
            data.removePlayerAllergen(allergenId);
            PlayerAllergenState state = data.getState(allergenId);
            if (state != null) {
                AllergenConfig allergen = ConfigManager.get().getAllergen(allergenId);
                if (allergen != null && state.getReaction().getPhase() != ReactionPhase.NONE) {
                    removeReactionEffects(player, allergen);
                }
                data.removeState(allergenId);
            }
            data.markChanged();
            ModNetwork.sendAllergySync(player, data);
        });
    }

    private static void ingest(ServerPlayer player, PlayerAllergenState state, String allergenId, double exposureAmount) {
        AllergenConfig allergen = ConfigManager.get().getAllergen(allergenId);
        if (allergen == null) {
            return;
        }
        ReactionState reaction = state.getReaction();

        // Exposure 增加，封顶 exposureMax。
        // 不重置过敏效果计时（decayTicks 不清回全间隔）：而是把最大等待时间（衰减间隔）
        // 随 Exposure 提高而缩短，有效间隔 = decay_interval_ticks * (1 - exposure * latency_per_exposure)，
        // 下限为 decay_interval_ticks * latency_minimum_multiplier（复用潜伏期的 exposure 缩短参数）。
        state.setExposure(Math.min(allergen.exposureMax, state.getExposure() + exposureAmount));
        int effectiveInterval = effectiveDecayInterval(allergen, state.getExposure());
        state.setDecayTicks(Math.min(state.getDecayTicks() > 0 ? state.getDecayTicks() : effectiveInterval, effectiveInterval));

        switch (reaction.getPhase()) {
            case NONE -> startLatent(player, state, reaction, allergen);
            case LATENT -> {
                // 潜伏期再摄入：不新建 Reaction，只缩短剩余潜伏期（下限 base * minimum_multiplier）
                int floor = Math.max(1, (int) Math.floor(reaction.getLatencyBase() * allergen.latencyMinimumMultiplier));
                int shortened = (int) Math.floor(reaction.getLatencyRemaining() * (1.0 - allergen.latencyPerExposure));
                reaction.setLatencyRemaining(Math.max(floor, shortened));
            }
            case ACTIVE -> {
                // 发作中再摄入：先尝试升级 Severity；若本次没有升级，则可按 Reaction 配置刷新当前 Severity 的 Effect。
                double chance = Math.min(allergen.severityMaximumChance,
                        allergen.severityBaseUpgradeChance + state.getExposure() * allergen.severityPerExposure);
                boolean upgraded = false;
                if (reaction.getSeverity() < allergen.severityMax && player.getRandom().nextDouble() < chance) {
                    removeReactionEffects(player, allergen);
                    reaction.setSeverity(reaction.getSeverity() + 1);
                    SeverityLevel level = resolveLevel(allergen, reaction.getSeverity());
                    // 不重置过敏时间：新时长 = 基础过敏时间 + 该过敏原剩余的持续时间。
                    int base = level != null ? level.durationTicks : allergen.activeDurationTicks;
                    reaction.setActiveRemaining(base + reaction.getActiveRemaining());
                    applyReactionEffects(player, allergen, level);
                    upgraded = true;
                }

                // 未升级（包括已经达到最高 Severity）时，重复摄入可以刷新当前 Severity 的 Effect 持续时间。
                // 这不会重置 ACTIVE 计时器，也不会改变 Severity；只重新应用当前等级配置的效果。
                if (!upgraded && shouldRefreshEffectsOnRepeat(allergen)) {
                    applyReactionEffects(player, allergen, resolveLevel(allergen, reaction.getSeverity()));
                }
            }
            case RECOVERY -> {
                // 恢复期再摄入：重新开始一轮反应
                startLatent(player, state, reaction, allergen);
            }
            default -> {
            }
        }
    }

    /**
     * 玩家非死亡克隆（如末地返回）后（PlayerEvent.Clone, isWasDeath=false）调用：重新施加所有 ACTIVE
     * 反应的效果与 Modifier，因为新实体身上不会继承旧实体的效果/属性修改。
     */
    public static void reapplyActiveEffects(Player player) {
        player.getCapability(ModCapabilities.PLAYER_ALLERGY).ifPresent(data -> {
            for (PlayerAllergenState state : data.getStates()) {
                ReactionState r = state.getReaction();
                if (r.getPhase() == ReactionPhase.ACTIVE) {
                    AllergenConfig allergen = ConfigManager.get().getAllergen(state.getAllergenId());
                    if (allergen != null) {
                        applyReactionEffects(player, allergen, resolveLevel(allergen, r.getSeverity()));
                    }
                }
            }
        });
    }

    /**
     * 玩家死亡（PlayerEvent.Clone, isWasDeath=true）时调用：清除当前过敏发作的效果
     * （MobEffect / AttributeModifier），并把 Reaction 归零（LATENT/ACTIVE/RECOVERY -> NONE），
     * Exposure 保留并继续衰减——即仍保留对过敏原的过敏，下次食用会重新进入潜伏期并再次发作。
     * 需在旧（死亡）实体数据上先执行，随后 PlayerAllergyData.copyFrom 把归零后的状态带到新实体。
     */
    public static void resetReactionsOnDeath(Player player) {
        player.getCapability(ModCapabilities.PLAYER_ALLERGY).ifPresent(data -> {
            for (PlayerAllergenState state : data.getStates()) {
                ReactionState r = state.getReaction();
                if (r.getPhase() == ReactionPhase.NONE) {
                    continue;
                }
                AllergenConfig allergen = ConfigManager.get().getAllergen(state.getAllergenId());
                if (allergen != null) {
                    removeReactionEffects(player, allergen);
                }
                r.setPhase(ReactionPhase.NONE);
                r.setSeverity(0);
                r.setLatencyRemaining(0);
                r.setLatencyBase(0);
                r.setActiveRemaining(0);
                r.setRecoveryRemaining(0);
            }
        });
    }

    private static void startLatent(ServerPlayer player, PlayerAllergenState state, ReactionState reaction, AllergenConfig allergen) {
        int min = allergen.latencyMinTicks;
        int max = allergen.latencyMaxTicks;
        int base = min + (max > min ? player.getRandom().nextInt(max - min + 1) : 0);
        reaction.setPhase(ReactionPhase.LATENT);
        reaction.setSeverity(allergen.severityStart);
        reaction.setLatencyBase(base);
        // Exposure 缩短潜伏期：乘 (1 - exposure * per_exposure)，下限 base * minimum_multiplier
        double multiplier = Math.max(allergen.latencyMinimumMultiplier,
                1.0 - state.getExposure() * allergen.latencyPerExposure);
        int effective = (int) Math.floor(base * multiplier);
        reaction.setLatencyRemaining(Math.max(1, effective));
    }

    /** 服务端每 tick 对玩家调用一次。 */
    public static void tickPlayer(ServerPlayer player) {
        player.getCapability(ModCapabilities.PLAYER_ALLERGY).ifPresent(data -> {
            data.tickSyncCooldown();
            boolean changed = false;
            for (PlayerAllergenState state : data.getStates()) {
                changed |= tickState(player, state);
            }
            if (changed) {
                data.markChanged();
            }
            if (data.shouldSync()) {
                ModNetwork.sendAllergySync(player, data);
            }
        });
    }

    private static boolean tickState(ServerPlayer player, PlayerAllergenState state) {
        AllergenConfig allergen = ConfigManager.get().getAllergen(state.getAllergenId());
        if (allergen == null) {
            return false;
        }
        ReactionState reaction = state.getReaction();
        boolean changed = false;

        // Exposure 衰减（独立于 Reaction 状态机，最低 minExposure，默认 0）。
        // 先判断再递减：decayTicks 归零才衰减并重置计时器，
        // 避免初始 0 值被 -1 提前触发，保证衰减周期恰为当前有效间隔（随 Exposure 缩短）
        if (state.getDecayTicks() > 0) {
            state.setDecayTicks(state.getDecayTicks() - 1);
        }
        if (state.getDecayTicks() <= 0) {
            state.setDecayTicks(effectiveDecayInterval(allergen, state.getExposure()));
            if (state.getExposure() > allergen.minExposure) {
                double newExposure = Math.max(allergen.minExposure, state.getExposure() - allergen.decayAmount);
                if (newExposure != state.getExposure()) {
                    state.setExposure(newExposure);
                    changed = true;
                }
            }
        }

        switch (reaction.getPhase()) {
            case LATENT -> {
                reaction.setLatencyRemaining(reaction.getLatencyRemaining() - 1);
                if (reaction.getLatencyRemaining() <= 0) {
                    reaction.setPhase(ReactionPhase.ACTIVE);
                    reaction.setSeverity(allergen.severityStart);
                    SeverityLevel level = resolveLevel(allergen, reaction.getSeverity());
                    reaction.setActiveRemaining(level != null ? level.durationTicks : allergen.activeDurationTicks);
                    applyReactionEffects(player, allergen, level);
                    changed = true;
                }
            }
            case ACTIVE -> {
                reaction.setActiveRemaining(reaction.getActiveRemaining() - 1);
                if (reaction.getActiveRemaining() <= 0) {
                    removeReactionEffects(player, allergen);
                    reaction.setPhase(ReactionPhase.RECOVERY);
                    reaction.setRecoveryRemaining(resolveRecoveryDuration(allergen));
                    changed = true;
                }
            }
            case RECOVERY -> {
                reaction.setRecoveryRemaining(reaction.getRecoveryRemaining() - 1);
                if (reaction.getRecoveryRemaining() <= 0) {
                    // Recovery 结束：Reaction 归零，Exposure 保留并继续衰减
                    reaction.setPhase(ReactionPhase.NONE);
                    reaction.setSeverity(0);
                    changed = true;
                }
            }
            default -> {
            }
        }
        return changed;
    }

    private static SeverityLevel resolveLevel(AllergenConfig allergen, int severity) {
        ReactionConfig rc = ConfigManager.get().getReaction(allergen.reactionId);
        return rc == null ? null : rc.getLevel(severity);
    }

    private static boolean shouldRefreshEffectsOnRepeat(AllergenConfig allergen) {
        ReactionConfig rc = ConfigManager.get().getReaction(allergen.reactionId);
        return rc != null && rc.refreshEffectsOnRepeatIngest;
    }

    /**
     * 当前 Exposure 对应的有效衰减间隔（最大等待时间）：Exposure 越高，间隔越短。
     * 复用潜伏期的 exposure 缩短参数（latency_per_exposure / latency_minimum_multiplier）。
     */
    private static int effectiveDecayInterval(AllergenConfig allergen, double exposure) {
        double multiplier = Math.max(allergen.latencyMinimumMultiplier,
                1.0 - exposure * allergen.latencyPerExposure);
        return Math.max(1, (int) Math.floor(allergen.decayIntervalTicks * multiplier));
    }

    private static int resolveRecoveryDuration(AllergenConfig allergen) {
        ReactionConfig rc = ConfigManager.get().getReaction(allergen.reactionId);
        return rc != null ? rc.recoveryDurationTicks : DEFAULT_RECOVERY_DURATION_TICKS;
    }

    private static void applyReactionEffects(Player player, AllergenConfig allergen, @Nullable SeverityLevel level) {
        if (level == null) {
            return;
        }
        for (ReactionEffect e : level.effects) {
            MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(e.effectId);
            if (effect == null) {
                continue;
            }
            int duration = e.durationTicks > 0 ? e.durationTicks : level.durationTicks;
            player.addEffect(new MobEffectInstance(effect, duration, e.amplifier, false, false, false));
        }
        for (ReactionAttribute a : level.attributes) {
            Attribute attr = ForgeRegistries.ATTRIBUTES.getValue(a.attributeId);
            if (attr == null) {
                continue;
            }
            AttributeInstance inst = player.getAttribute(attr);
            if (inst == null) {
                continue;
            }
            UUID uuid = modifierUuid(allergen, a.attributeId.toString());
            inst.removeModifier(uuid);
            inst.addTransientModifier(new AttributeModifier(uuid, "tallergen:" + allergen.id, a.amount, a.operation));
        }
    }

    private static void removeReactionEffects(Player player, AllergenConfig allergen) {
        ReactionConfig rc = ConfigManager.get().getReaction(allergen.reactionId);
        if (rc == null) {
            return;
        }
        for (SeverityLevel level : rc.severityLevels) {
            for (ReactionEffect e : level.effects) {
                MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(e.effectId);
                if (effect != null) {
                    player.removeEffect(effect);
                }
            }
            for (ReactionAttribute a : level.attributes) {
                Attribute attr = ForgeRegistries.ATTRIBUTES.getValue(a.attributeId);
                if (attr == null) {
                    continue;
                }
                AttributeInstance inst = player.getAttribute(attr);
                if (inst != null) {
                    inst.removeModifier(modifierUuid(allergen, a.attributeId.toString()));
                }
            }
        }
    }

    /**
     * 固定 UUID：由 (过敏原 attribute_modifier_uuid + 属性 id) 派生。
     * 保证是 Tallergen 专属、稳定可复现，移除时只删自己的 Modifier。
     */
    private static UUID modifierUuid(AllergenConfig allergen, String attributeId) {
        return UUID.nameUUIDFromBytes(
                (allergen.attributeModifierUuid + ":" + attributeId).getBytes(StandardCharsets.UTF_8));
    }
    /**
     * 供 TAlchemy 等可选整合调用：修改玩家当前过敏原暴露值。
     * 不会凭空给玩家添加一个未拥有的过敏原；只有玩家已经拥有该过敏原才生效。
     * operation 使用 Core ModifierOperation 的字符串形式：set/add/multiply/random_add/random_multiply。
     */
    public static void modifyExposure(ServerPlayer player, String allergenId, String operation,
                                      double amount, double min, double max) {
        if (player == null || allergenId == null || allergenId.isBlank()) return;
        player.getCapability(ModCapabilities.PLAYER_ALLERGY).ifPresent(data -> {
            if (!data.hasAllergen(allergenId)) return;
            AllergenConfig allergen = ConfigManager.get().getAllergen(allergenId);
            PlayerAllergenState state = data.getState(allergenId);
            if (allergen == null || state == null) return;

            double current = state.getExposure();
            double result;
            switch (operation == null ? "add" : operation.toLowerCase(java.util.Locale.ROOT)) {
                case "set" -> result = amount;
                case "multiply", "mul" -> result = current * amount;
                case "random_add", "randomadd" -> result = current +
                        (min + player.getRandom().nextDouble() * (max - min));
                case "random_multiply", "randommultiply", "random_mul" -> result = current *
                        (min + player.getRandom().nextDouble() * (max - min));
                case "add" -> result = current + amount;
                default -> { return; }
            }

            // 暴露值仍遵循 TAllergen 自己的定义上限；药物可以把它降低到 0。
            result = Math.max(0.0, Math.min(allergen.exposureMax, result));
            state.setExposure(result);
            data.markChanged();
            ModNetwork.sendAllergySync(player, data);
        });
    }

}
