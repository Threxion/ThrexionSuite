package Threxion.tallergen.config;

import Threxion.tallergen.TallergenMod;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.fml.loading.FMLPaths;
import Threxion.threxioncore.config.JsoncLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * 数据驱动配置总入口。
 *
 * 目录结构（均在 config/threxioncore/tallergen/ 下）：
 *   allergens/<id>.json   过敏原定义（含 initial_allergy_chance 初始过敏概率）
 *   reactions/<id>.json   反应定义
 *   option.json           分配选项（max_allergens / min_allergens / allergen_assignment_chance）
 *   player.json           玩家随机分配总开关（random_allergen_enabled）
 *   blacklist.json        黑名单（优先级最高）
 *   whitelist.json        白名单
 *   example/              过敏原/反应配置格式范例（仅参考，不参与加载）
 *   cache/allergen_database.json   Recipe Scanner 的产物缓存
 *
 * 整合包作者无需改 Java：改 json -> /tallergen scan（自动重载配置并重扫）-> /tallergen inspect。
 */
public class ConfigManager {
    private static ConfigManager INSTANCE;

    private final Path configDir;
    private final Map<String, AllergenConfig> allergens = new LinkedHashMap<>();
    private final Map<String, ReactionConfig> reactions = new LinkedHashMap<>();
    private final AllergenScope scope = new AllergenScope();
    private PlayerConfig playerConfig = new PlayerConfig(true, 0.05);
    private OptionConfig optionConfig = new OptionConfig(1, 0, 0.15);

    /** jar 内置的默认配置文件列表，首启复制到 config 目录。 */
    private static final String[][] DEFAULT_FILES = {
            {"allergens", "wheat.json"},
            {"reactions", "wheat_reaction.json"},
            {"", "blacklist.json"},
            {"", "whitelist.json"},
            {"", "player.json"},
            {"", "option.json"},
            {"example", "allergen_example.json"},
            {"example", "reaction_example.json"},
    };

    private ConfigManager() {
        this.configDir = FMLPaths.CONFIGDIR.get().resolve("threxioncore").resolve(TallergenMod.MOD_ID);
    }

    public static ConfigManager get() {
        if (INSTANCE == null) {
            INSTANCE = new ConfigManager();
        }
        return INSTANCE;
    }

    public void init() {
        copyDefaultsIfMissing();
        loadAll();
    }

    public Path getConfigDir() {
        return configDir;
    }

    public Path getCacheFile() {
        return configDir.resolve("cache").resolve("allergen_database.json");
    }

    private void copyDefaultsIfMissing() {
        for (String[] entry : DEFAULT_FILES) {
            String sub = entry[0];
            String file = entry[1];
            Path target = sub.isEmpty() ? configDir.resolve(file) : configDir.resolve(sub).resolve(file);
            if (Files.exists(target)) {
                continue;
            }
            String resourcePath = "/config_defaults/" + (sub.isEmpty() ? "" : sub + "/") + file;
            try (InputStream in = getClass().getResourceAsStream(resourcePath)) {
                if (in == null) {
                    TallergenMod.LOGGER.warn("[Tallergen] 未找到内置默认配置文件 {}", resourcePath);
                    continue;
                }
                Files.createDirectories(target.getParent());
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
                TallergenMod.LOGGER.info("[Tallergen] 已生成默认配置文件 {}", target);
            } catch (IOException e) {
                TallergenMod.LOGGER.error("[Tallergen] 复制默认配置文件失败: {}", target, e);
            }
        }
    }

    public void loadAll() {
        allergens.clear();
        reactions.clear();
        scope.clear();
        loadAllergens();
        loadReactions();
        loadListFile(configDir.resolve("blacklist.json"), true);
        loadListFile(configDir.resolve("whitelist.json"), false);
        loadPlayerConfig();
        loadOptionConfig();
        TallergenMod.LOGGER.info(
                "[Tallergen] 配置加载完成: {} 个过敏原, {} 个反应, 黑名单 {} 项, 白名单 {} 项",
                allergens.size(), reactions.size(), scope.getBlacklist().size(), scope.getWhitelist().size());
    }

