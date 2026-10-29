package Threxion.threxioncore.api.modifier;

import java.util.Locale;

/**
 * Modifier 来源：标记一个数值变化来自哪里（规格 modifier.ModifierSource）。
 * 用于 Debug 链路追踪（source 字段）。
 */
public enum ModifierSource {
    /** 由代码/API 注册的 Modifier。 */
    API,
    /** 由 JSON 文件（modifiers/ 目录）加载的 Modifier。 */
    DEFINITION,
    /** 由 Transfer 引起的数值变化（Transfer 负责传递，Modifier 负责变化）。 */
    TRANSFER,
    /** 由 Integration 初始化时注册的 Modifier。 */
    INTEGRATION,
    /** 由事件监听器添加的 Modifier。 */
    EVENT,
    /** 由命令或调试工具注入的 Modifier。 */
    COMMAND,
    /** 未指定来源。 */
    UNKNOWN;

    public static ModifierSource parse(String s) {
        if (s == null) {
            return UNKNOWN;
        }
        return switch (s.toLowerCase(Locale.ROOT)) {
            case "api" -> API;
            case "definition", "json" -> DEFINITION;
            case "transfer" -> TRANSFER;
            case "integration" -> INTEGRATION;
            case "event" -> EVENT;
            case "command" -> COMMAND;
            default -> UNKNOWN;
        };
    }
}
