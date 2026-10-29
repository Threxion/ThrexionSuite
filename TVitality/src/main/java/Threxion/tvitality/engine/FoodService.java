package Threxion.tvitality.engine;

import Threxion.tvitality.definition.FoodDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

/** JSON-driven food lookup backed by the reload-aware item index. */
public final class FoodService {
    private FoodService() {}
    public static FoodDefinition find(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return id == null ? null : VitalityDefinitionIndex.food(id.toString());
    }
}