    private void loadPlayerConfig() {
        Path file = configDir.resolve("player.json");
        if (!Files.exists(file)) {
            playerConfig = new PlayerConfig(true, 0.05);
            return;
        }
        try {
            JsonObject obj = readJson(file);
            playerConfig = obj == null ? new PlayerConfig(true, 0.05) : PlayerConfig.fromJson(obj);
        } catch (Exception e) {
            TallergenMod.LOGGER.error("[Tallergen] 解析玩家配置失败: {}", file, e);
        }
    }

    private void loadOptionConfig() {
        Path file = configDir.resolve("option.json");
        if (!Files.exists(file)) {
            optionConfig = new OptionConfig(1, 0, 0.15);
            return;
        }
        try {
            JsonObject obj = readJson(file);
            optionConfig = obj == null ? new OptionConfig(1, 0, 0.15) : OptionConfig.fromJson(obj);
        } catch (Exception e) {
            TallergenMod.LOGGER.error("[Tallergen] 解析分配选项配置失败: {}", file, e);
        }
    }

    private void loadAllergens() {
        Path dir = configDir.resolve("allergens");
        if (!Files.isDirectory(dir)) {
            return;
        }
        try (Stream<Path> files = Files.list(dir)) {
            files.filter(p -> p.getFileName().toString().endsWith(".json"))
                    .sorted()
                    .forEach(p -> {
                        try {
                            JsonObject obj = readJson(p);
                            if (obj == null) {
                                return;
                            }
                            AllergenConfig cfg = AllergenConfig.fromJson(p.getFileName().toString(), obj);
                            allergens.put(cfg.id, cfg);
                        } catch (Exception e) {
                            TallergenMod.LOGGER.error("[Tallergen] 解析过敏原配置失败: {}", p, e);
                        }
                    });
        } catch (IOException e) {
            TallergenMod.LOGGER.error("[Tallergen] 读取过敏原目录失败: {}", dir, e);
        }
    }

    private void loadReactions() {
        Path dir = configDir.resolve("reactions");
        if (!Files.isDirectory(dir)) {
            return;
        }
        try (Stream<Path> files = Files.list(dir)) {
            files.filter(p -> p.getFileName().toString().endsWith(".json"))
                    .sorted()
                    .forEach(p -> {
                        try {
                            JsonObject obj = readJson(p);
                            if (obj == null) {
                                return;
                            }
                            ReactionConfig cfg = ReactionConfig.fromJson(p.getFileName().toString(), obj);
                            reactions.put(cfg.id, cfg);
                        } catch (Exception e) {
                            TallergenMod.LOGGER.error("[Tallergen] 解析反应配置失败: {}", p, e);
                        }
                    });
        } catch (IOException e) {
            TallergenMod.LOGGER.error("[Tallergen] 读取反应目录失败: {}", dir, e);
        }
    }

    private void loadListFile(Path file, boolean isBlacklist) {
        if (!Files.exists(file)) {
            return;
        }
        try {
            JsonObject obj = readJson(file);
            if (obj == null) {
                return;
            }
            boolean enabled = GsonHelper.getAsBoolean(obj, "enabled", true);
            JsonArray arr = GsonHelper.getAsJsonArray(obj, "items", new JsonArray());
            for (JsonElement e : arr) {
                ResourceLocation rl = ResourceLocation.tryParse(e.getAsString());
                if (rl == null) {
                    continue;
                }
                if (isBlacklist) {
                    scope.addBlacklistItem(rl);
                } else {
                    scope.addWhitelistItem(rl);
                }
            }
            if (isBlacklist) {
                scope.setBlacklistEnabled(enabled);
            } else {
                scope.setWhitelistEnabled(enabled);
            }
        } catch (Exception e) {
            TallergenMod.LOGGER.error("[Tallergen] 解析列表文件失败: {}", file, e);
        }
    }

