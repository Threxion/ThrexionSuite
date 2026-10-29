package Threxion.talchemy.definition;

import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.data.DefinitionLoader;
import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;

import java.nio.file.Path;
import java.util.Set;

/** JSON loader for configurable potency reactions. */
public final class AlchemyReactionDefinitionLoader implements DefinitionLoader<AlchemyReactionDefinition> {
    @Override public PropertyId getType() { return PropertyId.parse("talchemy:reaction"); }
    @Override public Set<String> getDirectories() { return Set.of("talchemy/reactions"); }

    @Override
    public AlchemyReactionDefinition load(Path file, JsonObject obj) {
        return new AlchemyReactionDefinition(
                PropertyId.parse(GsonHelper.getAsString(obj, "id")),
                GsonHelper.getAsString(obj, "display_name", ""),
                PropertyId.parse(GsonHelper.getAsString(obj, "potency")),
                GsonHelper.getAsDouble(obj, "threshold", 0.0),
                PropertyId.parse(GsonHelper.getAsString(obj, "effect")),
                GsonHelper.getAsInt(obj, "priority", 0));
    }

    @Override
    public void validate(AlchemyReactionDefinition d) {
        if (d.threshold() < 0) throw new IllegalArgumentException("Reaction threshold must be >= 0: " + d.getId());
    }
}
