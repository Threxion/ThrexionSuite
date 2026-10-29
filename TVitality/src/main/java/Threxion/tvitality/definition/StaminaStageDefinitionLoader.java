package Threxion.tvitality.definition;

import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.data.DefinitionLoader;
import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;

import java.nio.file.Path;
import java.util.Set;

public final class StaminaStageDefinitionLoader implements DefinitionLoader<StaminaStageDefinition> {
    @Override public PropertyId getType() { return PropertyId.parse("tvitality:stamina_stage"); }
    @Override public Set<String> getDirectories() { return Set.of("tvitality/stamina/stages"); }

    @Override public StaminaStageDefinition load(Path file, JsonObject obj) {
        String id = GsonHelper.getAsString(obj, "id", "tvitality:" + file.getFileName().toString().replace(".json", ""));
        String name = GsonHelper.getAsString(obj, "display_name", id);
        int min = GsonHelper.getAsInt(obj, "min", 0);
        int max = GsonHelper.getAsInt(obj, "max", 0);
        double speed = GsonHelper.getAsDouble(obj, "movement_speed_multiplier", 1.0D);
        return new StaminaStageDefinition(id, name, min, max, speed);
    }

    @Override public void validate(StaminaStageDefinition d) {
        if (d.min() > d.max()) throw new IllegalArgumentException("Stamina stage min > max: " + d.getId());
        if (d.movementSpeedMultiplier() < 0.0D) throw new IllegalArgumentException("Stamina stage speed multiplier < 0: " + d.getId());
    }
}
