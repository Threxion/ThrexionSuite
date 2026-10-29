package Threxion.tvitality.definition;

import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.data.Definition;

public final class DoctorCurrencyDefinition implements Definition {
    private final PropertyId id;
    private final String item;
    public DoctorCurrencyDefinition(String id, String item) { this.id = PropertyId.parse(id); this.item = item; }
    @Override public PropertyId getId() { return id; }
    public String item() { return item; }
}
