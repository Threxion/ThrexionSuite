package Threxion.tallergen.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 客户端过敏数据缓存（仅客户端存在，物理服务端不加载）。
 * 数据完全来自服务端同步包，客户端不决定过敏结果。
 */
public final class ClientAllergyData {
    private static final Map<String, ClientAllergenState> STATES = new HashMap<>();
    private static Set<String> PLAYER_ALLERGENS = Set.of();
    private static boolean STATS_DISPLAY = true;

    private ClientAllergyData() {
    }

    public static void update(Map<String, ClientAllergenState> states, Set<String> playerAllergens, boolean statsDisplay) {
        STATES.clear();
        STATES.putAll(states);
        PLAYER_ALLERGENS = Set.copyOf(playerAllergens);
        STATS_DISPLAY = statsDisplay;
    }

    public static List<ClientAllergenState> getStates() {
        return List.copyOf(STATES.values());
    }

    public static Set<String> getPlayerAllergens() {
        return PLAYER_ALLERGENS;
    }

    /** gamerule tAllergyStatsDisplay 的客户端同步副本（服务端裁决）。 */
    public static boolean isStatsDisplay() {
        return STATS_DISPLAY;
    }
}
