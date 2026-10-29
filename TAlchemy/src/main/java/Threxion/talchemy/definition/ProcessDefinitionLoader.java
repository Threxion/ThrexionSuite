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

public final class ProcessDefinitionLoader implements DefinitionLoader<ProcessDefinition> {
    @Override public PropertyId getType() { return PropertyId.parse("talchemy:process"); }
    @Override public Set<String> getDirectories() { return Set.of("talchemy/processes"); }

    @Override
    public ProcessDefinition load(Path file, JsonObject obj) {
        PropertyId id = PropertyId.parse(GsonHelper.getAsString(obj, "id"));
        String displayName = GsonHelper.getAsString(obj, "display_name", id.getString());
        int time = GsonHelper.getAsInt(obj, "time", 0);

        List<ProcessDefinition.Stage> stages = new ArrayList<>();
        if (obj.has("stages")) {
            JsonArray arr = GsonHelper.getAsJsonArray(obj, "stages");
            for (int i = 0; i < arr.size(); i++) {
                JsonObject s = arr.get(i).getAsJsonObject();
                List<ProcessDefinition.StageModifier> modifiers = new ArrayList<>();
                if (s.has("modifiers")) {
                    JsonArray ms = GsonHelper.getAsJsonArray(s, "modifiers");
                    for (int j = 0; j < ms.size(); j++) {
                        JsonObject m = ms.get(j).getAsJsonObject();
                        double amount = GsonHelper.getAsDouble(m, "amount", 0.0);
                        modifiers.add(new ProcessDefinition.StageModifier(
                                PropertyId.parse(GsonHelper.getAsString(m, "property")),
                                ModifierOperation.parse(GsonHelper.getAsString(m, "operation", "add")),
                                amount,
                                GsonHelper.getAsDouble(m, "min", amount),
                                GsonHelper.getAsDouble(m, "max", amount)
                        ));
                    }
                }
                String stageId = GsonHelper.getAsString(s, "id", "stage_" + i);
                stages.add(new ProcessDefinition.Stage(
                        stageId,
                        GsonHelper.getAsString(s, "display_name", stageId),
                        GsonHelper.getAsInt(s, "duration_ticks", 0),
                        GsonHelper.getAsDouble(s, "failure_chance", 0.0),
                        modifiers
                ));
            }
        }
        ProcessDefinition.Failure failure = null;
        if (obj.has("failure")) {
            JsonObject f = GsonHelper.getAsJsonObject(obj, "failure");
            failure = new ProcessDefinition.Failure(
                    GsonHelper.getAsDouble(f, "chance", 0.0),
                    GsonHelper.getAsString(f, "output", ""),
                    GsonHelper.getAsInt(f, "output_count", 1));
        }
        return new ProcessDefinition(id, displayName, time, stages, failure);
    }

    @Override
    public void validate(ProcessDefinition definition) {
                if (definition.totalDuration() < 0)
            throw new IllegalArgumentException("Process duration must be >= 0: " + definition.getId());

        if (definition.failure().chance() < 0 || definition.failure().chance() > 1)
            throw new IllegalArgumentException("Process failure.chance must be 0..1: " + definition.getId());
        if (!definition.failure().output().isBlank() && definition.failure().outputCount() < 1)
            throw new IllegalArgumentException("Process failure.output_count must be >= 1: " + definition.getId());

        for (ProcessDefinition.Stage stage : definition.stages()) {
            if (stage.id().isBlank())
                throw new IllegalArgumentException("Process stage id is required: " + definition.getId());
            if (stage.durationTicks() < 0)
                throw new IllegalArgumentException("Stage duration must be >= 0: " + definition.getId());
            if (stage.failureChance() < 0 || stage.failureChance() > 1)
                throw new IllegalArgumentException("Stage failure_chance must be 0..1: " + definition.getId());
        }
    }
}
