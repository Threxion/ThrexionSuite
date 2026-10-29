package Threxion.talchemy.definition;

import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.data.Definition;
import Threxion.threxioncore.api.modifier.ModifierOperation;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * JSON 可扩展的炼金工作流程。
 *
 * Process 只描述“怎么处理”，Recipe 描述“什么材料变成什么”。
 * Core 不理解这些字段；TAlchemy 负责执行。
 */
public final class ProcessDefinition implements Definition {
    public record Stage(
            String id,
            String displayName,
            int durationTicks,
            double failureChance,
            List<StageModifier> modifiers
    ) {}

    public record StageModifier(
            PropertyId property,
            ModifierOperation operation,
            double amount,
            double min,
            double max
    ) {}

    private final PropertyId id;
    private final String displayName;
    private final int time;
    private final List<Stage> stages;
    private final Failure failure;

    public record Failure(double chance, String output, int outputCount) {}

    public ProcessDefinition(PropertyId id, String displayName, int time, List<Stage> stages, Failure failure) {
        this.id = Objects.requireNonNull(id);
        this.displayName = displayName == null || displayName.isBlank() ? id.getString() : displayName;
        this.time = Math.max(0, time);
        this.stages = List.copyOf(stages);
        this.failure = failure == null ? new Failure(0.0, "", 0) : failure;
    }

    public ProcessDefinition(PropertyId id, int time, List<Stage> stages) {
        this(id, id.getString(), time, stages, null);
    }

    @Override public PropertyId getId() { return id; }
    public String displayName() { return displayName; }
    public int time() { return time; }
    public List<Stage> stages() { return stages; }
    public Failure failure() { return failure; }

    /** 总处理时间。存在 stages 时优先使用各阶段时间之和。 */
    public int totalDuration() {
        if (stages.isEmpty()) return time;
        return stages.stream().mapToInt(Stage::durationTicks).sum();
    }
}
