package Threxion.talchemy.definition;

import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.data.Definition;
import Threxion.threxioncore.api.modifier.ModifierOperation;

import java.util.List;

/** 可配置的工艺等级。它不决定工艺名称，只决定稳定性、批量与额外属性。 */
public record ProcessTierDefinition(
        PropertyId id,
        String displayName,
        double failureChanceMultiplier,
        int batchCount,
        boolean recipeAllowed,
        List<PropertyModifier> modifiers
) implements Definition {
    @Override public PropertyId getId() { return id; }

    public record PropertyModifier(PropertyId property, ModifierOperation operation,
                                    double amount, double min, double max) {}
}
