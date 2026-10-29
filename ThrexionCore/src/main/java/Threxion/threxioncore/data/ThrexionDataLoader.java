package Threxion.threxioncore.data;

import Threxion.threxioncore.CoreRegistries;
import Threxion.threxioncore.ThrexionConstants;
import Threxion.threxioncore.ThrexionCore;
import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.data.CoreDefinitionLoader;
import Threxion.threxioncore.api.data.Definition;
import Threxion.threxioncore.api.data.DefinitionLoader;
import Threxion.threxioncore.api.data.DefinitionLoaderRegistry;
import Threxion.threxioncore.api.event.DefinitionLoadEvent;
import Threxion.threxioncore.api.event.DefinitionReloadEvent;
import Threxion.threxioncore.api.event.IntegrationLoadEvent;
import Threxion.threxioncore.api.integration.Integration;
import Threxion.threxioncore.api.modifier.Modifier;
import Threxion.threxioncore.api.modifier.ModifierOperation;
import Threxion.threxioncore.api.modifier.ModifierSource;
import Threxion.threxioncore.api.property.PropertySchema;
import Threxion.threxioncore.api.property.PropertyType;
import Threxion.threxioncore.api.transfer.Transfer;
import Threxion.threxioncore.api.transfer.TransferOperation;
import Threxion.threxioncore.config.JsoncLoader;
import Threxion.threxioncore.config.ThrexionConfig;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * 数据加载流水线（规格 load_flow）：
 * read_config → load_property_schema → load_definition → load_modifier →
 * load_transfer → detect_mod → load_integration → sync_data → mods_running。
 *
 * <p>错误处理（规格 error_handling）：单个数据文件错误仅跳过该文件，
 * 不中断整体加载；日志指明具体文件与缺失字段。</p>
 *
 * <p>热重载（规格 reload_config）：/reload 会重读 config / definition /
 * modifier / transfer / partial_schema（properties JSON）。Java API 与
 * Registry 类型不可热重载——因此重载时只清除由 JSON 加载的条目，保留
 * 各 Mod 通过代码注册的条目。</p>
 */
public final class ThrexionDataLoader {

    private static final Set<PropertyId> JSON_SCHEMA_IDS = new HashSet<>();
    private static final Set<PropertyId> JSON_DEFINITION_IDS = new HashSet<>();
    private static final Set<PropertyId> JSON_MODIFIER_IDS = new HashSet<>();
    private static final Set<PropertyId> JSON_TRANSFER_IDS = new HashSet<>();

    private static final Map<String, Boolean> DETECTED_MODS = new LinkedHashMap<>();

    private static volatile boolean loaded = false;

    private ThrexionDataLoader() {
    }

    public static boolean isLoaded() {
        return loaded;
    }

    public static Map<String, Boolean> getDetectedMods() {
        return Map.copyOf(DETECTED_MODS);
    }

    /** 首次加载（FMLCommonSetupEvent，主线程）。 */
    public static void initialLoad() {
        ThrexionCore.LOGGER.info("[threxioncore] Initial load starting...");
        // read_config
        ThrexionConfig.load();
        // ensure data directories + first-run examples
        try {
            ensureDirectories();
        } catch (IOException e) {
            ThrexionCore.LOGGER.error("[threxioncore] Failed to create config directories: {}", e.getMessage());
        }
        // load_property_schema
        loadPropertySchemas();
        // load_definition_loader（Core 自带加载器 + 各 T Mod 注册的加载器）
        loadDefinitionLoaders();
        // load_definition
        loadDefinitions();
        // load_modifier
        loadModifiers();
        // load_transfer
        loadTransfers();
        // detect_mod
        detectMods();
        // load_integration
        loadIntegrations();
        loaded = true;
        // sync_data：由 OnDatapackSyncEvent（玩家进服 / datapack reload）负责下发
        ThrexionCore.LOGGER.info("[threxioncore] Initial load complete: {} properties, {} definitions, {} modifiers, {} transfers",
                CoreRegistries.PROPERTIES.size(), CoreRegistries.DEFINITIONS.size(),
                CoreRegistries.MODIFIERS.size(), CoreRegistries.TRANSFERS.size());
    }

