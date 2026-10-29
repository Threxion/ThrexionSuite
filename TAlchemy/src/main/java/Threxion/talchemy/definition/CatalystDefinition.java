package Threxion.talchemy.definition;

import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.data.Definition;
import Threxion.threxioncore.api.modifier.ModifierOperation;

import java.util.List;

public record CatalystDefinition(
        PropertyId id,
        String item,
        boolean consumed,
        List<Modifier> modifiers
) implements Definition {
    @Override public PropertyId getId() { return id; }

    public record Modifier(PropertyId property, ModifierOperation operation,
                           double amount, double min, double max) {}
}
