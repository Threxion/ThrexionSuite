package Threxion.talchemy.definition;

import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.data.DefinitionLoader;
import Threxion.threxioncore.api.modifier.ModifierOperation;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class ProcessTierDefinitionLoader implements DefinitionLoader<ProcessTierDefinition> {
    @Override public PropertyId getType() { return PropertyId.parse("talchemy:process_tier"); }
    @Override public Set<String> getDirectories() { return Set.of("talchemy/tiers"); }

    @Override public ProcessTierDefinition load(Path file, JsonObject obj) {
        List<ProcessTierDefinition.PropertyModifier> modifiers = new ArrayList<>();
        if (obj.has("property_modifiers")) {
            JsonArray a = GsonHelper.getAsJsonArray(obj, "property_modifiers");
            for (int i = 0; i < a.size(); i++) {
                JsonObject m = a.get(i).getAsJsonObject();
                double amount = GsonHelper.getAsDouble(m, "amount", 0.0);
                modifiers.add(new ProcessTierDefinition.PropertyModifier(
                        PropertyId.parse(GsonHelper.getAsString(m, "property")),
                        ModifierOperation.parse(GsonHelper.getAsString(m, "operation", "add")),
                        amount, GsonHelper.getAsDouble(m, "min", amount),
                        GsonHelper.getAsDouble(m, "max", amount)));
            }
        }
        return new ProcessTierDefinition(
                PropertyId.parse(GsonHelper.getAsString(obj, "id")),
                GsonHelper.getAsString(obj, "display_name", ""),
                GsonHelper.getAsDouble(obj, "failure_chance_multiplier", 1.0),
                GsonHelper.getAsInt(obj, "batch_count", 1),
                GsonHelper.getAsBoolean(obj, "recipe_allowed", true),
                modifiers);
    }

    @Override public void validate(ProcessTierDefinition d) {
        if (d.failureChanceMultiplier() < 0) throw new IllegalArgumentException("failure_chance_multiplier must be >= 0: " + d.id());
        if (d.batchCount() < 1) throw new IllegalArgumentException("batch_count must be >= 1: " + d.id());
    }
}
