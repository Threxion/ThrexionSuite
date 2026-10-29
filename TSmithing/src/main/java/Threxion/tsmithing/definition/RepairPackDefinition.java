package Threxion.tsmithing.definition;

import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.data.Definition;

/** JSON-defined repair material. It can repair tools/weapons and armor. */
public record RepairPackDefinition(
        PropertyId id,
        String displayName,
        String item,
        double repairAmount,
        double repairCostMultiplier,
        boolean overSpecArmor) implements Definition {
    @Override public PropertyId getId() { return id; }
}
