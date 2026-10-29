package Threxion.tquality.definition;

import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.data.DefinitionLoader;
import Threxion.threxioncore.api.property.PropertyValue;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

public final class QualityDefinitionLoader implements DefinitionLoader<QualityDefinition> {
    @Override public PropertyId getType(){ return PropertyId.parse("tquality:grade"); }
    @Override public Set<String> getDirectories(){ return Set.of("tquality/grades"); }

    @Override public QualityDefinition load(Path file, JsonObject obj){
        List<QualityDefinition.Grade> grades = new ArrayList<>();
        var a = GsonHelper.getAsJsonArray(obj, "grades", new com.google.gson.JsonArray());
        for (int i = 0; i < a.size(); i++) {
            JsonObject g = a.get(i).getAsJsonObject();
            var props = new LinkedHashMap<PropertyId, PropertyValue>();
            if (g.has("properties")) {
                for (var e : g.getAsJsonObject("properties").entrySet()) {
                    props.put(PropertyId.parse(e.getKey()), parseValue(e.getValue()));
                }
            }
            grades.add(new QualityDefinition.Grade(
                    GsonHelper.getAsString(g, "id"),
                    GsonHelper.getAsString(g, "display_name", GsonHelper.getAsString(g, "id")),
                    GsonHelper.getAsInt(g, "order", i),
                    Map.copyOf(props)));
        }
        return new QualityDefinition(
                PropertyId.parse(GsonHelper.getAsString(obj, "id")),
                GsonHelper.getAsString(obj, "display_name", ""),
                List.copyOf(grades));
    }

    private static PropertyValue parseValue(JsonElement e) {
        if (!e.isJsonPrimitive()) throw new IllegalArgumentException("Quality property value must be primitive");
        var p = e.getAsJsonPrimitive();
        if (p.isBoolean()) return PropertyValue.ofBoolean(p.getAsBoolean());
        if (p.isNumber()) return PropertyValue.ofNumber(p.getAsDouble());
        return PropertyValue.ofString(p.getAsString());
    }

    @Override public void validate(QualityDefinition d) {
        if (d.grades().isEmpty()) throw new IllegalArgumentException("Quality definition has no grades: " + d.getId());
        var ids = new java.util.HashSet<String>();
        var orders = new java.util.HashSet<Integer>();
        for (var g : d.grades()) {
            if (g.id().isBlank() || !ids.add(g.id())) throw new IllegalArgumentException("Duplicate/blank grade in " + d.getId());
            if (!orders.add(g.order())) throw new IllegalArgumentException("Duplicate grade order in " + d.getId());
        }
    }
}
