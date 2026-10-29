package Threxion.threxioncore.api.property;

import Threxion.threxioncore.api.PropertyId;

import java.util.Optional;

/**
 * 最小属性访问契约（规格 property_access_api）：声明一个对象
 * "能读取 / 写入 / 查询 / 删除属性" 的能力，与具体存储介质解耦。
 *
 * <p>ItemStack（NBT）、Entity、AlchemyProcess、RecipeContext、临时计算对象
 * 都可以实现本接口。Core 只依赖本接口读写属性，不关心属性究竟存在 NBT、
 * 组件系统还是内存对象里——持久化载体差异由各实现自行处理。</p>
 *
 * <p>这是只读 / 可写共用的最小接口；带默认值与属性遍历、便于按权限分发的
 * 完整容器见 {@link PropertyContainer}（扩展了本接口）。</p>
 */
public interface PropertyAccess {

    Optional<PropertyValue> get(PropertyId id);

    void set(PropertyId id, PropertyValue value);

    boolean has(PropertyId id);

    void remove(PropertyId id);
}
