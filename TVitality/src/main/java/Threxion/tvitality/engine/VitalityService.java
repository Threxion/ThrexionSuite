package Threxion.tvitality.engine;

import Threxion.tvitality.TVitalityGameRules;
import Threxion.tvitality.definition.StaminaConsumptionDefinition;
import Threxion.tvitality.definition.StaminaRecoveryDefinition;
import Threxion.tvitality.definition.StaminaSettingsDefinition;
import Threxion.tvitality.definition.StaminaStageDefinition;
import Threxion.tvitality.definition.FoodDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** TV 精力运行服务。所有对外 API 最终落到这里。 */
public final class VitalityService {
    private static final UUID FATIGUE_SPEED_UUID = UUID.fromString("9b5c1d6f-1d9b-4f25-9c3f-0a6b7e3d9a21");
    private static final Map<UUID, Set<String>> EXTERNAL_REST = new HashMap<>();

    private VitalityService() {}

    public static int getMaxStamina(Player player) { return configuredMax(); }

    public static int getStamina(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) return configuredMax();
        ensureInitialized(serverPlayer);
        return TVitalityState.stamina(serverPlayer, configuredMax());
    }

    public static int setStamina(ServerPlayer player, int value) {
        int clamped = Math.max(0, Math.min(configuredMax(), value));
        TVitalityState.stamina(player, clamped);
        updateStageModifier(player, clamped);
        return clamped;
    }

    public static int addStamina(ServerPlayer player, int amount) { return setStamina(player, getStamina(player) + amount); }

    /** 返回实际扣除量；不足时只扣除当前剩余精力，不阻止原动作。 */
    public static int consumeStamina(ServerPlayer player, int amount) {
        if (amount <= 0) return 0;
        int before = getStamina(player);
        int actual = Math.min(before, amount);
        setStamina(player, before - actual);
        return actual;
    }

    public static boolean isResting(Player player) {
        if (player.isPassenger()) return false;
        if (player.isSleeping()) return true;
        Set<String> sources = EXTERNAL_REST.get(player.getUUID());
        return sources != null && !sources.isEmpty();
    }

    public static void clearExternalResting(Player player) { EXTERNAL_REST.remove(player.getUUID()); }

    public static void markExternalResting(Player player, String sourceId, boolean resting) {
        if (sourceId == null || sourceId.isBlank()) return;
        Set<String> sources = EXTERNAL_REST.computeIfAbsent(player.getUUID(), ignored -> new HashSet<>());
        if (resting) sources.add(sourceId); else sources.remove(sourceId);
        if (sources.isEmpty()) EXTERNAL_REST.remove(player.getUUID());
    }

    public static void ensureInitialized(ServerPlayer player) {
        if (!TVitalityState.initialized(player)) {
            TVitalityState.stamina(player, configuredInitial());
            TVitalityState.lastRealTime(player, System.currentTimeMillis());
        }
    }

    public static void settleLogin(ServerPlayer player) {
        ensureInitialized(player);
        long now = System.currentTimeMillis();
        long last = TVitalityState.lastRealTime(player);
        if (last > 0 && now > last) applyOfflineRecovery(player, now - last);
        TVitalityState.lastRealTime(player, now);
        updateStageModifier(player, getStamina(player));
    }

    public static void tick(ServerPlayer player) {
        ensureInitialized(player);
        long now = System.currentTimeMillis();
        long last = TVitalityState.lastRealTime(player);
        if (last <= 0) last = now;
        long elapsed = Math.max(0L, now - last);
        int beforeRecovery = getStamina(player);
        if (elapsed > 0) {
            applyOnlineRecovery(player, elapsed);
            TVitalityState.lastRealTime(player, now);
        }
        expireFood(player, true, now);
        expireFood(player, false, now);
        int afterRecovery = getStamina(player);
        if (afterRecovery != beforeRecovery) updateStageModifier(player, afterRecovery);
    }

    public static void consumeAction(ServerPlayer player, String action) {
        StaminaConsumptionDefinition c = VitalityDefinitionIndex.consumption(action);
        if (c != null && c.enabled()) consumeStamina(player, c.amount());
    }

    public static void applyFood(ServerPlayer player, FoodDefinition food) {
        if (!food.enabled()) return;
        long expires = System.currentTimeMillis() + food.durationSeconds() * 1000L;
        boolean meal = food.category() == FoodDefinition.Category.MEAL;
        removeFoodEffects(player, meal);
        TVitalityState.setFood(player, meal, food.getId().getString(), expires);
        if (food.staminaRestore() != 0) addStamina(player, food.staminaRestore());
        for (FoodDefinition.Effect effect : food.effects()) {
            MobEffect mobEffect = ForgeRegistries.MOB_EFFECTS.getValue(ResourceLocation.tryParse(effect.id()));
            if (mobEffect != null && effect.durationTicks() > 0) {
                player.addEffect(new MobEffectInstance(mobEffect, effect.durationTicks(), effect.amplifier(), false, true, true));
            }
        }
    }

    private static void removeFoodEffects(ServerPlayer player, boolean meal) {
        String oldId = TVitalityState.foodId(player, meal);
        if (oldId.isBlank()) return;
        var oldDef = CoreRegistries.DEFINITIONS.get(Threxion.threxioncore.api.PropertyId.parse(oldId)).orElse(null);
        if (!(oldDef instanceof FoodDefinition oldFood)) return;
        for (FoodDefinition.Effect effect : oldFood.effects()) {
            var mobEffect = ForgeRegistries.MOB_EFFECTS.getValue(ResourceLocation.tryParse(effect.id()));
            if (mobEffect != null) player.removeEffect(mobEffect);
        }
    }

    private static void applyOnlineRecovery(ServerPlayer player, long elapsedMs) {
        boolean force = player.level().getGameRules().getBoolean(TVitalityGameRules.FORCE_NORMAL_RECOVERY);
        if (force || isResting(player)) recoverByDefinitions(player, elapsedMs, "rest", false);
    }

    private static void applyOfflineRecovery(ServerPlayer player, long elapsedMs) { recoverByDefinitions(player, elapsedMs, "offline", true); }

    private static void recoverByDefinitions(ServerPlayer player, long elapsedMs, String mode, boolean offline) {
        for (StaminaRecoveryDefinition recovery : VitalityDefinitionIndex.recoveryFor(mode)) {
            if (!recovery.enabled()) continue;
            long intervalMs = recovery.intervalSeconds() * 1000L;
            if (intervalMs <= 0) continue;
            long accumulated = TVitalityState.recoveryAccumulated(player, recovery.getId().getString()) + elapsedMs;
            long units = accumulated / intervalMs;
            if (units > 0) {
                addStamina(player, (int) Math.min(Integer.MAX_VALUE, units * recovery.amount()));
                accumulated -= units * intervalMs;
            }
            TVitalityState.recoveryAccumulated(player, recovery.getId().getString(), accumulated);
        }
    }

    private static void expireFood(ServerPlayer player, boolean meal, long now) {
        long expires = TVitalityState.foodExpires(player, meal);
        if (expires > 0 && now >= expires) {
            removeFoodEffects(player, meal);
            TVitalityState.clearFood(player, meal);
        }
    }

    private static int configuredMax() {
        return VitalityDefinitionIndex.max();
    }

    private static int configuredInitial() {
        return VitalityDefinitionIndex.initial();
    }

    private static StaminaStageDefinition stageFor(int stamina) {
        for (StaminaStageDefinition stage : VitalityDefinitionIndex.stages()) if (stage.contains(stamina)) return stage;
        return null;
    }

    private static void updateStageModifier(ServerPlayer player, int stamina) {
        var attribute = player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
        if (attribute == null) return;
        AttributeModifier existing = attribute.getModifier(FATIGUE_SPEED_UUID);
        StaminaStageDefinition stage = stageFor(stamina);
        double multiplier = stage == null ? 1.0D : stage.movementSpeedMultiplier();
        double desiredAmount = multiplier - 1.0D;
        if (existing != null && Math.abs(existing.getAmount() - desiredAmount) < 1.0E-9) return;
        if (existing != null) attribute.removeModifier(FATIGUE_SPEED_UUID);
        if (multiplier != 1.0D) {
            attribute.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                    FATIGUE_SPEED_UUID, "TVitality stage", multiplier - 1.0D,
                    net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
    }
}
