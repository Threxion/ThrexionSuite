package Threxion.tallergen;

import Threxion.tallergen.network.ModNetwork;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;

/**
 * 自定义 gamerule 注册。
 *
 * tAllergyStatsDisplay：控制客户端左上角 HUD 是否显示当前过敏状态，默认 true。
 * 注意：vanilla 1.20.1 的 gamerule 不会同步到客户端（客户端 Level 持有独立的默认 GameRules），
 * 因此本 mod 通过 {@link ModNetwork#sendAllergySync(net.minecraft.server.level.ServerPlayer)}
 * 把该 gamerule 的值随过敏状态同步包发给客户端；规则变化时经 change listener 广播给在线玩家。
 *
 * 本类必须在任何 GameRules 实例创建（世界加载）之前加载——由 {@link TallergenMod} 构造函数引用触发。
 */
public final class TallergenGameRules {

    public static final GameRules.Key<GameRules.BooleanValue> ALLERGY_STATS_DISPLAY = GameRules.register(
            "tAllergyStatsDisplay",
            GameRules.Category.PLAYER,
            GameRules.BooleanValue.create(true, TallergenGameRules::broadcastAllergyDisplay));

    private TallergenGameRules() {
    }

    /** 规则被 /gamerule 修改时调用：把新值广播给所有在线玩家（连同其过敏状态一起同步）。 */
    private static void broadcastAllergyDisplay(MinecraftServer server, GameRules.BooleanValue value) {
        if (server == null) {
            return;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            ModNetwork.sendAllergySync(player);
        }
    }
}
