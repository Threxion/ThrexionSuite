package Threxion.tallergen.client;

import Threxion.tallergen.network.packet.AllergySyncPacket;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 客户端网络包处理（仅客户端加载）。
 */
public final class ClientPacketHandler {
    private ClientPacketHandler() {
    }

    public static void handleSync(AllergySyncPacket msg) {
        Map<String, ClientAllergenState> map = new HashMap<>();
        for (AllergySyncPacket.Entry e : msg.getEntries()) {
            map.put(e.allergenId, new ClientAllergenState(e.allergenId, e.displayName, e.exposure, e.phase, e.severity));
        }
        Set<String> playerAllergens = new HashSet<>(msg.getPlayerAllergens());
        ClientAllergyData.update(map, playerAllergens, msg.isStatsDisplay());
    }
}
