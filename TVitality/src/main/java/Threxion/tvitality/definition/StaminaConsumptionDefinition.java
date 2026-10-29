package Threxion.tvitality.definition;

import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.data.Definition;

public final class StaminaConsumptionDefinition implements Definition {
    private final PropertyId id;
    private final String action;
    private final boolean enabled;
    private final int amount;

    public StaminaConsumptionDefinition(String id, String action, boolean enabled, int amount) {
        this.id = PropertyId.parse(id);
        this.action = action;
        this.enabled = enabled;
        this.amount = amount;
    }

    @Override public PropertyId getId() { return id; }
    public String action() { return action; }
    public boolean enabled() { return enabled; }
    public int amount() { return amount; }
}
