package Threxion.threxioncore.api.event;

import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.modifier.Modifier;
import Threxion.threxioncore.api.property.PropertyContainer;
import net.minecraftforge.eventbus.api.Cancelable;

/**
 * 应用单个 Modifier 时触发（PropertyContainer.modify 的 fire_event 阶段）。
 * 权限：read / modify / cancel。
 * 监听器可读取 base 值与 Modifier，用 setComputedValue 修改最终结果，
 * 或用 setCanceled(true) 取消本次修改（结果不写回）。
 *
 * <p>注意：Forge 的 Event 基类已占用 getResult()/setResult(Event.Result)，
 * 故本事件使用 getComputedValue()/setComputedValue(double)。</p>
 */
@Cancelable
public class PropertyModifyEvent extends ThrexionEvent {

    private final PropertyContainer container;
    private final PropertyId property;
    private final Modifier modifier;
    private final double base;
    private double result;

    public PropertyModifyEvent(PropertyContainer container, PropertyId property,
                               Modifier modifier, double base, double result) {
        super("property_modify");
        this.container = container;
        this.property = property;
        this.modifier = modifier;
        this.base = base;
        this.result = result;
    }

    public PropertyContainer getContainer() {
        return container;
    }

    public PropertyId getProperty() {
        return property;
    }

    public Modifier getModifier() {
        return modifier;
    }

    public double getBase() {
        return base;
    }

    public double getComputedValue() {
        return result;
    }

    public void setComputedValue(double result) {
        this.result = result;
    }
}
