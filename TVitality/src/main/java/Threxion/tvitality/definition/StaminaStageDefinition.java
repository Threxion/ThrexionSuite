package Threxion.tvitality.definition;

import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.data.Definition;

public final class StaminaStageDefinition implements Definition {
    private final PropertyId id;
    private final String displayName;
    private final int min;
    private final int max;
    private final double movementSpeedMultiplier;

    public StaminaStageDefinition(String id, String displayName, int min, int max, double movementSpeedMultiplier) {
        this.id = PropertyId.parse(id);
        this.displayName = displayName;
        this.min = min;
        this.max = max;
        this.movementSpeedMultiplier = movementSpeedMultiplier;
    }

    @Override public PropertyId getId() { return id; }
    public String displayName() { return displayName; }
    public int min() { return min; }
    public int max() { return max; }
    public double movementSpeedMultiplier() { return movementSpeedMultiplier; }
    public boolean contains(int stamina) { return stamina >= min && stamina <= max; }
}
