package Threxion.talchemy.engine;

import Threxion.talchemy.definition.*;
import Threxion.threxioncore.CoreRegistries;
import Threxion.threxioncore.api.data.Definition;
import Threxion.threxioncore.api.event.DefinitionReloadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.*;

/** Reload-aware indexes for recipe, station-block and reaction lookups. */
@Mod.EventBusSubscriber(modid = "talchemy", bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class AlchemyDefinitionIndex {
    private static volatile boolean initialized;
    private static volatile Map<String, List<AlchemyDefinition>> recipesByInput = Map.of();
    private static volatile List<AlchemyReactionDefinition> reactions = List.of();
    private static volatile Map<String, StationDefinition> stationsByBlock = Map.of();
    private AlchemyDefinitionIndex() {}
    @SubscribeEvent public static void onDefinitionsReloaded(DefinitionReloadEvent event) { rebuild(event.getDefinitions()); }
    public static synchronized void ensureInitialized() { if (!initialized) rebuild(CoreRegistries.DEFINITIONS.values()); }
    private static synchronized void rebuild(Collection<Definition> defs) {
        List<AlchemyDefinition> newRecipes = new ArrayList<>();
        Map<String, List<AlchemyDefinition>> recipeBuilder = new HashMap<>();
        List<AlchemyReactionDefinition> newReactions = new ArrayList<>();
        Map<String, StationDefinition> newStations = new HashMap<>();
        for (Definition d : defs) {
            if (d instanceof AlchemyDefinition recipe) {
                newRecipes.add(recipe);
                ResourceLocation input = ResourceLocation.tryParse(recipe.input());
                if (input != null) recipeBuilder.computeIfAbsent(input.toString(), ignored -> new ArrayList<>()).add(recipe);
            }
            else if (d instanceof AlchemyReactionDefinition reaction) newReactions.add(reaction);
            else if (d instanceof StationDefinition station && !station.block().isBlank()) newStations.putIfAbsent(station.block(), station);
        }
        Map<String, List<AlchemyDefinition>> indexedRecipes = new HashMap<>();
        recipeBuilder.forEach((id, list) -> indexedRecipes.put(id, List.copyOf(list)));
        recipesByInput = Map.copyOf(indexedRecipes); reactions = List.copyOf(newReactions);
        stationsByBlock = Map.copyOf(newStations); initialized = true;
    }
    public static List<AlchemyDefinition> recipesForInput(ItemStack stack) {
        ensureInitialized();
        if (stack == null || stack.isEmpty()) return List.of();
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return id == null ? List.of() : recipesByInput.getOrDefault(id.toString(), List.of());
    }
    public static List<AlchemyReactionDefinition> reactions() { ensureInitialized(); return reactions; }
    public static StationDefinition stationForBlock(String blockId) { ensureInitialized(); return stationsByBlock.get(blockId); }
}
