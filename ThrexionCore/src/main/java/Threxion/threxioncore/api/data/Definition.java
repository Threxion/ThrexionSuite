package Threxion.threxioncore.api.data;

import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.modifier.Modifier;
import Threxion.threxioncore.api.modifier.ModifierPipeline;
import Threxion.threxioncore.api.property.DefaultPropertyContainer;

import java.util.Collection;

/**
 * Definition 接口：一个命名对象（如 example:wheat / tallergen:wheat）。
 * Core 只约定公共标识 {@link #getId()}，具体字段由各实现自行定义——
 * 框架自带实现 {@link CoreDefinition}（携带一组 Property），下游 T Mod
 * （如 TAllergen 的 AllergenDefinition）实现本接口并自行解析自有字段，
 * Core 不解析它们的 Definition 格式（见 {@link DefinitionLoaderRegistry}）。
 *
 * <p>{@link #getProperties()} / {@link #applyModifiers(...)} 提供默认行为：
 * 携带属性的实现（如 {@link CoreDefinition}）应覆盖 {@link #getProperties()}
 * 返回自身容器；无属性语义的实现沿用默认空容器即可。</p>
 */
public interface Definition {

    /** 该 Definition 的全局标识（namespace:id）。 */
    PropertyId getId();

    /**
     * 返回该 Definition 的属性容器。默认返回空容器；
     * 携带属性的实现（如 {@link CoreDefinition}）应覆盖此方法并返回自身持有的容器。
     */
    default DefaultPropertyContainer getProperties() {
        return new DefaultPropertyContainer();
    }

    /** 对某个属性应用一组 Modifier（含优先级/阶段排序与事件）。 */
    default boolean applyModifiers(PropertyId property, Collection<Modifier> modifiers) {
        return ModifierPipeline.apply(this, property, modifiers);
    }
}
