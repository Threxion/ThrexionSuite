package Threxion.tdiscovery.definition;

import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.data.Definition;

public record DiscoveryEntryDefinition(PropertyId getId, String displayName, String category, String description) implements Definition {
    public String key() { return getId().getString(); }
}
