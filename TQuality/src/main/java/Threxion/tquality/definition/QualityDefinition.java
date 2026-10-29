package Threxion.tquality.definition;

import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.data.Definition;
import Threxion.threxioncore.api.property.PropertyValue;

import java.util.List;
import java.util.Map;

/**
 * A named quality scale. The grade is the persistent state; roll values are not stored.
 */
public record QualityDefinition(PropertyId id, String displayName, List<Grade> grades) implements Definition {
    public record Grade(String id, String displayName, int order, Map<PropertyId, PropertyValue> properties) {}

    @Override public PropertyId getId(){ return id; }
}
