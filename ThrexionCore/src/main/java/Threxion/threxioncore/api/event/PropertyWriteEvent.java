package Threxion.threxioncore.api.event;

import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.property.PropertyContainer;
import Threxion.threxioncore.api.property.PropertyValue;

/**
 * 写入属性时触发（PropertyContainer.set / modify 写回阶段）。
 * 权限：read —— 仅通知，不可拦截写入。
 */
public class PropertyWriteEvent extends ThrexionEvent {

    private final PropertyContainer container;
    private final PropertyId property;
    private final PropertyValue value;

    public PropertyWriteEvent(PropertyContainer container, PropertyId property, PropertyValue value) {
        super("property_write");
        this.container = container;
        this.property = property;
        this.value = value;
    }

    public PropertyContainer getContainer() {
        return container;
    }

    public PropertyId getProperty() {
        return property;
    }

    public PropertyValue getValue() {
        return value;
    }
}