    /** 热重载：重读配置 + 重新加载 JSON 数据，保留 Java API 注册条目。 */
    public static synchronized void reload() {
        ThrexionConfig.load();
        clearJsonData();
        try {
            ensureDirectories();
        } catch (IOException e) {
            ThrexionCore.LOGGER.error("[threxioncore] Failed to ensure config directories: {}", e.getMessage());
        }
        loadPropertySchemas();
        loadDefinitionLoaders();
        loadDefinitions();
        loadModifiers();
        loadTransfers();
        detectMods();
        loadIntegrations();
        loaded = true;
        MinecraftForge.EVENT_BUS.post(new DefinitionReloadEvent(CoreRegistries.DEFINITIONS.values()));
        ThrexionCore.LOGGER.info("[threxioncore] Reload done: {} properties, {} definitions, {} modifiers, {} transfers",
                CoreRegistries.PROPERTIES.size(), CoreRegistries.DEFINITIONS.size(),
                CoreRegistries.MODIFIERS.size(), CoreRegistries.TRANSFERS.size());
    }

    private static void clearJsonData() {
        for (PropertyId id : JSON_SCHEMA_IDS) {
            CoreRegistries.PROPERTIES.remove(id);
        }
        JSON_SCHEMA_IDS.clear();
        for (PropertyId id : JSON_DEFINITION_IDS) {
            CoreRegistries.DEFINITIONS.remove(id);
        }
        JSON_DEFINITION_IDS.clear();
        for (PropertyId id : JSON_MODIFIER_IDS) {
            CoreRegistries.MODIFIERS.remove(id);
        }
        JSON_MODIFIER_IDS.clear();
        for (PropertyId id : JSON_TRANSFER_IDS) {
            CoreRegistries.TRANSFERS.remove(id);
        }
        JSON_TRANSFER_IDS.clear();
    }

    // ---------------------------------------------------------------- 目录

    private static void ensureDirectories() throws IOException {
        Path root = ThrexionConfig.rootPath();
        Files.createDirectories(root);
        for (String sub : ThrexionConstants.SUBDIRECTORIES) {
            Path dir = root.resolve(sub);
            if (Files.notExists(dir)) {
                Files.createDirectories(dir);
                writeExamples(sub, dir);
            }
        }
        // 下游 T Mod 数据目录骨架：仅建目录，不扫描其中的文件（TAllergen 等自行读写其子目录）
        for (String[] segments : ThrexionConstants.T_MOD_SUBDIRECTORIES) {
            Path dir = root;
            for (String segment : segments) {
                dir = dir.resolve(segment);
            }
            Files.createDirectories(dir);
        }
    }

    /** 首次创建子目录时写入示例文件，演示数据格式。 */
    private static void writeExamples(String sub, Path dir) throws IOException {
        switch (sub) {
            case "properties" -> Files.write(dir.resolve("example_property.json"),
                    exampleProperty().getBytes(StandardCharsets.UTF_8));
            case "definitions" -> Files.write(dir.resolve("wheat.json"),
                    exampleDefinition().getBytes(StandardCharsets.UTF_8));
            case "modifiers" -> Files.write(dir.resolve("example_modifier.json"),
                    exampleModifier().getBytes(StandardCharsets.UTF_8));
            case "transfers" -> Files.write(dir.resolve("example_transfer.json"),
                    exampleTransfer().getBytes(StandardCharsets.UTF_8));
            case "integrations" -> Files.write(dir.resolve("example_integration.json"),
                    exampleIntegration().getBytes(StandardCharsets.UTF_8));
            default -> {
            }
        }
    }

    private static String exampleProperty() {
        return """
                {
                  "//": "Property Schema：真正声明一个属性的类型/默认值/范围/持久化",
                  "id": "example:property",
                  "type": "number",
                  "description": "示例数值属性",
                  "default": 1.0,
                  "min": 0.0,
                  "max": 100.0,
                  "modifiable": true,
                  "persistent": true,
                  "hidden": false
                }
                """;
    }

