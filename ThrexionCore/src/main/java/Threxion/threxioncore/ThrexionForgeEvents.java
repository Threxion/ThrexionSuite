package Threxion.threxioncore;

import Threxion.threxioncore.command.ThrexionCommand;
import Threxion.threxioncore.network.NetworkManager;
import Threxion.threxioncore.reload.ThrexionReloadListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * FORGE bus 事件监听器：/reload 钩子、数据同步（OnDatapackSyncEvent）、命令注册。
 */
@Mod.EventBusSubscriber(modid = ThrexionCore.MODID)
public class ThrexionForgeEvents {

    /** 注册 /reload 监听器，触发配置与数据热重载。 */
    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(new ThrexionReloadListener());
    }

    /**
     * 服务端权威同步：玩家进服 / datapack reload 时向客户端下发数据。
     * 客户端不能决定最终数据（server authoritative）。
     */
    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() != null) {
            NetworkManager.syncToPlayer(event.getPlayer());
        } else {
            for (ServerPlayer player : event.getPlayers()) {
                NetworkManager.syncToPlayer(player);
            }
        }
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        ThrexionCommand.register(event);
    }
}
