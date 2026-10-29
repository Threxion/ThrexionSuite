package Threxion.threxioncore.network;

import Threxion.threxioncore.ThrexionConstants;
import Threxion.threxioncore.ThrexionCore;
import Threxion.threxioncore.config.ThrexionConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.server.ServerLifecycleHooks;

/**
 * 网络管理器（规格 network_model: server_authoritative）。
 * 服务端计算并同步最终数据，客户端不能决定最终数据。
 *
 * <p>同步时机：OnDatapackSyncEvent（玩家进服 / datapack reload）。</p>
 */
public final class NetworkManager {

    private static SimpleChannel CHANNEL;
    private static int nextId = 0;

    private NetworkManager() {
    }

    public static void register() {
        CHANNEL = NetworkRegistry.newSimpleChannel(
                ResourceLocation.fromNamespaceAndPath(ThrexionConstants.MODID, "main"),
                () -> ThrexionConstants.NETWORK_PROTOCOL_VERSION,
                ThrexionConstants.NETWORK_PROTOCOL_VERSION::equals,
                ThrexionConstants.NETWORK_PROTOCOL_VERSION::equals
        );
        CHANNEL.messageBuilder(SyncDataPacket.class, nextId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SyncDataPacket::encode)
                .decoder(SyncDataPacket::new)
                .consumerMainThread(SyncDataPacket::handle)
                .add();
        ThrexionCore.LOGGER.info("[threxioncore] Network channel registered");
    }

    public static SimpleChannel channel() {
        return CHANNEL;
    }

    /** 向单个玩家同步数据（OnDatapackSyncEvent，player 非 null）。 */
    public static void syncToPlayer(ServerPlayer player) {
        if (CHANNEL == null || player == null || player.connection == null) {
            return;
        }
        if (!isSyncEnabled()) {
            return;
        }
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SyncDataPacket());
    }

    /** 向所有在线玩家同步数据（datapack reload 后）。 */
    public static void syncToAll() {
        if (CHANNEL == null) {
            return;
        }
        if (!isSyncEnabled()) {
            return;
        }
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }
        CHANNEL.send(PacketDistributor.ALL.noArg(), new SyncDataPacket());
    }

    private static boolean isSyncEnabled() {
        ThrexionConfig config = ThrexionConfig.get();
        return config == null || config.isNetworkSync();
    }
}