    private static String exampleDefinition() {
        return """
                {
                  "//": "Definition：命名对象 + 一组属性（type 决定由哪个加载器解析）",
                  "id": "example:wheat",
                  "type": "threxioncore:definition",
                  "properties": {
                    "example:property": 1.0
                  }
                }
                """;
    }

    private static String exampleModifier() {
        return """
                {
                  "//": "Modifier：对指定属性执行 set/add/multiply/random_add/random_multiply；condition 为 ConditionRegistry 注册的判定键（如 tallergen:is_allergic），留空表示无条件",
                  "id": "example:modifier",
                  "property": "example:property",
                  "operation": "add",
                  "amount": 1.0,
                  "priority": 5,
                  "source": "definition",
                  "condition": "",
                  "enabled": true
                }
                """;
    }

    private static String exampleTransfer() {
        return """
                {
                  "//": "Transfer：属性跨对象传递（copy/add/multiply/ignore）",
                  "id": "example:transfer",
                  "source": "example:source",
                  "target": "example:property",
                  "operation": "add",
                  "factor": 1.0,
                  "enabled": true
                }
                """;
    }

    private static String exampleIntegration() {
        return """
                {
                  "//": "Integration：可选联动声明（默认 required=false）",
                  "id": "example:core_demo",
                  "enabled": true,
                  "required": false
                }
                """;
    }

    // ------------------------------------------------------------ schema

    private static void loadPropertySchemas() {
        Path dir = rootDir("properties");
        int count = 0;
        for (Path file : listJsonFiles(dir)) {
            try {
                PropertySchema schema = parsePropertySchema(file);
                if (CoreRegistries.PROPERTIES.register(schema.getId(), schema)) {
                    JSON_SCHEMA_IDS.add(schema.getId());
                    count++;
                    verboseLog("Loaded property schema {}", file);
                } else {
                    ThrexionCore.LOGGER.warn("[threxioncore] Duplicate property schema '{}' from {} (skipped)", schema.getId(), file);
                }
            } catch (Exception e) {
                ThrexionCore.LOGGER.error("[threxioncore] Failed to load property schema from {}: {}", file, e.getMessage());
            }
        }
        if (count > 0) {
            ThrexionCore.LOGGER.info("[threxioncore] Loaded {} property schema(s)", count);
        }
    }

    private static PropertySchema parsePropertySchema(Path file) throws IOException {
        JsonObject obj = JsoncLoader.parseObject(Files.readString(file));
        if (obj == null) {
            throw new IllegalArgumentException("Invalid JSON (after comment stripping)");
        }
        if (!obj.has("id")) {
            throw new IllegalArgumentException("Missing field \"id\"");
        }
        String idStr = obj.get("id").getAsString();
        if (!PropertyId.isValid(idStr)) {
            throw new IllegalArgumentException("Invalid id \"" + idStr + "\"");
        }
        PropertyId id = PropertyId.parse(idStr);
        PropertyType type = obj.has("type") ? PropertyType.parse(obj.get("type").getAsString()) : PropertyType.NUMBER;
        JsonElement defaultElement = obj.get("default");
        double def = defaultElement != null && defaultElement.isJsonPrimitive() && defaultElement.getAsJsonPrimitive().isNumber()
                ? defaultElement.getAsDouble() : 0.0;
        double min = obj.has("min") ? obj.get("min").getAsDouble() : Double.NEGATIVE_INFINITY;
        double max = obj.has("max") ? obj.get("max").getAsDouble() : Double.POSITIVE_INFINITY;
        boolean modifiable = !obj.has("modifiable") || obj.get("modifiable").getAsBoolean();
        boolean persistent = !obj.has("persistent") || obj.get("persistent").getAsBoolean();
        boolean hidden = obj.has("hidden") && obj.get("hidden").getAsBoolean();
        String description = obj.has("description") ? obj.get("description").getAsString() : "";

        PropertySchema.Builder builder = PropertySchema.builder(id).type(type);
        if (defaultElement != null && defaultElement.isJsonPrimitive()) {
            var primitive = defaultElement.getAsJsonPrimitive();
            if (primitive.isBoolean()) builder.defaultValue(primitive.getAsBoolean());
            else if (primitive.isString()) builder.defaultValue(primitive.getAsString());
            else if (primitive.isNumber()) builder.defaultValue(def);
        }
        return builder
                .range(min, max)
                .modifiable(modifiable)
                .persistent(persistent)
                .hidden(hidden)
                .description(description)
                .build();
    }

