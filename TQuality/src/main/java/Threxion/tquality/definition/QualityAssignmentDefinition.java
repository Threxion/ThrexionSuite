package Threxion.tquality.definition;

import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.data.Definition;

import java.util.List;

/** Assigns one discrete grade by rolling an ephemeral integer range. The roll is never stored. */
public record QualityAssignmentDefinition(
        PropertyId id,
        List<String> whitelist,
        PropertyId qualityDefinition,
        int rollMin,
        int rollMax,
        List<Range> ranges
) implements Definition {
    public record Range(String grade, int min, int max) {}
    @Override public PropertyId getId(){ return id; }
}
