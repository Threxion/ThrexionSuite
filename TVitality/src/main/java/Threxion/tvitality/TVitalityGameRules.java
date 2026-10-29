package Threxion.tvitality;

import net.minecraft.world.level.GameRules;

/** TV Gamerule。 */
public final class TVitalityGameRules {
    public static final GameRules.Key<GameRules.BooleanValue> FORCE_NORMAL_RECOVERY = GameRules.register(
            "tvForceNormalStaminaRecovery",
            GameRules.Category.PLAYER,
            GameRules.BooleanValue.create(false));

    private TVitalityGameRules() {}
}