    // ---------------------------------------------------- definition loaders

    /** load_definition_loader：注册 Core 自带的 DefinitionLoader（threxioncore:definition）。
     *  各 T Mod 在自身初始化时通过 {@link DefinitionLoaderRegistry#register} 注册自己的加载器。 */
    private static void loadDefinitionLoaders() {
        DefinitionLoaderRegistry.register(
                PropertyId.of(ThrexionConstants.CORE_NAMESPACE, "definition"),
                new CoreDefinitionLoader(ThrexionConfig.get().isStrictValidation()));
    }

    // --------------------------------------------------------- definitions

    private static void loadDefinitions() {
        int count = 0;
        Set<Path> directories = new java.util.LinkedHashSet<>();
        // Core 自带 Definition 使用 definitions/。
        directories.add(rootDir("definitions"));
        // 各 T Mod 的 DefinitionLoader 可以声明自己的目录；Core 只负责发现和分发。
        for (DefinitionLoader<?> loader : DefinitionLoaderRegistry.entries().values()) {
            for (String relative : loader.getDirectories()) {
                if (relative == null || relative.isBlank()) {
                    continue;
                }
                Path normalized = Path.of(relative).normalize();
                if (normalized.isAbsolute() || normalized.startsWith("..")) {
                    ThrexionCore.LOGGER.warn("[threxioncore] Ignoring invalid definition directory '{}' from loader {}", relative, loader.getType());
                    continue;
                }
                directories.add(rootDir(relative));
            }
        }

        for (Path dir : directories) {
            for (Path file : listJsonFiles(dir)) {
                try {
                    JsonObject raw = JsoncLoader.parseObject(Files.readString(file));
                    if (raw == null) {
                        throw new IllegalArgumentException("Invalid JSON (after comment stripping)");
                    }
                    // 通用外壳校验（所有 type 统一）：id 必填/合法，且文件名 = id 的 path 部分。
                    requireDefinitionId(file, raw);
                    // 读取 type → 查 DefinitionLoaderRegistry → 找到加载器。
                    PropertyId type = definitionType(raw);
                    DefinitionLoader<?> loader = DefinitionLoaderRegistry.getOrNull(type);
                    if (loader == null) {
                        throw new IllegalArgumentException("No DefinitionLoader registered for type '" + type + "'");
                    }
                    // 防止一个 Loader 声明的目录被错误 Loader 共用：最终仍由 type 决定解析。
                    if (processDefinitionFile(file, raw, loader)) {
                        count++;
                        verboseLog("Loaded definition {} (type {})", file, type);
                    }
                } catch (Exception e) {
                    ThrexionCore.LOGGER.error("[threxioncore] Failed to load definition from {}: {}", file, e.getMessage());
                }
            }
        }
        if (count > 0) {
            ThrexionCore.LOGGER.info("[threxioncore] Loaded {} definition(s)", count);
        }
    }

    /**
     * 定义文件外壳校验（所有 type 统一执行）：id 必填、合法，且文件名（去扩展名）
     * 等于 id 的 path 部分。不符合时抛异常 → 该文件被跳过并记录明确错误。
     * 规则：wheat.json → 要求 id 的 path 为 "wheat"（如 tallergen:wheat）。
     */
    private static PropertyId requireDefinitionId(Path file, JsonObject obj) {
        if (!obj.has("id")) {
            throw new IllegalArgumentException("Missing field \"id\"");
        }
        JsonElement idEl = obj.get("id");
        if (!idEl.isJsonPrimitive() || !idEl.getAsJsonPrimitive().isString()) {
            throw new IllegalArgumentException("Field \"id\" must be a string");
        }
        String idStr = idEl.getAsString();
        if (!PropertyId.isValid(idStr)) {
            throw new IllegalArgumentException("Invalid id \"" + idStr + "\" (must be namespace:id, a-z0-9_. and a-z0-9._-)");
        }
        PropertyId id = PropertyId.parse(idStr);
        String stem = filenameStem(file);
        if (!stem.equals(id.getPath())) {
            throw new IllegalArgumentException("Filename \"" + file.getFileName()
                    + "\" does not match id path \"" + id.getPath()
                    + "\" (filename must equal the id path part, e.g. wheat.json → tallergen:wheat)");
        }
        return id;
    }

