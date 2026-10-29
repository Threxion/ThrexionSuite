package Threxion.threxioncore.api.property;

import Threxion.threxioncore.api.PropertyId;

/**
 * 属性 Schema：描述一个属性的类型、默认值、取值范围、是否可被 Modifier 修改，以及其持久化约束。
 * 可由各 Mod 通过 Java API 注册，也可由 JSON 文件（properties/ 目录）注册。
 */
public final class PropertySchema {

    private final PropertyId id;
    private final PropertyType type;
    private final PropertyValue defaultPropertyValue;
    private final double min;
    private final double max;
    private final boolean modifiable;
    private final boolean hidden;
    private final boolean persistent;
    private final String description;

    private PropertySchema(PropertyId id, PropertyType type, PropertyValue defaultPropertyValue,
                           double min, double max, boolean modifiable, boolean hidden,
                           boolean persistent, String description) {
        this.id = id;
        this.type = type;
        this.defaultPropertyValue = defaultPropertyValue == null ? PropertyValue.ofNumber(0.0) : defaultPropertyValue;
        this.min = min;
        this.max = max;
        this.modifiable = modifiable;
        this.hidden = hidden;
        this.persistent = persistent;
        this.description = description == null ? "" : description;
    }

    public static Builder builder(PropertyId id) {
        return new Builder(id);
    }

    public PropertyId getId() {
        return id;
    }

    public PropertyType getType() {
        return type;
    }

    /** 兼容旧 API：数值型默认值；非数值型返回 0。 */
    public double getDefaultValue() {
        return defaultPropertyValue.isNumeric() ? defaultPropertyValue.asDouble() : 0.0;
    }

    /** 类型安全的默认 PropertyValue。 */
    public PropertyValue getDefaultPropertyValue() {
        return defaultPropertyValue;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public boolean isModifiable() {
        return modifiable;
    }

    public boolean isHidden() {
        return hidden;
    }

    /** 是否持久化：persistent=true 的属性需要保存到持久化载体（如 ItemStack NBT），
     * 否则只存在于计算上下文（如 RecipeContext / AlchemyContext）。默认 true。 */
    public boolean isPersistent() {
        return persistent;
    }

    public String getDescription() {
        return description;
    }

    public boolean isWithinRange(double value) {
        return value >= min && value <= max;
    }

    /** 仅数值型属性；若类型不匹配返回 false。 */
    public boolean accepts(PropertyValue value) {
        if (value == null) {
            return false;
        }
        if (value.getType() == type) {
            return true;
        }
        return type == PropertyType.NUMBER && value.getType() == PropertyType.INT;
    }

    public static final class Builder {
        private final PropertyId id;
        private PropertyType type = PropertyType.NUMBER;
        private PropertyValue defaultPropertyValue = PropertyValue.ofNumber(0.0);
        private double min = Double.NEGATIVE_INFINITY;
        private double max = Double.POSITIVE_INFINITY;
        private boolean modifiable = true;
        private boolean hidden = false;
        private boolean persistent = true;
        private String description = "";

        private Builder(PropertyId id) {
            this.id = id;
        }

        public Builder type(PropertyType type) {
            this.type = type == null ? PropertyType.NUMBER : type;
            return this;
        }

        public Builder defaultValue(double defaultValue) {
            this.defaultPropertyValue = PropertyValue.ofNumber(defaultValue);
            return this;
        }

        public Builder defaultValue(long defaultValue) {
            this.defaultPropertyValue = PropertyValue.ofInt(defaultValue);
            return this;
        }

        public Builder defaultValue(boolean defaultValue) {
            this.defaultPropertyValue = PropertyValue.ofBoolean(defaultValue);
            return this;
        }

        public Builder defaultValue(String defaultValue) {
            this.defaultPropertyValue = PropertyValue.ofString(defaultValue == null ? "" : defaultValue);
            return this;
        }

        public Builder defaultPropertyValue(PropertyValue value) {
            this.defaultPropertyValue = value == null ? PropertyValue.ofNumber(0.0) : value;
            return this;
        }

        public Builder range(double min, double max) {
            this.min = min;
            this.max = max;
            return this;
        }

        public Builder min(double min) {
            this.min = min;
            return this;
        }

        public Builder max(double max) {
            this.max = max;
            return this;
        }

        public Builder modifiable(boolean modifiable) {
            this.modifiable = modifiable;
            return this;
        }

        public Builder hidden(boolean hidden) {
            this.hidden = hidden;
            return this;
        }

        public Builder persistent(boolean persistent) {
            this.persistent = persistent;
            return this;
        }

        public Builder description(String description) {
            this.description = description == null ? "" : description;
            return this;
        }

        public PropertySchema build() {
            return new PropertySchema(id, type, defaultPropertyValue, min, max, modifiable, hidden, persistent, description);
        }
    }
}
