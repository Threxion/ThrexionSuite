package Threxion.threxioncore;

import Threxion.threxioncore.api.modifier.ModifierOperation;

import java.util.List;

/**
 * ThrexionCore 框架的全局常量。
 * 依据规格：id_format=namespace:id，namespace 允许 a-z0-9_（小写），id 路径允许 a-z0-9._-（小写）。
 */
public final class ThrexionConstants {

    public static final String MODID = "threxioncore";

    /** 框架自身使用的命名空间（用于核心注册的扫描器等）。 */
    public static final String CORE_NAMESPACE = "threxioncore";

    /** 配置根目录（相对 minecraft 的 config/ 目录）。 */
    public static final String CONFIG_ROOT = "threxioncore";

    /** 核心配置文件。 */
    public static final String CONFIG_FILE = "config.json";

    /** 数据子目录。 */
    public static final String[] SUBDIRECTORIES = {
            "properties", "definitions", "modifiers", "transfers", "integrations"
    };

    /**
     * 下游 T Mod 数据目录骨架（相对根目录的多级路径）。
     * 仅建目录、写入不扫描其中的文件；数据由各 T Mod 通过
     * {@code ThrexionConfig.rootPath().resolve("tallergen")...} 自行读写。
     */
    public static final String[][] T_MOD_SUBDIRECTORIES = {
            {"tallergen", "allergens"},
            {"tallergen", "reactions"},
            {"talchemy", "recipes"},
            {"talchemy", "processes"},
            {"talchemy", "stations"},
            {"talchemy", "catalysts"},
            {"tquality", "grades"},
            {"tquality", "assignments"}
    };

    /** Modifier 执行顺序：set → add → multiply → random_add → random_multiply。 */
    public static final List<ModifierOperation> MODIFIER_EXECUTION_ORDER = List.of(
            ModifierOperation.SET,
            ModifierOperation.ADD,
            ModifierOperation.MULTIPLY,
            ModifierOperation.RANDOM_ADD,
            ModifierOperation.RANDOM_MULTIPLY
    );

    /** 命名空间字符规则：小写，a-z0-9_。 */
    public static final String NAMESPACE_PATTERN = "[a-z0-9_]+";

    /** ID 路径字符规则：小写，a-z0-9._-。 */
    public static final String PATH_PATTERN = "[a-z0-9._-]+";

    /** 核心配置默认字段。 */
    public static final boolean DEFAULT_DEBUG = false;
    public static final boolean DEFAULT_VERBOSE_LOGGING = false;
    public static final boolean DEFAULT_SCAN_ENABLED = true;
    public static final boolean DEFAULT_NETWORK_SYNC = true;
    public static final boolean DEFAULT_STRICT_VALIDATION = false;
    public static final boolean DEFAULT_TRACE_PROPERTIES = false;
    public static final boolean DEFAULT_TRACE_TRANSFERS = false;

    /** 下游/联动 mod，用于模组检测（detect_mod 阶段）。 */
    public static final List<String> DOWNSTREAM_MODS = List.of("tallergen", "talchemy", "tquality");
    public static final List<String> FUTURE_MODS = List.of("tweather", "tinjury", "tdisease", "tfarming", "tmagic");

    /** 网络协议版本。 */
    public static final String NETWORK_PROTOCOL_VERSION = "1";

    private ThrexionConstants() {
    }
}
