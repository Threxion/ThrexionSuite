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

public final class AlchemyEffectDefinitionLoader implements DefinitionLoader<AlchemyEffectDefinition> {
    @Override public PropertyId getType() { return PropertyId.parse("talchemy:effect"); }
    @Override public Set<String> getDirectories() { return Set.of("talchemy/effects"); }

    @Override
    public AlchemyEffectDefinition load(Path file, JsonObject obj) {
        List<AlchemyEffectDefinition.PropertyModifier> modifiers = new ArrayList<>();
        if (obj.has("property_modifiers")) {
            JsonArray arr = GsonHelper.getAsJsonArray(obj, "property_modifiers");
            for (int i = 0; i < arr.size(); i++) {
                JsonObject m = arr.get(i).getAsJsonObject();
                double amount = GsonHelper.getAsDouble(m, "amount", 0.0);
                modifiers.add(new AlchemyEffectDefinition.PropertyModifier(
                        PropertyId.parse(GsonHelper.getAsString(m, "property")),
                        ModifierOperation.parse(GsonHelper.getAsString(m, "operation", "add")),
                        amount,
                        GsonHelper.getAsDouble(m, "min", amount),
                        GsonHelper.getAsDouble(m, "max", amount)));
            }
        }

        List<AlchemyEffectDefinition.PotionEffect> potionEffects = new ArrayList<>();
        if (obj.has("potion_effects")) {
            JsonArray arr = GsonHelper.getAsJsonArray(obj, "potion_effects");
            for (int i = 0; i < arr.size(); i++) {
                JsonObject e = arr.get(i).getAsJsonObject();
                potionEffects.add(new AlchemyEffectDefinition.PotionEffect(
                        GsonHelper.getAsString(e, "effect"),
                        GsonHelper.getAsInt(e, "duration_ticks", 200),
                        GsonHelper.getAsInt(e, "amplifier", 0),
                        GsonHelper.getAsBoolean(e, "ambient", false),
                        GsonHelper.getAsBoolean(e, "show_particles", true),
                        GsonHelper.getAsBoolean(e, "show_icon", true)));
            }
        }

        return new AlchemyEffectDefinition(
                PropertyId.parse(GsonHelper.getAsString(obj, "id")),
                GsonHelper.getAsString(obj, "display_name", ""),
                modifiers, potionEffects);
    }

    @Override
    public void validate(AlchemyEffectDefinition definition) {
        for (var e : definition.potionEffects()) {
            if (e.effect().isBlank()) throw new IllegalArgumentException("Potion effect id is required: " + definition.getId());
            if (e.durationTicks() < 1) throw new IllegalArgumentException("Potion effect duration_ticks must be >= 1: " + definition.getId());
            if (e.amplifier() < 0) throw new IllegalArgumentException("Potion effect amplifier must be >= 0: " + definition.getId());
        }
    }
}
