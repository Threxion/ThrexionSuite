package Threxion.tallergen.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Reaction 配置里的单个效果条目。{ "effect": "minecraft:hunger", "amplifier": 0, "duration_ticks": 400 }
 */
public class ReactionEffect {
    public final ResourceLocation effectId;
    public final int amplifier;
    /** 0 表示使用 severity level 的 duration_ticks。 */
    public final int durationTicks;

    public ReactionEffect(ResourceLocation effectId, int amplifier, int durationTicks) {
        this.effectId = effectId;
        this.amplifier = amplifier;
        this.durationTicks = durationTicks;
    }

    public static ReactionEffect fromJson(JsonObject obj) {
        ResourceLocation effectId = ResourceLocation.tryParse(GsonHelper.getAsString(obj, "effect", "minecraft:hunger"));
        if (effectId == null) {
            effectId = ResourceLocation.tryParse("minecraft:hunger");
        }
        return new ReactionEffect(
                effectId,
                GsonHelper.getAsInt(obj, "amplifier", 0),
                GsonHelper.getAsInt(obj, "duration_ticks", 0)
        );
    }

    public static List<ReactionEffect> parseList(JsonArray arr) {
        List<ReactionEffect> list = new ArrayList<>();
        if (arr == null) return list;
        for (int i = 0; i < arr.size(); i++) {
            list.add(fromJson(arr.get(i).getAsJsonObject()));
        }
        return list;
    }
}
