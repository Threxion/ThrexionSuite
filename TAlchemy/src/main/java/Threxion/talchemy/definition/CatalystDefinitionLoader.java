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

public final class CatalystDefinitionLoader implements DefinitionLoader<CatalystDefinition> {
    @Override public PropertyId getType(){ return PropertyId.parse("talchemy:catalyst"); }
    @Override public Set<String> getDirectories(){ return Set.of("talchemy/catalysts"); }

    @Override public CatalystDefinition load(Path file, JsonObject obj) {
        List<CatalystDefinition.Modifier> modifiers = new ArrayList<>();
        if (obj.has("modifiers")) {
            JsonArray a = GsonHelper.getAsJsonArray(obj, "modifiers");
            for (int i=0;i<a.size();i++) {
                JsonObject m=a.get(i).getAsJsonObject();
                double amount=GsonHelper.getAsDouble(m,"amount",0.0);
                modifiers.add(new CatalystDefinition.Modifier(
                        PropertyId.parse(GsonHelper.getAsString(m,"property")),
                        ModifierOperation.parse(GsonHelper.getAsString(m,"operation","add")),
                        amount,
                        GsonHelper.getAsDouble(m,"min",amount),
                        GsonHelper.getAsDouble(m,"max",amount)
                ));
            }
        }
        return new CatalystDefinition(
                PropertyId.parse(GsonHelper.getAsString(obj,"id")),
                GsonHelper.getAsString(obj,"item",""),
                GsonHelper.getAsBoolean(obj,"consumed",true),
                modifiers
        );
    }

    @Override public void validate(CatalystDefinition d) {
        if (d.item().isBlank()) throw new IllegalArgumentException("Catalyst item is required: "+d.getId());
    }
}
