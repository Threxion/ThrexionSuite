package Threxion.tallergen.definition;

import Threxion.tallergen.config.ReactionConfig;
import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.data.Definition;
import java.util.Objects;

public final class ReactionDefinition implements Definition {
    private final PropertyId id;
    private final ReactionConfig config;
    public ReactionDefinition(ReactionConfig config) {
        this.config=Objects.requireNonNull(config,"config");
        this.id=PropertyId.parse("tallergen:" + config.id);
    }
    @Override public PropertyId getId(){return id;}
    public ReactionConfig config(){return config;}
}
