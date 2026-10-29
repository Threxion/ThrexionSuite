package Threxion.threxioncore.api.property;

import java.util.Objects;

/**
 * 属性值：带类型标签的不可变数值/布尔/字符串容器。
 */
public final class PropertyValue {

    private final PropertyType type;
    private final double number;
    private final boolean bool;
    private final String string;

    private PropertyValue(PropertyType type, double number, boolean bool, String string) {
        this.type = type;
        this.number = number;
        this.bool = bool;
        this.string = string;
    }

    public static PropertyValue ofNumber(double value) {
        return new PropertyValue(PropertyType.NUMBER, value, false, null);
    }

    public static PropertyValue ofInt(long value) {
        return new PropertyValue(PropertyType.INT, value, false, null);
    }

    public static PropertyValue ofBoolean(boolean value) {
        return new PropertyValue(PropertyType.BOOLEAN, 0.0, value, null);
    }

    public static PropertyValue ofString(String value) {
        Objects.requireNonNull(value, "value");
        return new PropertyValue(PropertyType.STRING, 0.0, false, value);
    }

    public PropertyType getType() {
        return type;
    }

    public boolean isNumeric() {
        return type == PropertyType.NUMBER || type == PropertyType.INT;
    }

    public double asDouble() {
        if (!isNumeric()) {
            throw new IllegalStateException("Property value of type " + type + " is not numeric");
        }
        return number;
    }

    public double getNumber() {
        return isNumeric() ? number : 0.0;
    }

    public long getInt() {
        if (type == PropertyType.INT) {
            return (long) number;
        }
        if (isNumeric()) {
            return Math.round(number);
        }
        return 0L;
    }

    public boolean getBoolean() {
        return type == PropertyType.BOOLEAN && bool;
    }

    public String getString() {
        return string == null ? "" : string;
    }

    /**
     * 应用数值修改后返回同类型的新值（INT 会四舍五入，STRING/BOOLEAN 原样返回）。
     */
    public PropertyValue withNumber(double newNumber) {
        return switch (type) {
            case INT -> ofInt(Math.round(newNumber));
            case NUMBER -> ofNumber(newNumber);
            default -> this;
        };
    }

    @Override
    public String toString() {
        return switch (type) {
            case NUMBER, INT -> Double.toString(number);
            case BOOLEAN -> Boolean.toString(bool);
            case STRING -> string;
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PropertyValue other)) {
            return false;
        }
        return type == other.type
                && Double.compare(number, other.number) == 0
                && bool == other.bool
                && Objects.equals(string, other.string);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, number, bool, string);
    }
}
