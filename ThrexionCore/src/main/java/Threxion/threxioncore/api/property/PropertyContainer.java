package Threxion.threxioncore.api.property;

import Threxion.threxioncore.api.PropertyId;

import java.util.Optional;
import java.util.Set;

/**
 * 属性容器：持有 PropertyId → PropertyValue 的映射（规格 property_container_api）。
 *
 * <p>这是逻辑接口，与具体存储介质解耦——NBT 只是其中一种持久化方式
 * （见 {@link ItemStackPropertyContainer} 与 {@link PropertyValueCodec}）。
 * ItemStack / Entity / BlockEntity / RecipeContext / AlchemyContext 等
 * 都可以实现同一套 Property API。</p>
 *
 * <p>在最小契约 {@link PropertyAccess}（get/set/has/remove）之上补充了默认值
 * 读取与属性遍历。读取与修改可分别按权限分发：只读方按 {@link PropertyContainer}
 * 使用，可写方按 {@link MutablePropertyContainer} 使用。</p>
 */
public interface PropertyContainer extends PropertyAccess {

    @Override
    boolean has(PropertyId id);

    @Override
    Optional<PropertyValue> get(PropertyId id);

    PropertyValue getOrDefault(PropertyId id);

    @Override
    void set(PropertyId id, PropertyValue value);

    @Override
    void remove(PropertyId id);

    Set<PropertyId> getPropertyIds();
}
