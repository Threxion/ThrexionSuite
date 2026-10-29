package Threxion.tvitality.definition;

import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.data.Definition;

public final class StaminaSettingsDefinition implements Definition {
    private final PropertyId id;
    private final int max;
    private final int initial;

    public StaminaSettingsDefinition(String id, int max, int initial) {
        this.id = PropertyId.parse(id);
        this.max = max;
        this.initial = initial;
    }

    @Override public PropertyId getId() { return id; }
    public int max() { return max; }
    public int initial() { return initial; }
}
