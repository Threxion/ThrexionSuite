package Threxion.tallergen.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * 单个过敏反应配置，对应 config/threxioncore/tallergen/reactions/<id>.json。
 * 定义各 severity 等级的效果/属性/时长，以及 Recovery 阶段的时长。
 */
public class ReactionConfig {
    public final String id;
    public final String allergenId;
    public final int recoveryDurationTicks;
    /** ACTIVE 状态下重复摄入同一过敏原且未升级时，是否刷新当前 Severity 的药水效果持续时间。 */
    public final boolean refreshEffectsOnRepeatIngest;
    public final List<SeverityLevel> severityLevels;

    private static final int DEFAULT_RECOVERY_DURATION_TICKS = 200;

    public ReactionConfig(String id, String allergenId, int recoveryDurationTicks, boolean refreshEffectsOnRepeatIngest, List<SeverityLevel> severityLevels) {
        this.id = id;
        this.allergenId = allergenId;
        this.recoveryDurationTicks = recoveryDurationTicks;
        this.refreshEffectsOnRepeatIngest = refreshEffectsOnRepeatIngest;
        this.severityLevels = List.copyOf(severityLevels);
    }

    public static ReactionConfig fromJson(String fileName, JsonObject obj) {
        List<SeverityLevel> levels = new ArrayList<>();
        if (obj.has("severity_levels")) {
            JsonArray arr = GsonHelper.getAsJsonArray(obj, "severity_levels", new JsonArray());
            for (int i = 0; i < arr.size(); i++) {
                levels.add(SeverityLevel.fromJson(arr.get(i).getAsJsonObject()));
            }
        }
        return new ReactionConfig(
                GsonHelper.getAsString(obj, "id", fileName),
                GsonHelper.getAsString(obj, "allergen_id", ""),
                GsonHelper.getAsInt(obj, "recovery_duration_ticks", DEFAULT_RECOVERY_DURATION_TICKS),
                GsonHelper.getAsBoolean(obj, "refresh_effects_on_repeat_ingest", true),
                levels
        );
    }

    /** 按 severity 取等级配置；找不到返回 null。 */
    public SeverityLevel getLevel(int severity) {
        for (SeverityLevel l : severityLevels) {
            if (l.severity == severity) return l;
        }
        return null;
    }
}
