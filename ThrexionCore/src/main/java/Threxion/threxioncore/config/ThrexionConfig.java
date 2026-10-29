package Threxion.threxioncore.config;

import Threxion.threxioncore.ThrexionConstants;
import Threxion.threxioncore.ThrexionCore;
import com.google.gson.JsonObject;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * ThrexionCore 核心配置（config/threxioncore/config.json，JSONC 格式）。
 *
 * <p>字段（规格 core_config_fields）：debug / verbose_logging / scan_enabled /
 * network_sync / strict_validation；以及 debug_config 子对象
 * （debug / trace_properties / trace_transfers）。debug 最终值取
 * debug_config.debug（若存在），否则取顶层 debug。缺失的字段回退到默认值。</p>
 */
public final class ThrexionConfig {

    private static volatile ThrexionConfig INSTANCE = new ThrexionConfig(
            ThrexionConstants.DEFAULT_DEBUG,
            ThrexionConstants.DEFAULT_VERBOSE_LOGGING,
            ThrexionConstants.DEFAULT_SCAN_ENABLED,
            ThrexionConstants.DEFAULT_NETWORK_SYNC,
            ThrexionConstants.DEFAULT_STRICT_VALIDATION,
            ThrexionConstants.DEFAULT_TRACE_PROPERTIES,
            ThrexionConstants.DEFAULT_TRACE_TRANSFERS
    );

    private final boolean debug;
    private final boolean verboseLogging;
    private final boolean scanEnabled;
    private final boolean networkSync;
    private final boolean strictValidation;
    private final boolean traceProperties;
    private final boolean traceTransfers;

    private ThrexionConfig(boolean debug, boolean verboseLogging, boolean scanEnabled,
                           boolean networkSync, boolean strictValidation,
                           boolean traceProperties, boolean traceTransfers) {
        this.debug = debug;
        this.verboseLogging = verboseLogging;
        this.scanEnabled = scanEnabled;
        this.networkSync = networkSync;
        this.strictValidation = strictValidation;
        this.traceProperties = traceProperties;
        this.traceTransfers = traceTransfers;
    }

    public static ThrexionConfig get() {
        return INSTANCE;
    }

    public static Path rootPath() {
        return FMLPaths.CONFIGDIR.get().resolve(ThrexionConstants.CONFIG_ROOT);
    }

    public static Path configPath() {
        return rootPath().resolve(ThrexionConstants.CONFIG_FILE);
    }

    /** 读取并解析配置文件；文件不存在时写入默认配置。失败时保留默认值并记录错误。 */
    public static void load() {
        try {
            ensureConfigFile();
            String content = Files.readString(configPath(), StandardCharsets.UTF_8);
            JsonObject obj = JsoncLoader.parseObject(content);
            if (obj == null) {
                ThrexionCore.LOGGER.warn("[threxioncore] Config {} is empty or invalid JSON, using defaults.", configPath());
                return;
            }
            boolean debug = getBool(obj, "debug", ThrexionConstants.DEFAULT_DEBUG);
            boolean verbose = getBool(obj, "verbose_logging", ThrexionConstants.DEFAULT_VERBOSE_LOGGING);
            boolean scan = getBool(obj, "scan_enabled", ThrexionConstants.DEFAULT_SCAN_ENABLED);
            boolean sync = getBool(obj, "network_sync", ThrexionConstants.DEFAULT_NETWORK_SYNC);
            boolean strict = getBool(obj, "strict_validation", ThrexionConstants.DEFAULT_STRICT_VALIDATION);
            boolean traceProps = ThrexionConstants.DEFAULT_TRACE_PROPERTIES;
            boolean traceTransfers = ThrexionConstants.DEFAULT_TRACE_TRANSFERS;
            if (obj.has("debug_config") && obj.get("debug_config").isJsonObject()) {
                JsonObject dc = obj.getAsJsonObject("debug_config");
                debug = getBool(dc, "debug", debug);
                traceProps = getBool(dc, "trace_properties", traceProps);
                traceTransfers = getBool(dc, "trace_transfers", traceTransfers);
            }
            INSTANCE = new ThrexionConfig(debug, verbose, scan, sync, strict, traceProps, traceTransfers);
            ThrexionCore.LOGGER.info("[threxioncore] Config loaded: debug={}, verbose={}, scan={}, sync={}, strict={}",
                    debug, verbose, scan, sync, strict);
        } catch (IOException e) {
            ThrexionCore.LOGGER.error("[threxioncore] Failed to load config {}: {}", configPath(), e.getMessage());
        }
    }

    private static void ensureConfigFile() throws IOException {
        Path root = rootPath();
        Files.createDirectories(root);
        if (!Files.exists(configPath())) {
            Files.write(configPath(), defaultConfigText().getBytes(StandardCharsets.UTF_8));
            ThrexionCore.LOGGER.info("[threxioncore] Created default config at {}", configPath());
        }
    }

    private static boolean getBool(JsonObject obj, String key, boolean fallback) {
        if (obj.has(key) && obj.get(key).isJsonPrimitive() && obj.get(key).getAsJsonPrimitive().isBoolean()) {
            return obj.get(key).getAsBoolean();
        }
        return fallback;
    }

    private static String defaultConfigText() {
        return """
                // ThrexionCore 核心配置（JSONC：支持 // 与 /* */ 注释）
                {
                  "//comment": "debug 控制 Debug API 与详细日志；verbose_logging 输出每个文件加载明细",
                  "debug": false,
                  "verbose_logging": false,
                  "scan_enabled": true,
                  "network_sync": true,
                  "strict_validation": false,
                  "debug_config": {
                    "//comment": "属性/Transfer 链路追踪开关",
                    "debug": false,
                    "trace_properties": false,
                    "trace_transfers": false
                  }
                }
                """;
    }

    public boolean isDebug() {
        return debug;
    }

    public boolean isVerboseLogging() {
        return verboseLogging;
    }

    public boolean isScanEnabled() {
        return scanEnabled;
    }

    public boolean isNetworkSync() {
        return networkSync;
    }

    public boolean isStrictValidation() {
        return strictValidation;
    }

    public boolean isTraceProperties() {
        return traceProperties;
    }

    public boolean isTraceTransfers() {
        return traceTransfers;
    }

    @Override
    public String toString() {
        return "ThrexionConfig[debug=" + debug + ", verbose=" + verboseLogging
                + ", scan=" + scanEnabled + ", sync=" + networkSync
                + ", strict=" + strictValidation
                + ", traceProps=" + traceProperties + ", traceTransfers=" + traceTransfers + "]";
    }
}
