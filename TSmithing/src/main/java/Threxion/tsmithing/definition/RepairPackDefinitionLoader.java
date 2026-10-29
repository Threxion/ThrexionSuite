package Threxion.tsmithing.definition;

import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.data.DefinitionLoader;
import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;
import java.nio.file.Path;
import java.util.Set;

public final class RepairPackDefinitionLoader implements DefinitionLoader<RepairPackDefinition> {
    private static final PropertyId TYPE = PropertyId.parse("tsmithing:repair_pack");
    @Override public PropertyId getType() { return TYPE; }
    @Override public Set<String> getDirectories() { return Set.of("tsmithing/repair_packs"); }

    @Override public RepairPackDefinition load(Path file, JsonObject obj) {
        return new RepairPackDefinition(
                PropertyId.parse(GsonHelper.getAsString(obj, "id")),
                GsonHelper.getAsString(obj, "display_name", ""),
                GsonHelper.getAsString(obj, "item"),
                GsonHelper.getAsDouble(obj, "repair_amount", 0.0),
                GsonHelper.getAsDouble(obj, "repair_cost_multiplier", 1.0),
                GsonHelper.getAsBoolean(obj, "over_spec_armor", false));
    }

    @Override public void validate(RepairPackDefinition d) {
        if (d.displayName().isBlank()) throw new IllegalArgumentException("TSmithing repair pack has blank display_name: " + d.id());
        if (d.item().isBlank()) throw new IllegalArgumentException("TSmithing repair pack has blank item: " + d.id());
        if (d.repairAmount() <= 0) throw new IllegalArgumentException("TSmithing repair pack repair_amount must be > 0: " + d.id());
        if (d.repairCostMultiplier() <= 0) throw new IllegalArgumentException("TSmithing repair pack repair_cost_multiplier must be > 0: " + d.id());
    }
}