    /** 文件名去除 .json / .jsonc 扩展名。 */
    private static String filenameStem(Path file) {
        String name = file.getFileName().toString();
        if (name.endsWith(".json")) {
            return name.substring(0, name.length() - 5);
        }
        if (name.endsWith(".jsonc")) {
            return name.substring(0, name.length() - 6);
        }
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(0, dot) : name;
    }

    /** 用指定加载器解析、校验并注册一个定义文件；返回是否成功注册（非重复）。 */
    private static <T extends Definition> boolean processDefinitionFile(
            Path file, JsonObject raw, DefinitionLoader<T> loader) {
        T def = loader.load(file, raw);
        loader.validate(def);
        DefinitionLoadEvent event = new DefinitionLoadEvent(def, raw);
        MinecraftForge.EVENT_BUS.post(event);
        if (CoreRegistries.DEFINITIONS.register(def.getId(), def)) {
            JSON_DEFINITION_IDS.add(def.getId());
            return true;
        }
        ThrexionCore.LOGGER.warn("[threxioncore] Duplicate definition '{}' from {} (skipped)", def.getId(), file);
        return false;
    }

    /** 读取 definition 文件的 "type" 字段；JSON 中必须有 type，缺失即抛错跳过该文件。 */
    private static PropertyId definitionType(JsonObject obj) {
        if (!obj.has("type")) {
            throw new IllegalArgumentException("Missing field \"type\" (must be a DefinitionLoader type, e.g. \"threxioncore:definition\")");
        }
        JsonElement t = obj.get("type");
        if (!t.isJsonPrimitive() || !t.getAsJsonPrimitive().isString()) {
            throw new IllegalArgumentException("Field \"type\" must be a string");
        }
        String typeStr = t.getAsString();
        if (!PropertyId.isValid(typeStr)) {
            throw new IllegalArgumentException("Invalid type \"" + typeStr + "\" (must be namespace:id)");
        }
        return PropertyId.parse(typeStr);
    }

    // ----------------------------------------------------------- modifiers

    private static void loadModifiers() {
        Path dir = rootDir("modifiers");
        int count = 0;
        for (Path file : listJsonFiles(dir)) {
            try {
                Modifier mod = parseModifier(file);
                PropertyId modId = PropertyId.parse(mod.getId());
                if (CoreRegistries.MODIFIERS.register(modId, mod)) {
                    JSON_MODIFIER_IDS.add(modId);
                    count++;
                    verboseLog("Loaded modifier {}", file);
                } else {
                    ThrexionCore.LOGGER.warn("[threxioncore] Duplicate modifier '{}' from {} (skipped)", mod.getId(), file);
                }
            } catch (Exception e) {
                ThrexionCore.LOGGER.error("[threxioncore] Failed to load modifier from {}: {}", file, e.getMessage());
            }
        }
        if (count > 0) {
            ThrexionCore.LOGGER.info("[threxioncore] Loaded {} modifier(s)", count);
        }
    }

