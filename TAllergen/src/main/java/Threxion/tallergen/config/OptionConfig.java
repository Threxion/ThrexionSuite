package Threxion.tallergen.config;

import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;

/**
 * 全局分配选项，对应 config/threxioncore/tallergen/option.json。
 *
 *  - max_allergens：单名玩家可被随机分配到的最大过敏原数量（默认 1）。
 *  - min_allergens：单名玩家可被随机分配到的最小过敏原数量（默认 0）。
 *  - allergen_assignment_chance：新玩家首次登录时进入过敏原分配的概率（0.15 = 15%），
 *    仅在 enabled 判定通过后生效；命中后再按每个过敏原的 initial_allergy_chance 挑选。
 */
public class OptionConfig {

    public final int maxAllergens;
    public final int minAllergens;
    public final double allergenAssignmentChance;

    private static final int DEFAULT_MAX_ALLERGENS = 1;
    private static final int DEFAULT_MIN_ALLERGENS = 0;
    private static final double DEFAULT_ALLERGEN_ASSIGNMENT_CHANCE = 0.15;

    public OptionConfig(int maxAllergens, int minAllergens, double allergenAssignmentChance) {
        this.maxAllergens = maxAllergens;
        this.minAllergens = minAllergens;
        this.allergenAssignmentChance = allergenAssignmentChance;
    }

    public static OptionConfig fromJson(JsonObject obj) {
        return new OptionConfig(
                GsonHelper.getAsInt(obj, "max_allergens", DEFAULT_MAX_ALLERGENS),
                GsonHelper.getAsInt(obj, "min_allergens", DEFAULT_MIN_ALLERGENS),
                GsonHelper.getAsDouble(obj, "allergen_assignment_chance", DEFAULT_ALLERGEN_ASSIGNMENT_CHANCE)
        );
    }
}
