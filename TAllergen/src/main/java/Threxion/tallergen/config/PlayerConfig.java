package Threxion.tallergen.config;

import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;

/**
 * 玩家相关配置，对应 config/threxioncore/tallergen/player.json。
 *
 *  - random_allergen_enabled：是否在新玩家首次登录时按概率随机分配过敏原（总开关）。
 *  - random_allergen_chance：历史配置（原默认 5%）。自 option.json 引入后，
 *    过敏原分配概率改由 option.json 的 allergen_assignment_chance（默认 15%）控制，
 *    本字段仅保留解析以兼容旧配置，不再参与分配判定。
 */
public class PlayerConfig {

    public final boolean randomAllergenEnabled;
    public final double randomAllergenChance;

    private static final boolean DEFAULT_RANDOM_ALLERGEN_ENABLED = true;
    private static final double DEFAULT_RANDOM_ALLERGEN_CHANCE = 0.05;

    public PlayerConfig(boolean randomAllergenEnabled, double randomAllergenChance) {
        this.randomAllergenEnabled = randomAllergenEnabled;
        this.randomAllergenChance = randomAllergenChance;
    }

    public static PlayerConfig fromJson(JsonObject obj) {
        return new PlayerConfig(
                GsonHelper.getAsBoolean(obj, "random_allergen_enabled", DEFAULT_RANDOM_ALLERGEN_ENABLED),
                GsonHelper.getAsDouble(obj, "random_allergen_chance", DEFAULT_RANDOM_ALLERGEN_CHANCE)
        );
    }
}
