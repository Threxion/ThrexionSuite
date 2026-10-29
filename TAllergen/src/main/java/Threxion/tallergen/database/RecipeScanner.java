package Threxion.tallergen.database;

import Threxion.tallergen.CakeRegistry;
import Threxion.tallergen.config.AllergenScope;
import Threxion.tallergen.config.ConfigManager;
import Threxion.tallergen.config.AllergenConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.core.RegistryAccess;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Recipe Scanner：把「过敏原 -> 食物」的映射从 recipes 传播出来。
 *
 * 思路：
 *  1. 每个过敏原的 foods 是直接携带者（种子）。
 *  2. BFS 遍历食谱：凡是配料里出现携带过敏原的物品，其产物继承这些过敏原。
 *  3. visited 集合防止配方循环造成死循环；max_iterations 限制传播深度（间接传播上限）。
 *  4. 黑名单优先于白名单，控制处理范围。
 *
 * 传播只针对"可吃的东西"（isEdible() 或蛋糕方块），避免把过敏原传播到面包、蛋糕之外的
 * 不可食用产物（如小麦合成干草块这种不吃的方块）。
 */
public class RecipeScanner {

    /** 配方扫描是否启用（spec: recipe_scan.enabled）。 */
    public static final boolean ENABLED = true;
    /** 最大迭代次数（spec: recipe_scan.max_iterations）。 */
    public static final int MAX_ITERATIONS = 100;

    private RecipeScanner() {
    }

    public static Map<ResourceLocation, Set<String>> scan(RecipeManager recipeManager, RegistryAccess registryAccess) {
        ConfigManager config = ConfigManager.get();
        List<AllergenConfig> allergens = config.getAllergens();
        AllergenScope scope = config.getScope();
        Map<ResourceLocation, Set<String>> db = new HashMap<>();

        // 1) 直接携带者（种子）
        for (AllergenConfig a : allergens) {
            for (String food : a.foods) {
                ResourceLocation rl = ResourceLocation.tryParse(food);
                if (rl == null) {
                    continue;
                }
                if (scope.isEligible(rl)) {
                    db.computeIfAbsent(rl, k -> new HashSet<>()).add(a.id);
                }
            }
        }

        // 2) BFS 间接传播（recipe_scan.enabled=false 时只保留直接携带者）
        if (ENABLED) {
            propagate(recipeManager, registryAccess, scope, db);
        }
        return db;
    }

    private static void propagate(RecipeManager recipeManager, RegistryAccess registryAccess,
                                  AllergenScope scope, Map<ResourceLocation, Set<String>> db) {
        Deque<ResourceLocation> queue = new ArrayDeque<>(db.keySet());
        Set<ResourceLocation> processed = new HashSet<>();
        Collection<Recipe<?>> recipes = recipeManager.getRecipes();
        int iterations = 0;
        while (!queue.isEmpty() && iterations < MAX_ITERATIONS) {
            iterations++;
            ResourceLocation source = queue.poll();
            if (!processed.add(source)) {
                continue;
            }
            Set<String> sourceAllergens = db.get(source);
            if (sourceAllergens == null || sourceAllergens.isEmpty()) {
                continue;
            }
            for (Recipe<?> recipe : recipes) {
                ItemStack out = recipe.getResultItem(registryAccess);
                if (out.isEmpty()) {
                    continue;
                }
                Item outItem = out.getItem();
                ResourceLocation outId = ForgeRegistries.ITEMS.getKey(outItem);
                if (outId == null || outId.equals(source)) {
                    continue;
                }
                if (!scope.isEligible(outId)) {
                    continue;
                }
                if (!isFoodLike(outItem)) {
                    continue;
                }
                if (!usesItem(recipe, source)) {
                    continue;
                }
                Set<String> outAllergens = db.computeIfAbsent(outId, k -> new HashSet<>());
                boolean changed = outAllergens.addAll(sourceAllergens);
                if (changed && !processed.contains(outId)) {
                    queue.addLast(outId);
                }
            }
        }
    }

    private static boolean usesItem(Recipe<?> recipe, ResourceLocation item) {
        for (Ingredient ing : recipe.getIngredients()) {
            for (ItemStack s : ing.getItems()) {
                ResourceLocation id = ForgeRegistries.ITEMS.getKey(s.getItem());
                if (id != null && id.equals(item)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** 只有可食用物品（含蛋糕类这种通过方块交互吃的食物）才接收传播。 */
    private static boolean isFoodLike(Item item) {
        if (item.isEdible()) {
            return true;
        }
        return CakeRegistry.isCakeItem(item);
    }
}
