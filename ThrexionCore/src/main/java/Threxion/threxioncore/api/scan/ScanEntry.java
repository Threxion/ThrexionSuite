package Threxion.threxioncore.api.scan;

import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.property.PropertyValue;

/**
 * 一条扫描结果条目：名称 + 值 + 来源（供 Debug 追踪）。
 */
public final class ScanEntry {

    private final String name;
    private final PropertyValue value;
    private final String source;

    public ScanEntry(String name, PropertyValue value, String source) {
        this.name = name;
        this.value = value;
        this.source = source == null ? "" : source;
    }

    public String getName() {
        return name;
    }

    public PropertyValue getValue() {
        return value;
    }

    public String getSource() {
        return source;
    }

    @Override
    public String toString() {
        return name + " = " + value + " (" + source + ")";
    }
}
