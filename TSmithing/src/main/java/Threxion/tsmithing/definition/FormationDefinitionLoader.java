package Threxion.tsmithing.definition;

import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.data.DefinitionLoader;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class FormationDefinitionLoader implements DefinitionLoader<FormationDefinition> {
    private static final PropertyId TYPE = PropertyId.parse("tsmithing:formation");
    @Override public PropertyId getType() { return TYPE; }
    @Override public Set<String> getDirectories() { return Set.of("tsmithing/formations"); }
    @Override public FormationDefinition load(Path file, JsonObject obj) {
        List<FormationDefinition.MaterialInput> materials = new ArrayList<>();
        JsonArray a = GsonHelper.getAsJsonArray(obj, "materials", new JsonArray());
        for (int i = 0; i < a.size(); i++) {
            JsonObject m = a.get(i).getAsJsonObject();
            materials.add(new FormationDefinition.MaterialInput(
                    GsonHelper.getAsString(m, "item"),
                    GsonHelper.getAsString(m, "material"),
                    GsonHelper.getAsInt(m, "count", 1)));
        }
        Map<PropertyId, Double> tqeff = new LinkedHashMap<>();
        JsonObject q = GsonHelper.getAsJsonObject(obj, "tqeff", new JsonObject());
        for (var e : q.entrySet()) if (PropertyId.isValid(e.getKey()) && e.getValue().isJsonPrimitive() && e.getValue().getAsJsonPrimitive().isNumber()) tqeff.put(PropertyId.parse(e.getKey()), e.getValue().getAsDouble());
        return new FormationDefinition(PropertyId.parse(GsonHelper.getAsString(obj, "id")), GsonHelper.getAsString(obj, "display_name", ""), GsonHelper.getAsString(obj, "result_item"), List.copyOf(materials), Map.copyOf(tqeff));
    }
    @Override public void validate(FormationDefinition d) {
        if (d.displayName().isBlank()) throw new IllegalArgumentException("TSmithing formation has blank display_name: " + d.id());
        if (d.resultItem().isBlank()) throw new IllegalArgumentException("TSmithing formation has blank result_item: " + d.id());
        if (d.materials().isEmpty()) throw new IllegalArgumentException("TSmithing formation has no materials: " + d.id());
        for (var m : d.materials()) if (m.count() < 1) throw new IllegalArgumentException("TSmithing material count must be >= 1: " + d.id());
        for (double v : d.tqeff().values()) if (v < 0 || v > 100) throw new IllegalArgumentException("TSmithing tqeff must be 0..100: " + d.id());
    }
}
