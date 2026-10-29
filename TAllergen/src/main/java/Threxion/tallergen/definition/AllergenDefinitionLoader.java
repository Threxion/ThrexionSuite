package Threxion.tallergen.definition;

import Threxion.tallergen.config.AllergenConfig;
import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.data.DefinitionLoader;
import com.google.gson.JsonObject;
import java.nio.file.Path;
import java.util.Set;

public final class AllergenDefinitionLoader implements DefinitionLoader<AllergenDefinition> {
    @Override public PropertyId getType() { return PropertyId.parse("tallergen:allergen"); }
    @Override public Set<String> getDirectories() { return Set.of("tallergen/allergens"); }
    @Override public AllergenDefinition load(Path file, JsonObject obj) {
        return new AllergenDefinition(AllergenConfig.fromJson(file.getFileName().toString(), obj));
    }
    @Override public void validate(AllergenDefinition definition) {
        if (definition.config().id.isBlank()) throw new IllegalArgumentException("Allergen id cannot be blank");
        if (definition.config().reactionId.isBlank()) throw new IllegalArgumentException("Allergen reaction_id cannot be blank: " + definition.getId());
        if (definition.config().severityStart < 1 || definition.config().severityStart > definition.config().severityMax)
            throw new IllegalArgumentException("Invalid severity range: " + definition.getId());
    }
}
