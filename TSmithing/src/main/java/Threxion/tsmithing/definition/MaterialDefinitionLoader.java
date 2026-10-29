package Threxion.tsmithing.definition;

import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.data.DefinitionLoader;
import Threxion.threxioncore.api.property.PropertyValue;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class MaterialDefinitionLoader implements DefinitionLoader<MaterialDefinition> {
    private static final PropertyId TYPE = PropertyId.parse("tsmithing:material");
    @Override public PropertyId getType() { return TYPE; }
    @Override public Set<String> getDirectories() { return Set.of("tsmithing/materials"); }
    @Override public MaterialDefinition load(Path file, JsonObject obj) {
        Map<PropertyId, PropertyValue> props = new LinkedHashMap<>();
        JsonObject p = GsonHelper.getAsJsonObject(obj, "properties", new JsonObject());
        for (Map.Entry<String, JsonElement> e : p.entrySet()) {
            if (!PropertyId.isValid(e.getKey()) || !e.getValue().isJsonPrimitive() || !e.getValue().getAsJsonPrimitive().isNumber()) continue;
            props.put(PropertyId.parse(e.getKey()), PropertyValue.ofNumber(e.getValue().getAsDouble()));
        }
        return new MaterialDefinition(PropertyId.parse(GsonHelper.getAsString(obj, "id")), GsonHelper.getAsString(obj, "display_name", ""), Map.copyOf(props));
    }
    @Override public void validate(MaterialDefinition d) {
        if (d.displayName().isBlank()) throw new IllegalArgumentException("TSmithing material has blank display_name: " + d.id());
    }
}
