package Threxion.tvitality.definition;

import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.data.DefinitionLoader;
import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;

import java.nio.file.Path;
import java.util.Set;

public final class StaminaSettingsDefinitionLoader implements DefinitionLoader<StaminaSettingsDefinition> {
    @Override public PropertyId getType() { return PropertyId.parse("tvitality:stamina_settings"); }
    @Override public Set<String> getDirectories() { return Set.of("tvitality/stamina"); }

    @Override public StaminaSettingsDefinition load(Path file, JsonObject obj) {
        String id = GsonHelper.getAsString(obj, "id", "tvitality:stamina_settings");
        int max = GsonHelper.getAsInt(obj, "max", 1000);
        int initial = GsonHelper.getAsInt(obj, "initial", max);
        return new StaminaSettingsDefinition(id, max, initial);
    }

    @Override public void validate(StaminaSettingsDefinition d) {
        if (d.max() <= 0) throw new IllegalArgumentException("Stamina max must be > 0");
        if (d.initial() < 0 || d.initial() > d.max()) throw new IllegalArgumentException("Stamina initial must be within 0..max");
    }
}
