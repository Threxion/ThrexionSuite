package Threxion.tallergen.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;

import java.util.List;

/**
 * 单个 severity 等级：进入该等级后持续 duration_ticks，并施加 effects/attributes。
 */
public class SeverityLevel {
    public final int severity;
    public final int durationTicks;
    public final List<ReactionEffect> effects;
    public final List<ReactionAttribute> attributes;

    public SeverityLevel(int severity, int durationTicks, List<ReactionEffect> effects, List<ReactionAttribute> attributes) {
        this.severity = severity;
        this.durationTicks = durationTicks;
        this.effects = effects;
        this.attributes = attributes;
    }

    public static SeverityLevel fromJson(JsonObject obj) {
        return new SeverityLevel(
                GsonHelper.getAsInt(obj, "severity", 1),
                GsonHelper.getAsInt(obj, "duration_ticks", 400),
                ReactionEffect.parseList(GsonHelper.getAsJsonArray(obj, "effects", new JsonArray())),
                ReactionAttribute.parseList(GsonHelper.getAsJsonArray(obj, "attributes", new JsonArray()))
        );
    }
}
