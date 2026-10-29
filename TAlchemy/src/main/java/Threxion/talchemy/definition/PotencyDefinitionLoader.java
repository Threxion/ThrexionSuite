package Threxion.talchemy.definition;

import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.data.DefinitionLoader;
import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;

import java.nio.file.Path;
import java.util.Set;

/** Loads potency channel metadata. The numeric max remains in Core PropertySchema. */
public final class PotencyDefinitionLoader implements DefinitionLoader<PotencyDefinition> {
    @Override public PropertyId getType() { return PropertyId.parse("talchemy:potency"); }
    @Override public Set<String> getDirectories() { return Set.of("talchemy/potencies"); }

    @Override public PotencyDefinition load(Path file, JsonObject obj) {
        return new PotencyDefinition(
                PropertyId.parse(GsonHelper.getAsString(obj, "id")),
                GsonHelper.getAsString(obj, "display_name", ""),
                GsonHelper.getAsDouble(obj, "unit_value", 100.0));
    }

    @Override public void validate(PotencyDefinition d) {
        if (d.unitValue() <= 0) throw new IllegalArgumentException("unit_value must be > 0: " + d.getId());
    }
}