    private static Modifier parseModifier(Path file) throws IOException {
        JsonObject obj = JsoncLoader.parseObject(Files.readString(file));
        if (obj == null) {
            throw new IllegalArgumentException("Invalid JSON (after comment stripping)");
        }
        if (!obj.has("id")) {
            throw new IllegalArgumentException("Missing field \"id\"");
        }
        String id = obj.get("id").getAsString();
        if (!PropertyId.isValid(id)) {
            throw new IllegalArgumentException("Invalid modifier id \"" + id + "\" (must be namespace:id)");
        }
        if (!obj.has("property")) {
            throw new IllegalArgumentException("Missing field \"property\"");
        }
        String propStr = obj.get("property").getAsString();
        if (!PropertyId.isValid(propStr)) {
            throw new IllegalArgumentException("Invalid property \"" + propStr + "\"");
        }
        PropertyId property = PropertyId.parse(propStr);
        if (!obj.has("operation")) {
            throw new IllegalArgumentException("Missing field \"operation\"");
        }
        ModifierOperation operation = ModifierOperation.parse(obj.get("operation").getAsString());
        double amount = obj.has("amount") ? obj.get("amount").getAsDouble() : 0.0;
        double min = obj.has("min") ? obj.get("min").getAsDouble() : 0.0;
        double max = obj.has("max") ? obj.get("max").getAsDouble() : 0.0;
        int priority = obj.has("priority") ? obj.get("priority").getAsInt() : 0;
        ModifierSource source = obj.has("source") ? ModifierSource.parse(obj.get("source").getAsString()) : ModifierSource.UNKNOWN;
        boolean enabled = !obj.has("enabled") || obj.get("enabled").getAsBoolean();
        String condition = obj.has("condition") ? obj.get("condition").getAsString() : "";
        if (!condition.isBlank() && !PropertyId.isValid(condition)) {
            throw new IllegalArgumentException("Invalid condition \"" + condition + "\" (must be namespace:id, a ConditionRegistry key)");
        }
        Modifier.Builder builder = Modifier.builder(id, property, operation)
                .amount(amount).range(min, max)
                .priority(priority).source(source)
                .condition(condition).enabled(enabled);
        return builder.build();
    }

    // ----------------------------------------------------------- transfers

    private static void loadTransfers() {
        Path dir = rootDir("transfers");
        int count = 0;
        for (Path file : listJsonFiles(dir)) {
            try {
                Transfer transfer = parseTransfer(file);
                PropertyId transferId = PropertyId.parse(transfer.getId());
                if (CoreRegistries.TRANSFERS.register(transferId, transfer)) {
                    JSON_TRANSFER_IDS.add(transferId);
                    count++;
                    verboseLog("Loaded transfer {}", file);
                } else {
                    ThrexionCore.LOGGER.warn("[threxioncore] Duplicate transfer '{}' from {} (skipped)", transfer.getId(), file);
                }
            } catch (Exception e) {
                ThrexionCore.LOGGER.error("[threxioncore] Failed to load transfer from {}: {}", file, e.getMessage());
            }
        }
        if (count > 0) {
            ThrexionCore.LOGGER.info("[threxioncore] Loaded {} transfer(s)", count);
        }
    }

    private static Transfer parseTransfer(Path file) throws IOException {
        JsonObject obj = JsoncLoader.parseObject(Files.readString(file));
        if (obj == null) {
            throw new IllegalArgumentException("Invalid JSON (after comment stripping)");
        }
        if (!obj.has("id")) {
            throw new IllegalArgumentException("Missing field \"id\"");
        }
        String id = obj.get("id").getAsString();
        if (!PropertyId.isValid(id)) {
            throw new IllegalArgumentException("Invalid transfer id \"" + id + "\" (must be namespace:id)");
        }
        if (!obj.has("source")) {
            throw new IllegalArgumentException("Missing field \"source\"");
        }
        String srcStr = obj.get("source").getAsString();
        if (!PropertyId.isValid(srcStr)) {
            throw new IllegalArgumentException("Invalid source \"" + srcStr + "\"");
        }
        if (!obj.has("target")) {
            throw new IllegalArgumentException("Missing field \"target\"");
        }
        String tgtStr = obj.get("target").getAsString();
        if (!PropertyId.isValid(tgtStr)) {
            throw new IllegalArgumentException("Invalid target \"" + tgtStr + "\"");
        }
        if (!obj.has("operation")) {
            throw new IllegalArgumentException("Missing field \"operation\"");
        }
        TransferOperation operation = TransferOperation.parse(obj.get("operation").getAsString());
        double factor = obj.has("factor") ? obj.get("factor").getAsDouble() : 1.0;
        boolean enabled = !obj.has("enabled") || obj.get("enabled").getAsBoolean();
        return Transfer.builder(id, PropertyId.parse(srcStr), PropertyId.parse(tgtStr), operation)
                .factor(factor).enabled(enabled).build();
    }

    // ------------------------------------------------------- integrations

