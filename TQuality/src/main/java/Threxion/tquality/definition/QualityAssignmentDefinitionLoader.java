package Threxion.tquality.definition;

import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.data.DefinitionLoader;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class QualityAssignmentDefinitionLoader implements DefinitionLoader<QualityAssignmentDefinition> {
    @Override public PropertyId getType(){ return PropertyId.parse("tquality:assignment"); }
    @Override public Set<String> getDirectories(){ return Set.of("tquality/assignments"); }

    @Override public QualityAssignmentDefinition load(Path file, JsonObject obj){
        List<String> items = new ArrayList<>();
        String whitelistKey = obj.has("whitelist") ? "whitelist" : "items";
        if (obj.has(whitelistKey)) {
            JsonArray a = GsonHelper.getAsJsonArray(obj, whitelistKey);
            for (int i = 0; i < a.size(); i++) items.add(a.get(i).getAsString());
        }
        JsonObject roll = GsonHelper.getAsJsonObject(obj, "roll", new JsonObject());
        int rollMin = GsonHelper.getAsInt(roll, "min", 1);
        int rollMax = GsonHelper.getAsInt(roll, "max", 130);

        List<QualityAssignmentDefinition.Range> ranges = new ArrayList<>();
        JsonArray a = GsonHelper.getAsJsonArray(obj, "ranges", new JsonArray());
        for (int i = 0; i < a.size(); i++) {
            JsonObject r = a.get(i).getAsJsonObject();
            ranges.add(new QualityAssignmentDefinition.Range(
                    GsonHelper.getAsString(r, "grade"),
                    GsonHelper.getAsInt(r, "min"),
                    GsonHelper.getAsInt(r, "max")));
        }
        return new QualityAssignmentDefinition(
                PropertyId.parse(GsonHelper.getAsString(obj, "id")),
                List.copyOf(items),
                PropertyId.parse(GsonHelper.getAsString(obj, "quality_definition", "tquality:default")),
                rollMin, rollMax, List.copyOf(ranges));
    }

    @Override public void validate(QualityAssignmentDefinition d){
        if (d.whitelist().isEmpty()) throw new IllegalArgumentException("Quality assignment has no items: " + d.getId());
        if (d.rollMin() > d.rollMax()) throw new IllegalArgumentException("Invalid roll range: " + d.getId());
        if (d.ranges().isEmpty()) throw new IllegalArgumentException("Quality assignment has no ranges: " + d.getId());
        for (var r : d.ranges()) {
            if (r.grade().isBlank() || r.min() > r.max()) throw new IllegalArgumentException("Invalid quality range in " + d.getId());
            if (r.min() < d.rollMin() || r.max() > d.rollMax()) throw new IllegalArgumentException("Quality range outside roll bounds in " + d.getId());
        }
        // Every possible roll must map to exactly one grade. No hidden gaps or overlaps.
        int[] coverage = new int[d.rollMax() - d.rollMin() + 1];
        for (var r : d.ranges()) for (int n = r.min(); n <= r.max(); n++) coverage[n - d.rollMin()]++;
        for (int i = 0; i < coverage.length; i++) {
            if (coverage[i] != 1) throw new IllegalArgumentException("Quality ranges must cover every roll exactly once: " + (d.rollMin() + i) + " in " + d.getId());
        }
    }
}
