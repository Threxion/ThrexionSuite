package Threxion.tallergen.database;

import Threxion.tallergen.TallergenMod;
import Threxion.tallergen.config.ConfigManager;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Food Allergen Database：物品 -> 过敏原集合 的统一查询入口。
 *
 * 所有食物来源（合成 / 战利品 / 指令 / 其他 Mod）在食用时都统一走这里查询，
 * 客户端不参与判定，结果完全由服务端裁决（food_source_agnostic）。
 *
 * 数据来源：
 *  - /tallergen scan 或服务端启动时（无缓存）调用 RecipeScanner 生成。
 *  - 服务端启动优先读取 config/threxioncore/tallergen/cache/allergen_database.json 缓存。
 */
public class FoodAllergenDatabase {
    private static final FoodAllergenDatabase INSTANCE = new FoodAllergenDatabase();
    private static final int CACHE_VERSION = 1;

    private Map<ResourceLocation, Set<String>> itemAllergens = new HashMap<>();

    public static FoodAllergenDatabase get() {
        return INSTANCE;
    }

    public Set<String> getAllergens(Item item) {
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);
        if (key == null) {
            return Set.of();
        }
        return getAllergens(key);
    }

    public Set<String> getAllergens(ResourceLocation item) {
        Set<String> set = itemAllergens.get(item);
        return set == null ? Set.of() : set;
    }

    public void rebuild(RecipeManager recipeManager, RegistryAccess registryAccess) {
        itemAllergens = RecipeScanner.scan(recipeManager, registryAccess);
        TallergenMod.LOGGER.info("[Tallergen] 食谱扫描完成: {} 个物品携带过敏原", itemAllergens.size());
    }

    /** 尝试从缓存读取数据库；无缓存或版本不符返回 false。 */
    public boolean loadCache() {
        Path file = ConfigManager.get().getCacheFile();
        if (!Files.exists(file)) {
            return false;
        }
        try {
            String content = Files.readString(file, StandardCharsets.UTF_8);
            JsonObject root = JsonParser.parseString(content).getAsJsonObject();
            if (GsonHelper.getAsInt(root, "version", 0) != CACHE_VERSION) {
                return false;
            }
            Map<ResourceLocation, Set<String>> map = new HashMap<>();
            JsonObject allergens = GsonHelper.getAsJsonObject(root, "allergens", new JsonObject());
            for (Map.Entry<String, JsonElement> e : allergens.entrySet()) {
                ResourceLocation rl = ResourceLocation.tryParse(e.getKey());
                if (rl == null) {
                    continue;
                }
                Set<String> set = new HashSet<>();
                JsonArray arr = e.getValue().getAsJsonArray();
                for (JsonElement el : arr) {
                    set.add(el.getAsString());
                }
                map.put(rl, set);
            }
            itemAllergens = map;
            TallergenMod.LOGGER.info("[Tallergen] 从缓存加载过敏原数据库: {} 个物品", map.size());
            return true;
        } catch (Exception e) {
            TallergenMod.LOGGER.error("[Tallergen] 读取过敏原缓存失败: {}", file, e);
            return false;
        }
    }

    public void saveCache() {
        Path file = ConfigManager.get().getCacheFile();
        try {
            Files.createDirectories(file.getParent());
            JsonObject root = new JsonObject();
            root.addProperty("version", CACHE_VERSION);
            JsonObject allergens = new JsonObject();
            for (Map.Entry<ResourceLocation, Set<String>> e : itemAllergens.entrySet()) {
                JsonArray arr = new JsonArray();
                for (String a : e.getValue()) {
                    arr.add(a);
                }
                allergens.add(e.getKey().toString(), arr);
            }
            root.add("allergens", allergens);
            Files.writeString(file, new GsonBuilder().setPrettyPrinting().create().toJson(root), StandardCharsets.UTF_8);
            TallergenMod.LOGGER.info("[Tallergen] 过敏原数据库已写入缓存: {}", file);
        } catch (IOException e) {
            TallergenMod.LOGGER.error("[Tallergen] 写入过敏原缓存失败: {}", file, e);
        }
    }

    public Map<ResourceLocation, Set<String>> getDatabase() {
        return itemAllergens;
    }
}
