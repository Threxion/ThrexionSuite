package Threxion.tvitality.definition;

import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.data.DefinitionLoader;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class FoodDefinitionLoader implements DefinitionLoader<FoodDefinition> {
    @Override public PropertyId getType() { return PropertyId.parse("tvitality:food"); }
    @Override public Set<String> getDirectories() { return Set.of("tvitality/food"); }

    @Override public FoodDefinition load(Path file, JsonObject obj) {
        String id = GsonHelper.getAsString(obj, "id", "tvitality:" + file.getFileName().toString().replace(".json", ""));
        boolean enabled = GsonHelper.getAsBoolean(obj, "enabled", true);
        String item = GsonHelper.getAsString(obj, "item", "minecraft:air");
        FoodDefinition.Category category = FoodDefinition.Category.valueOf(GsonHelper.getAsString(obj, "category", "meal").toUpperCase(Locale.ROOT));
        long duration = GsonHelper.getAsLong(obj, "duration_seconds", 0L);
        int stamina = GsonHelper.getAsInt(obj, "stamina_restore", 0);
        List<FoodDefinition.Effect> effects = new ArrayList<>();
        if (obj.has("effects")) {
            JsonArray array = GsonHelper.getAsJsonArray(obj, "effects");
            for (JsonElement element : array) {
                JsonObject e = element.getAsJsonObject();
                effects.add(new FoodDefinition.Effect(
                        GsonHelper.getAsString(e, "effect", ""),
                        GsonHelper.getAsInt(e, "amplifier", 0),
                        GsonHelper.getAsInt(e, "duration_ticks", 0)));
            }
        }
        return new FoodDefinition(id, enabled, item, category, duration, stamina, effects);
    }

    @Override public void validate(FoodDefinition d) {
        if (d.itemId() == null) throw new IllegalArgumentException("Invalid food item: " + d.getId());
        if (d.durationSeconds() < 0) throw new IllegalArgumentException("Food duration < 0: " + d.getId());
        if (d.staminaRestore() < 0) throw new IllegalArgumentException("Food stamina_restore < 0: " + d.getId());
        for (FoodDefinition.Effect e : d.effects()) {
            if (e.id().isBlank() || e.durationTicks() < 0 || e.amplifier() < 0)
                throw new IllegalArgumentException("Invalid food effect: " + d.getId());
        }
    }
}
