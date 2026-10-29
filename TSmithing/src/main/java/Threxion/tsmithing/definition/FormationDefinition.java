package Threxion.tsmithing.definition;

import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.data.Definition;
import java.util.List;
import java.util.Map;

/**
 * A formation maps ordinary input items directly to a finished item. It does not
 * introduce intermediate blade/head/handle objects.
 */
public record FormationDefinition(
        PropertyId id,
        String displayName,
        String resultItem,
        List<MaterialInput> materials,
        Map<PropertyId, Double> tqeff) implements Definition {
    public record MaterialInput(String item, String material, int count) {}
    @Override public PropertyId getId() { return id; }
}
