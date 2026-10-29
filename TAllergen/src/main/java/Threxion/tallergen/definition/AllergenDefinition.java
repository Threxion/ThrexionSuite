package Threxion.tallergen.definition;

import Threxion.tallergen.config.AllergenConfig;
import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.data.Definition;
import java.util.Objects;

/** TAllergen 领域 Definition；Core 只负责注册与生命周期，不理解其业务字段。 */
public final class AllergenDefinition implements Definition {
    private final PropertyId id;
    private final AllergenConfig config;

    public AllergenDefinition(AllergenConfig config) {
        this.config = Objects.requireNonNull(config, "config");
        this.id = PropertyId.parse("tallergen:" + config.id);
    }

    @Override public PropertyId getId() { return id; }
    public AllergenConfig config() { return config; }
}
