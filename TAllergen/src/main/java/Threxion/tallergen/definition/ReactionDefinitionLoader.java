package Threxion.tallergen.definition;

import Threxion.tallergen.config.ReactionConfig;
import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.data.DefinitionLoader;
import com.google.gson.JsonObject;
import java.nio.file.Path;
import java.util.Set;

public final class ReactionDefinitionLoader implements DefinitionLoader<ReactionDefinition> {
    @Override public PropertyId getType(){return PropertyId.parse("tallergen:reaction");}
    @Override public Set<String> getDirectories(){return Set.of("tallergen/reactions");}
    @Override public ReactionDefinition load(Path file, JsonObject obj){return new ReactionDefinition(ReactionConfig.fromJson(file.getFileName().toString(),obj));}
    @Override public void validate(ReactionDefinition d){if(d.config().allergenId.isBlank()) throw new IllegalArgumentException("Reaction allergen_id cannot be blank: "+d.getId());}
}
