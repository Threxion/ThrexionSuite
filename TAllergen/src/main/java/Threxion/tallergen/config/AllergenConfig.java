package Threxion.tallergen.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 单个过敏原的配置，对应 config/threxioncore/tallergen/allergens/<id>.json。
 *
 * 所有字段都有框架级默认值（spec 的 state_machine），缺省/漏写时自动回退。
 * 状态机关键参数（exposure/decay/latency/severity）可在 json 里按过敏原覆盖。
 */
public class AllergenConfig {

    public final String id;
    public final String displayName;
    public final String reactionId;
    /** 直接携带该过敏原的物品 id 列表（如 minecraft:wheat），作为 Recipe Scanner 的种子。 */
    public final List<String> foods;

    /** 初始过敏概率（0.05 = 5%）：新玩家首次登录被随机分配过敏原时，该过敏原被选中的概率。 */
    public final double initialAllergyChance;

    public final double exposureMax;
    public final int decayIntervalTicks;
    public final double decayAmount;
    public final double minExposure;

    public final int severityMax;
    public final int severityStart;

    public final int latencyMinTicks;
    public final int latencyMaxTicks;
    /** Exposure 缩短潜伏期的比例（每次摄入乘 (1 - per_exposure)）。 */
    public final double latencyPerExposure;
    /** 潜伏期缩短的下限乘数：剩余潜伏期不低于 基准潜伏期 * minimum_multiplier。 */
    public final double latencyMinimumMultiplier;

    /** Exposure 累加到恶化概率的比例：chance = base + exposure * per_exposure。 */
    public final double severityPerExposure;
    /** 恶化概率上限。 */
    public final double severityMaximumChance;
    /** 恶化基础概率（摄入时的升级判定底数）。 */
    public final double severityBaseUpgradeChance;

    /** 发作（ACTIVE）默认时长；reaction 的 severity level 未指定时使用。 */
    public final int activeDurationTicks;

    /** 属性修改器的标识符（可写任意字符串，最终按 (此值:attributeId) 派生固定 UUID）。 */
    public final String attributeModifierUuid;

    private static final int DEFAULT_EXPOSURE_MAX = 5;
    private static final int DEFAULT_DECAY_INTERVAL_TICKS = 600;
    private static final int DEFAULT_DECAY_AMOUNT = 1;
    private static final int DEFAULT_MIN_EXPOSURE = 0;
    private static final double DEFAULT_INITIAL_ALLERGY_CHANCE = 0.05;
    private static final int DEFAULT_SEVERITY_MAX = 3;
    private static final int DEFAULT_SEVERITY_START = 1;
    private static final int DEFAULT_LATENCY_MIN_TICKS = 200;
    private static final int DEFAULT_LATENCY_MAX_TICKS = 600;
    private static final double DEFAULT_LATENCY_PER_EXPOSURE = 0.05;
    private static final double DEFAULT_LATENCY_MINIMUM_MULTIPLIER = 0.25;
    private static final double DEFAULT_SEVERITY_PER_EXPOSURE = 0.02;
    private static final double DEFAULT_SEVERITY_MAXIMUM_CHANCE = 0.9;
    private static final double DEFAULT_SEVERITY_BASE_UPGRADE_CHANCE = 0.2;
    private static final int DEFAULT_ACTIVE_DURATION_TICKS = 400;

    private AllergenConfig(
            String id, String displayName, String reactionId, List<String> foods,
            double initialAllergyChance,
            double exposureMax, int decayIntervalTicks, double decayAmount, double minExposure,
            int severityMax, int severityStart,
            int latencyMinTicks, int latencyMaxTicks, double latencyPerExposure,
            double latencyMinimumMultiplier, double severityPerExposure,
            double severityMaximumChance, double severityBaseUpgradeChance,
            int activeDurationTicks, String attributeModifierUuid) {
        this.id = id;
        this.displayName = displayName;
        this.reactionId = reactionId;
        this.foods = Collections.unmodifiableList(foods);
        this.initialAllergyChance = initialAllergyChance;
        this.exposureMax = exposureMax;
        this.decayIntervalTicks = decayIntervalTicks;
        this.decayAmount = decayAmount;
        this.minExposure = minExposure;
        this.severityMax = severityMax;
        this.severityStart = severityStart;
        this.latencyMinTicks = latencyMinTicks;
        this.latencyMaxTicks = latencyMaxTicks;
        this.latencyPerExposure = latencyPerExposure;
        this.latencyMinimumMultiplier = latencyMinimumMultiplier;
        this.severityPerExposure = severityPerExposure;
        this.severityMaximumChance = severityMaximumChance;
        this.severityBaseUpgradeChance = severityBaseUpgradeChance;
        this.activeDurationTicks = activeDurationTicks;
        this.attributeModifierUuid = attributeModifierUuid;
    }

    public static AllergenConfig fromJson(String fileName, JsonObject obj) {
        List<String> foods = new ArrayList<>();
        if (obj.has("foods")) {
            JsonArray arr = GsonHelper.getAsJsonArray(obj, "foods", new JsonArray());
            for (int i = 0; i < arr.size(); i++) {
                foods.add(arr.get(i).getAsString());
            }
        }
        String id = GsonHelper.getAsString(obj, "id", fileName);
        return new AllergenConfig(
                id,
                GsonHelper.getAsString(obj, "display_name", id),
                GsonHelper.getAsString(obj, "reaction_id", ""),
                foods,
                GsonHelper.getAsDouble(obj, "initial_allergy_chance", DEFAULT_INITIAL_ALLERGY_CHANCE),
                GsonHelper.getAsDouble(obj, "exposure_max", DEFAULT_EXPOSURE_MAX),
                GsonHelper.getAsInt(obj, "decay_interval_ticks", DEFAULT_DECAY_INTERVAL_TICKS),
                GsonHelper.getAsDouble(obj, "decay_amount", DEFAULT_DECAY_AMOUNT),
                GsonHelper.getAsDouble(obj, "min_exposure", DEFAULT_MIN_EXPOSURE),
                GsonHelper.getAsInt(obj, "severity_max", DEFAULT_SEVERITY_MAX),
                GsonHelper.getAsInt(obj, "severity_start", DEFAULT_SEVERITY_START),
                GsonHelper.getAsInt(obj, "latency_min_ticks", DEFAULT_LATENCY_MIN_TICKS),
                GsonHelper.getAsInt(obj, "latency_max_ticks", DEFAULT_LATENCY_MAX_TICKS),
                GsonHelper.getAsDouble(obj, "latency_per_exposure", DEFAULT_LATENCY_PER_EXPOSURE),
                GsonHelper.getAsDouble(obj, "latency_minimum_multiplier", DEFAULT_LATENCY_MINIMUM_MULTIPLIER),
                GsonHelper.getAsDouble(obj, "severity_per_exposure", DEFAULT_SEVERITY_PER_EXPOSURE),
                GsonHelper.getAsDouble(obj, "severity_maximum_chance", DEFAULT_SEVERITY_MAXIMUM_CHANCE),
                GsonHelper.getAsDouble(obj, "severity_base_upgrade_chance", DEFAULT_SEVERITY_BASE_UPGRADE_CHANCE),
                GsonHelper.getAsInt(obj, "active_duration_ticks", DEFAULT_ACTIVE_DURATION_TICKS),
                GsonHelper.getAsString(obj, "attribute_modifier_uuid", "tallergen:" + id)
        );
    }
}
