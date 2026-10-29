package Threxion.threxioncore.api.property;

import Threxion.threxioncore.api.PropertyId;

/**
 * 可写属性容器：在只读 {@link PropertyContainer} 基础上重申
 * set / remove 两个修改操作。把读取与修改分成两个接口，
 * 便于以后按权限分发（只读方拿 {@link PropertyContainer}，可写方拿本接口）。
 */
public interface MutablePropertyContainer extends PropertyContainer {

    @Override
    void set(PropertyId id, PropertyValue value);

    @Override
    void remove(PropertyId id);
}