    private static void loadIntegrations() {
        Path dir = rootDir("integrations");
        for (Path file : listJsonFiles(dir)) {
            try {
                processIntegration(file);
            } catch (Exception e) {
                ThrexionCore.LOGGER.error("[threxioncore] Failed to load integration from {}: {}", file, e.getMessage());
            }
        }
    }

    private static void processIntegration(Path file) throws IOException {
        JsonObject obj = JsoncLoader.parseObject(Files.readString(file));
        if (obj == null) {
            throw new IllegalArgumentException("Invalid JSON (after comment stripping)");
        }
        if (!obj.has("id")) {
            throw new IllegalArgumentException("Missing field \"id\"");
        }
        String idStr = obj.get("id").getAsString();
        if (!PropertyId.isValid(idStr)) {
            throw new IllegalArgumentException("Invalid integration id \"" + idStr + "\"");
        }
        PropertyId id = PropertyId.parse(idStr);
        boolean enabled = !obj.has("enabled") || obj.get("enabled").getAsBoolean();
        boolean required = obj.has("required") && obj.get("required").getAsBoolean();
        Integration impl = CoreRegistries.INTEGRATIONS.getOrNull(id);
        if (impl == null) {
            String msg = "Integration '" + id + "' has no registered implementation";
            if (required) {
                ThrexionCore.LOGGER.error("[threxioncore] {} (required, but not provided)", msg);
            } else {
                ThrexionCore.LOGGER.info("[threxioncore] {} (optional, skipped)", msg);
            }
            return;
        }
        if (!enabled) {
            ThrexionCore.LOGGER.info("[threxioncore] Integration '{}' disabled in config", id);
            return;
        }
        if (!impl.isAvailable()) {
            String msg = "Integration '" + id + "' is not available";
            if (required || impl.isRequired()) {
                ThrexionCore.LOGGER.error("[threxioncore] {} (required)", msg);
            } else {
                ThrexionCore.LOGGER.warn("[threxioncore] {} (optional)", msg);
            }
            return;
        }
        try {
            impl.initialize();
        } catch (Exception e) {
            ThrexionCore.LOGGER.error("[threxioncore] Integration '{}' failed to initialize: {}", id, e.getMessage());
            return;
        }
        MinecraftForge.EVENT_BUS.post(new IntegrationLoadEvent(impl, obj, true, required));
        ThrexionCore.LOGGER.info("[threxioncore] Integration '{}' initialized", id);
    }

    // ----------------------------------------------------------- detect mod

    /** detect_mod：检测下游 / 未来联动 mod 是否加载。 */
    private static void detectMods() {
        DETECTED_MODS.clear();
        ModList modList = ModList.get();
        for (String modId : ThrexionConstants.DOWNSTREAM_MODS) {
            boolean present = modList != null && modList.isLoaded(modId);
            DETECTED_MODS.put(modId, present);
            ThrexionCore.LOGGER.info("[threxioncore] Mod detection: '{}' {}loaded", modId, present ? "" : "NOT ");
        }
        for (String modId : ThrexionConstants.FUTURE_MODS) {
            DETECTED_MODS.putIfAbsent(modId, modList != null && modList.isLoaded(modId));
        }
    }

    // -------------------------------------------------------------- helpers

    private static Path rootDir(String sub) {
        return ThrexionConfig.rootPath().resolve(sub);
    }

    private static java.util.List<Path> listJsonFiles(Path dir) {
        if (!Files.isDirectory(dir)) {
            return java.util.List.of();
        }
        try (Stream<Path> stream = Files.list(dir)) {
            return stream.filter(p -> {
                String name = p.getFileName().toString().toLowerCase(Locale.ROOT);
                return name.endsWith(".json") || name.endsWith(".jsonc");
            }).sorted().toList();
        } catch (IOException e) {
            ThrexionCore.LOGGER.error("[threxioncore] Failed to list {}: {}", dir, e.getMessage());
            return java.util.List.of();
        }
    }

    private static void verboseLog(String format, Object... args) {
        ThrexionConfig config = ThrexionConfig.get();
        if (config != null && config.isVerboseLogging()) {
            ThrexionCore.LOGGER.info("[threxioncore] " + format, args);
        }
    }
}
