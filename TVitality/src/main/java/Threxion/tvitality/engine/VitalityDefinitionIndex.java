package Threxion.tvitality.engine;

import Threxion.tvitality.definition.*;
import Threxion.threxioncore.CoreRegistries;
import Threxion.threxioncore.api.data.Definition;
import Threxion.threxioncore.api.event.DefinitionReloadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

/** Immutable, reload-aware indexes for hot-path TV definition lookups. */
@Mod.EventBusSubscriber(modid = "tvitality", bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class VitalityDefinitionIndex {
    private static volatile boolean initialized;
    private static volatile int max = 1000;
    private static volatile int initial = 1000;
    private static volatile Map<String, StaminaConsumptionDefinition> consumption = Map.of();
    private static volatile Map<String, FoodDefinition> foods = Map.of();
    private static volatile Map<String, List<StaminaRecoveryDefinition>> recovery = Map.of();
    private static volatile List<StaminaStageDefinition> stages = List.of();

    private VitalityDefinitionIndex() {}

    @SubscribeEvent public static void onDefinitionsReloaded(DefinitionReloadEvent event) { rebuild(event.getDefinitions()); }

    public static synchronized void ensureInitialized() {
        if (!initialized) rebuild(CoreRegistries.DEFINITIONS.values());
    }

    private static synchronized void rebuild(Collection<Definition> definitions) {
        Map<String, StaminaConsumptionDefinition> newConsumption = new HashMap<>();
        Map<String, FoodDefinition> newFoods = new HashMap<>();
        Map<String, List<StaminaRecoveryDefinition>> recoveryBuilder = new HashMap<>();
        List<StaminaStageDefinition> newStages = new ArrayList<>();
        int newMax = 1000, newInitial = 1000;
        boolean settingsFound = false;
        for (Definition def : definitions) {
            if (def instanceof StaminaSettingsDefinition settings && !settingsFound) { newMax = Math.max(1, settings.max()); newInitial = Math.max(0, Math.min(newMax, settings.initial())); settingsFound = true; }
            else if (def instanceof StaminaConsumptionDefinition c) newConsumption.putIfAbsent(c.action(), c);
            else if (def instanceof FoodDefinition food) {
                ResourceLocation item = food.itemId();
                if (item != null && ForgeRegistries.ITEMS.containsKey(item)) newFoods.putIfAbsent(item.toString(), food);
            }
            else if (def instanceof StaminaRecoveryDefinition r) recoveryBuilder.computeIfAbsent(r.mode(), ignored -> new ArrayList<>()).add(r);
            else if (def instanceof StaminaStageDefinition stage) newStages.add(stage);
        }
        Map<String, List<StaminaRecoveryDefinition>> newRecovery = new HashMap<>();
        recoveryBuilder.forEach((mode, list) -> newRecovery.put(mode, List.copyOf(list)));
        newStages.sort(Comparator.comparingInt(StaminaStageDefinition::min).reversed());
        max = newMax; initial = newInitial;
        consumption = Map.copyOf(newConsumption); foods = Map.copyOf(newFoods); recovery = Map.copyOf(newRecovery);
        stages = List.copyOf(newStages); initialized = true;
    }

    public static int max() { ensureInitialized(); return max; }
    public static int initial() { ensureInitialized(); return initial; }
    public static StaminaConsumptionDefinition consumption(String action) { ensureInitialized(); return consumption.get(action); }
    public static FoodDefinition food(String itemId) { ensureInitialized(); return foods.get(itemId); }
    public static List<StaminaRecoveryDefinition> recoveryFor(String mode) { ensureInitialized(); return recovery.getOrDefault(mode, List.of()); }
    public static List<StaminaStageDefinition> stages() { ensureInitialized(); return stages; }
}
