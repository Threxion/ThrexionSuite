package Threxion.talchemy.engine;

import Threxion.talchemy.definition.AlchemyEffectDefinition;
import Threxion.talchemy.definition.AlchemyReactionDefinition;
import Threxion.threxioncore.CoreRegistries;
import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.event.AlchemyEffectEvent;
import Threxion.threxioncore.api.property.ItemStackPropertyContainer;
import Threxion.threxioncore.api.property.PropertyValue;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraft.network.chat.Component;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消费药物时根据药力属性选择最高匹配的 Reaction。
 * 同一药力属性只触发一个最高阈值反应，避免 heal=180 同时触发治疗1和治疗2。
 */
@Mod.EventBusSubscriber(modid = "talchemy", bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class AlchemyConsumptionHandler {
    private AlchemyConsumptionHandler() {}

    @SubscribeEvent
    public static void onUseStarted(LivingEntityUseItemEvent.Start event) {
        ItemStack stack = event.getItem();
        if (stack.isEmpty()) return;
        if (!AlchemyExpirationService.applyOnReference(stack, event.getEntity().level())) {
            event.setCanceled(true);
            if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
                player.displayClientMessage(Component.literal("这瓶药水已经失效了。"), true);
            }
        }
    }

    @SubscribeEvent
    public static void onUseFinished(LivingEntityUseItemEvent.Finish event) {
        LivingEntity entity = event.getEntity();
        ItemStack stack = event.getItem();
        if (stack.isEmpty()) return;
        // Finish 是第二次引用点：确保 add_potency 已经写入后，再选择 Reaction。
        if (!AlchemyExpirationService.applyOnReference(stack, entity.level())) return;
        applyReactions(entity, stack);
    }

    public static void applyReactions(LivingEntity entity, ItemStack stack) {
        ItemStackPropertyContainer properties = new ItemStackPropertyContainer(stack);
        Map<PropertyId, AlchemyReactionDefinition> best = new HashMap<>();

        for (AlchemyReactionDefinition reaction : AlchemyDefinitionIndex.reactions()) {
            PropertyValue value = properties.get(reaction.potencyProperty()).orElse(null);
            if (value == null || !value.isNumeric()) continue;
            if (value.asDouble() < reaction.threshold()) continue;

            AlchemyReactionDefinition previous = best.get(reaction.potencyProperty());
            if (previous == null
                    || reaction.threshold() > previous.threshold()
                    || (reaction.threshold() == previous.threshold() && reaction.priority() > previous.priority())) {
                best.put(reaction.potencyProperty(), reaction);
            }
        }

        for (AlchemyReactionDefinition reaction : best.values()) {
            Definition raw = CoreRegistries.DEFINITIONS.get(reaction.effect()).orElse(null);
            if (raw instanceof AlchemyEffectDefinition effect) {
                applyEffect(entity, stack, effect);
            }
        }
    }

    private static void applyEffect(LivingEntity entity, ItemStack source, AlchemyEffectDefinition effect) {
        List<AlchemyEffectEvent.PropertyModifier> modifiers = new ArrayList<>();
        for (var modifier : effect.propertyModifiers()) {
            modifiers.add(new AlchemyEffectEvent.PropertyModifier(
                    modifier.property().toString(), modifier.operation().name().toLowerCase(),
                    modifier.amount(), modifier.min(), modifier.max()));
        }
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new AlchemyEffectEvent(
                entity, source, effect.getId().toString(), effect.displayName(), modifiers));

        for (var potion : effect.potionEffects()) {
            ResourceLocation rl = ResourceLocation.tryParse(potion.effect());
            if (rl == null) continue;
            MobEffect mobEffect = ForgeRegistries.MOB_EFFECTS.getValue(rl);
            if (mobEffect == null) continue;
            entity.addEffect(new MobEffectInstance(mobEffect, potion.durationTicks(), potion.amplifier(),
                    potion.ambient(), potion.showParticles(), potion.showIcon()));
        }
    }
}
