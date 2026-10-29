package Threxion.tvitality.definition;

import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.data.DefinitionLoader;
import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;

import java.nio.file.Path;
import java.util.Set;

public final class StaminaConsumptionDefinitionLoader implements DefinitionLoader<StaminaConsumptionDefinition> {
    @Override public PropertyId getType() { return PropertyId.parse("tvitality:stamina_consumption"); }
    @Override public Set<String> getDirectories() { return Set.of("tvitality/stamina/consumption"); }

    @Override public StaminaConsumptionDefinition load(Path file, JsonObject obj) {
        String id = GsonHelper.getAsString(obj, "id", "tvitality:" + file.getFileName().toString().replace(".json", ""));
        String action = GsonHelper.getAsString(obj, "action", file.getFileName().toString().replace(".json", ""));
        boolean enabled = GsonHelper.getAsBoolean(obj, "enabled", true);
        int amount = GsonHelper.getAsInt(obj, "amount", 0);
        return new StaminaConsumptionDefinition(id, action, enabled, amount);
    }

    @Override public void validate(StaminaConsumptionDefinition d) {
        if (d.action().isBlank()) throw new IllegalArgumentException("Stamina consumption action cannot be blank: " + d.getId());
        if (d.amount() < 0) throw new IllegalArgumentException("Stamina consumption amount < 0: " + d.getId());
    }
}
