package Threxion.threxioncore.api.property;

import java.util.Locale;

/**
 * 属性值类型：number / int / boolean / string。
 */
public enum PropertyType {
    NUMBER,
    INT,
    BOOLEAN,
    STRING;

    public static PropertyType parse(String s) {
        if (s == null) {
            return NUMBER;
        }
        return switch (s.toLowerCase(Locale.ROOT)) {
            case "int", "integer", "long" -> INT;
            case "bool", "boolean" -> BOOLEAN;
            case "string", "str", "text" -> STRING;
            default -> NUMBER;
        };
    }
}