    private JsonObject readJson(Path p) throws IOException {
        String content = Files.readString(p, StandardCharsets.UTF_8);
        if (content.isBlank()) {
            return null;
        }
        JsonObject object = JsoncLoader.parseObject(content);
        if (object == null) throw new IllegalArgumentException("Invalid JSONC object: " + p);
        return object;
    }

    public List<AllergenConfig> getAllergens() {
        return List.copyOf(allergens.values());
    }

    public AllergenConfig getAllergen(String id) {
        return id == null ? null : allergens.get(id);
    }

    public Map<String, ReactionConfig> getReactions() {
        return reactions;
    }

    public ReactionConfig getReaction(String id) {
        return id == null ? null : reactions.get(id);
    }

    public AllergenScope getScope() {
        return scope;
    }

    public PlayerConfig getPlayerConfig() {
        return playerConfig;
    }

    public OptionConfig getOptionConfig() {
        return optionConfig;
    }

    /**
     * 给过敏原的 foods（直接携带者）列表加入一个物品并持久化到过敏原 JSON，随后热重载该过敏原。
     *
     * @return false 表示过敏原不存在或写入失败
     */
    public boolean addAllergenFood(String allergenId, ResourceLocation item) {
        return updateAllergenFoods(allergenId, item, true);
    }

    /** 从过敏原的 foods 列表移除一个物品并持久化，随后热重载该过敏原。 */
    public boolean removeAllergenFood(String allergenId, ResourceLocation item) {
        return updateAllergenFoods(allergenId, item, false);
    }

    private boolean updateAllergenFoods(String allergenId, ResourceLocation item, boolean add) {
        if (allergens.get(allergenId) == null) {
            return false;
        }
        Path file = configDir.resolve("allergens").resolve(allergenId + ".json");
        try {
            JsonObject obj = Files.exists(file) ? readJson(file) : null;
            if (obj == null) {
                obj = new JsonObject();
                obj.addProperty("id", allergenId);
            }
            JsonArray foods = GsonHelper.getAsJsonArray(obj, "foods", new JsonArray());
            String value = item.toString();
            boolean found = false;
            for (int i = 0; i < foods.size(); i++) {
                if (value.equals(foods.get(i).getAsString())) {
                    if (!add) {
                        foods.remove(i);
                    }
                    found = true;
                    break;
                }
            }
            if (add && found) {
                return true;
            }
            if (!add && !found) {
                return false;
            }
            if (add) {
                foods.add(value);
            }
            obj.add("foods", foods);
            Files.createDirectories(file.getParent());
            Files.writeString(file, new GsonBuilder().setPrettyPrinting().create().toJson(obj), StandardCharsets.UTF_8);
            allergens.put(allergenId, AllergenConfig.fromJson(allergenId + ".json", obj));
            return true;
        } catch (Exception e) {
            TallergenMod.LOGGER.error("[Tallergen] 写入过敏原来源失败: {}", file, e);
            return false;
        }
    }

    /**
     * 增删黑名单/白名单条目并持久化到对应的列表 JSON。
     *
     * @param isBlacklist true=黑名单，false=白名单
     * @return 写入成功与否
     */
    public boolean modifyListEntry(boolean isBlacklist, ResourceLocation item, boolean add) {
        Set<ResourceLocation> entries = isBlacklist ? scope.getBlacklist() : scope.getWhitelist();
        if (add) {
            entries.add(item);
        } else {
            entries.remove(item);
        }
        Path file = isBlacklist ? configDir.resolve("blacklist.json") : configDir.resolve("whitelist.json");
        JsonObject obj = new JsonObject();
        obj.addProperty("enabled", isBlacklist ? scope.isBlacklistEnabled() : scope.isWhitelistEnabled());
        JsonArray arr = new JsonArray();
        for (ResourceLocation rl : entries) {
            arr.add(rl.toString());
        }
        obj.add("items", arr);
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, new GsonBuilder().setPrettyPrinting().create().toJson(obj), StandardCharsets.UTF_8);
            return true;
        } catch (IOException e) {
            TallergenMod.LOGGER.error("[Tallergen] 写入{}失败: {}", isBlacklist ? "黑名单" : "白名单", file, e);
            return false;
        }
    }
}
