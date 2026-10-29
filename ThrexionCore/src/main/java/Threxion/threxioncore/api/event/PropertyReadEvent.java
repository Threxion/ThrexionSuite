package Threxion.threxioncore.api.event;

import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.property.PropertyContainer;

/**
 * 读取属性时触发（PropertyContainer.get）。权限：read —— 仅通知，不可修改返回值。
 */
public class PropertyReadEvent extends ThrexionEvent {

    private final PropertyContainer container;
    private final PropertyId property;

    public PropertyReadEvent(PropertyContainer container, PropertyId property) {
        super("property_read");
        this.container = container;
        this.property = property;
    }

    public PropertyContainer getContainer() {
        return container;
    }

    public PropertyId getProperty() {
        return property;
    }
}
