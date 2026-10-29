package Threxion.threxioncore.network;

/**
 * 客户端网络包处理（仅物理客户端通过 DistExecutor 调用，类本身无客户端依赖）。
 */
public final class ClientPacketHandler {

    private ClientPacketHandler() {
    }

    public static void handleSyncData(SyncDataPacket packet) {
        ClientDataCache.INSTANCE.replace(packet.getDefinitions());
    }
}
