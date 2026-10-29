package Threxion.tsmithing.definition;

import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.data.Definition;
import Threxion.threxioncore.api.property.PropertyValue;

import java.util.Map;

/** A material is a complete material definition; alloys are separate definitions. */
public record MaterialDefinition(PropertyId id, String displayName, Map<PropertyId, PropertyValue> properties) implements Definition {
    @Override public PropertyId getId() { return id; }
}
