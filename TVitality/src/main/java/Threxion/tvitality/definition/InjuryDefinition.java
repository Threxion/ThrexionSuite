package Threxion.tvitality.definition;

import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.data.Definition;

public final class InjuryDefinition implements Definition {
    private final PropertyId id;
    private final String displayName;
    private final int severity;
    private final boolean doctorTreatmentEnabled;
    private final String costItem;
    private final int costCount;

    public InjuryDefinition(String id, String displayName, int severity,
                            boolean doctorTreatmentEnabled, String costItem, int costCount) {
        this.id = PropertyId.parse(id);
        this.displayName = displayName;
        this.severity = severity;
        this.doctorTreatmentEnabled = doctorTreatmentEnabled;
        this.costItem = costItem;
        this.costCount = costCount;
    }

    @Override public PropertyId getId() { return id; }
    public String displayName() { return displayName; }
    public int severity() { return severity; }
    public boolean doctorTreatmentEnabled() { return doctorTreatmentEnabled; }
    public String costItem() { return costItem; }
    public int costCount() { return costCount; }
}
