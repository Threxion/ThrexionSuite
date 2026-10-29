package Threxion.tvitality.definition;

import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.data.DefinitionLoader;
import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;

import java.nio.file.Path;
import java.util.Set;

public final class InjuryDefinitionLoader implements DefinitionLoader<InjuryDefinition> {
    @Override public PropertyId getType() { return PropertyId.parse("tvitality:injury"); }
    @Override public Set<String> getDirectories() { return Set.of("tvitality/injuries"); }

    @Override public InjuryDefinition load(Path file, JsonObject obj) {
        String id = GsonHelper.getAsString(obj, "id", "tvitality:" + file.getFileName().toString().replace(".json", ""));
        String displayName = GsonHelper.getAsString(obj, "display_name", id);
        int severity = GsonHelper.getAsInt(obj, "severity", 1);
        boolean enabled = false;
        String item = "";
        int count = 0;
        if (obj.has("doctor_treatment")) {
            JsonObject doctor = GsonHelper.getAsJsonObject(obj, "doctor_treatment");
            enabled = GsonHelper.getAsBoolean(doctor, "enabled", false);
            if (doctor.has("cost")) {
                JsonObject cost = GsonHelper.getAsJsonObject(doctor, "cost");
                item = GsonHelper.getAsString(cost, "item", "");
                count = GsonHelper.getAsInt(cost, "count", 0);
            }
        }
        return new InjuryDefinition(id, displayName, severity, enabled, item, count);
    }

    @Override public void validate(InjuryDefinition d) {
        if (d.getId() == null) throw new IllegalArgumentException("Injury id cannot be null");
        if (d.displayName().isBlank()) throw new IllegalArgumentException("Injury display_name cannot be blank: " + d.getId());
        if (d.doctorTreatmentEnabled() && (d.costItem().isBlank() || d.costCount() <= 0))
            throw new IllegalArgumentException("Doctor treatment cost must define item + positive count: " + d.getId());
    }
}
