package Threxion.tvitality.definition;

import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.data.DefinitionLoader;
import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;

import java.nio.file.Path;
import java.util.Set;

public final class StaminaRecoveryDefinitionLoader implements DefinitionLoader<StaminaRecoveryDefinition> {
    @Override public PropertyId getType() { return PropertyId.parse("tvitality:stamina_recovery"); }
    @Override public Set<String> getDirectories() { return Set.of("tvitality/stamina/recovery"); }

    @Override public StaminaRecoveryDefinition load(Path file, JsonObject obj) {
        String id = GsonHelper.getAsString(obj, "id", "tvitality:" + file.getFileName().toString().replace(".json", ""));
        String mode = GsonHelper.getAsString(obj, "mode", "rest");
        boolean enabled = GsonHelper.getAsBoolean(obj, "enabled", false);
        long interval = GsonHelper.getAsLong(obj, "interval_seconds", 60L);
        int amount = GsonHelper.getAsInt(obj, "amount", 0);
        return new StaminaRecoveryDefinition(id, mode, enabled, interval, amount);
    }

    @Override public void validate(StaminaRecoveryDefinition d) {
        if (d.intervalSeconds() <= 0) throw new IllegalArgumentException("Recovery interval must be > 0: " + d.getId());
        if (d.amount() < 0) throw new IllegalArgumentException("Recovery amount < 0: " + d.getId());
    }
}
