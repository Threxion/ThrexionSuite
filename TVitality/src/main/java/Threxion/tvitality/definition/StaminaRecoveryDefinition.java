package Threxion.tvitality.definition;

import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.data.Definition;

public final class StaminaRecoveryDefinition implements Definition {
    private final PropertyId id;
    private final String mode;
    private final boolean enabled;
    private final long intervalSeconds;
    private final int amount;

    public StaminaRecoveryDefinition(String id, String mode, boolean enabled, long intervalSeconds, int amount) {
        this.id = PropertyId.parse(id);
        this.mode = mode;
        this.enabled = enabled;
        this.intervalSeconds = intervalSeconds;
        this.amount = amount;
    }

    @Override public PropertyId getId() { return id; }
    public String mode() { return mode; }
    public boolean enabled() { return enabled; }
    public long intervalSeconds() { return intervalSeconds; }
    public int amount() { return amount; }
}
