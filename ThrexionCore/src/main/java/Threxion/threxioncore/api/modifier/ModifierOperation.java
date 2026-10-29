package Threxion.threxioncore.api.modifier;

import Threxion.threxioncore.ThrexionConstants;

import java.util.Locale;

/**
 * Modifier 操作类型。
 * 执行顺序（规格）：set → add → multiply → random_add → random_multiply。
 * order() 用于排序，值越小越先执行。
 */
public enum ModifierOperation {

    SET(0),
    ADD(1),
    MULTIPLY(2),
    RANDOM_ADD(3),
    RANDOM_MULTIPLY(4);

    private final int order;

    ModifierOperation(int order) {
        this.order = order;
    }

    public int getOrder() {
        return order;
    }

    public int getExecutionIndex() {
        return ThrexionConstants.MODIFIER_EXECUTION_ORDER.indexOf(this);
    }

    public static ModifierOperation parse(String s) {
        if (s == null) {
            throw new IllegalArgumentException("Missing field \"operation\"");
        }
        return switch (s.toLowerCase(Locale.ROOT)) {
            case "set" -> SET;
            case "add" -> ADD;
            case "multiply", "mul" -> MULTIPLY;
            case "random_add", "randomadd" -> RANDOM_ADD;
            case "random_multiply", "randommultiply", "random_mul" -> RANDOM_MULTIPLY;
            default -> throw new IllegalArgumentException("Invalid operation \"" + s + "\"");
        };
    }
}
