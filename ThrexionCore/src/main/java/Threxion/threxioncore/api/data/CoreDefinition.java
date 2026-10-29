package Threxion.threxioncore.api.data;

import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.modifier.Modifier;
import Threxion.threxioncore.api.modifier.ModifierPipeline;
import Threxion.threxioncore.api.property.DefaultPropertyContainer;
import Threxion.threxioncore.api.property.PropertyContainer;
import Threxion.threxioncore.api.property.PropertyValue;

import java.util.Collection;
import java.util.Objects;

/**
 * Core 自带的 Definition 默认实现：一个命名对象 + 一组 Property。
 * 格式（规格 definition_system）：
 * {"id": "example:wheat", "type": "threxioncore:definition", "properties": {"example:property": 1.0}}。
 *
 * <p>properties 是可变的 DefaultPropertyContainer，因此事件监听器可以在
 * DefinitionLoadEvent 中增删属性（read/modify 权限）。</p>
 *
 * <p>下游 T Mod 可定义自己的 Definition 实现（实现 {@link Definition} 接口），
 * Core 不解析其自有字段。</p>
 */
public final class CoreDefinition implements Definition {

    private final PropertyId id;
    private final DefaultPropertyContainer properties;

    private CoreDefinition(PropertyId id, DefaultPropertyContainer properties) {
        this.id = Objects.requireNonNull(id, "id");
        this.properties = properties == null ? new DefaultPropertyContainer() : properties;
    }

    public static Builder builder(PropertyId id) {
        return new Builder(id);
    }

    @Override
    public boolean applyModifiers(PropertyId property, Collection<Modifier> modifiers) {
        return ModifierPipeline.apply(this, property, modifiers);
    }

    @Override
    public PropertyId getId() {
        return id;
    }

    /** 返回可变属性容器（DefaultPropertyContainer），支持 modify / asMap 等扩展操作。 */
    @Override
    public DefaultPropertyContainer getProperties() {
        return properties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CoreDefinition other)) {
            return false;
        }
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "CoreDefinition[" + id + " props=" + properties.size() + "]";
    }

    public static final class Builder {
        private final PropertyId id;
        private final DefaultPropertyContainer properties = new DefaultPropertyContainer();

        private Builder(PropertyId id) {
            this.id = Objects.requireNonNull(id, "id");
        }

        public Builder property(PropertyId propertyId, PropertyValue value) {
            if (propertyId != null && value != null) {
                this.properties.set(propertyId, value);
            }
            return this;
        }

        public Builder properties(PropertyContainer container) {
            if (container != null) {
                if (container instanceof DefaultPropertyContainer dpc) {
                    dpc.asMap().forEach(this.properties::set);
                } else {
                    for (PropertyId pid : container.getPropertyIds()) {
                        container.get(pid).ifPresent(v -> this.properties.set(pid, v));
                    }
                }
            }
            return this;
        }

        public CoreDefinition build() {
            return new CoreDefinition(id, properties);
        }
    }
}
