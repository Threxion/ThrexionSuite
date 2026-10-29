package Threxion.threxioncore.api.property;

import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.event.PropertyModifyEvent;
import Threxion.threxioncore.api.event.PropertyReadEvent;
import Threxion.threxioncore.api.event.PropertyWriteEvent;
import Threxion.threxioncore.api.modifier.Modifier;
import net.minecraftforge.common.MinecraftForge;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 内存型属性容器（默认实现）：持有 PropertyId → PropertyValue 的映射，
 * 提供 get / set / has / remove / modify 等操作（规格 property_container_api）。
 *
 * <p>每次 get / set / modify 都会在 Forge 事件总线上发出对应事件
 * （PropertyReadEvent / PropertyWriteEvent / PropertyModifyEvent），
 * 供其他 Mod 读取、修改或取消。modify 遵循规格中的
 * property_modify_flow：read_property → prepare_modifier → fire_event →
 * execute_modifier → write_result。</p>
 */
public class DefaultPropertyContainer implements MutablePropertyContainer {

    private final Map<PropertyId, PropertyValue> values = new LinkedHashMap<>();

    /** 读取属性，触发 PropertyReadEvent（只读权限）。 */
    @Override
    public Optional<PropertyValue> get(PropertyId id) {
        if (id == null) {
            return Optional.empty();
        }
        MinecraftForge.EVENT_BUS.post(new PropertyReadEvent(this, id));
        return Optional.ofNullable(values.get(id));
    }

    @Override
    public PropertyValue getOrDefault(PropertyId id) {
        return get(id).orElse(PropertyValue.ofNumber(0.0));
    }

    public PropertyValue getOr(PropertyId id, PropertyValue defaultValue) {
        Optional<PropertyValue> v = get(id);
        return v.orElse(defaultValue == null ? PropertyValue.ofNumber(0.0) : defaultValue);
    }

    public double getNumber(PropertyId id, double defaultValue) {
        Optional<PropertyValue> v = get(id);
        return v.isPresent() && v.get().isNumeric() ? v.get().asDouble() : defaultValue;
    }

    /** 写入属性，触发 PropertyWriteEvent（只读权限，事件仅作通知）。
     * Numeric zero means "property absent"; zero is never persisted as a property. */
    @Override
    public void set(PropertyId id, PropertyValue value) {
        if (id == null || value == null) {
            return;
        }
        if (value.isNumeric() && value.asDouble() == 0.0) {
            values.remove(id);
            return;
        }
        MinecraftForge.EVENT_BUS.post(new PropertyWriteEvent(this, id, value));
        values.put(id, value);
    }

    @Override
    public boolean has(PropertyId id) {
        return id != null && values.containsKey(id);
    }

    @Override
    public void remove(PropertyId id) {
        if (id != null) {
            values.remove(id);
        }
    }

    /**
     * 对单个属性应用一个 Modifier。
     * 流程：read_property → prepare_modifier → fire_event → execute_modifier → write_result。
     *
     * @return 是否成功修改（属性不存在 / 非数值 / 事件被取消则返回 false）
     */
    public boolean modify(PropertyId id, Modifier modifier) {
        PropertyValue current = values.get(id);
        if (current == null || !current.isNumeric() || modifier == null || !modifier.isEnabled()) {
            return false;
        }
        double base = current.asDouble();
        double result = modifier.compute(base);
        PropertyModifyEvent event = new PropertyModifyEvent(this, id, modifier, base, result);
        if (MinecraftForge.EVENT_BUS.post(event)) {
            return false;
        }
        PropertyValue written = current.withNumber(event.getComputedValue());
        MinecraftForge.EVENT_BUS.post(new PropertyWriteEvent(this, id, written));
        values.put(id, written);
        return true;
    }

    @Override
    public Set<PropertyId> getPropertyIds() {
        return Set.copyOf(values.keySet());
    }

    public Map<PropertyId, PropertyValue> asMap() {
        return Collections.unmodifiableMap(values);
    }

    public int size() {
        return values.size();
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }
}
