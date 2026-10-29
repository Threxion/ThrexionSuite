package Threxion.threxioncore.api.reload;

import Threxion.threxioncore.ThrexionCore;
import Threxion.threxioncore.data.ThrexionDataLoader;
import Threxion.threxioncore.network.NetworkManager;

import java.util.Set;

/**
 * ReloadManager（规格 reload_config）：
 * /reload 支持热重载 config / definition / modifier / transfer / partial_schema；
 * Java API、Registry 类型、网络协议不可热重载。
 */
public final class ReloadManager {

    private static final Set<String> HOT_RELOADABLE = Set.of(
            "config_data", "definition", "modifier", "transfer", "partial_schema"
    );

    private static final Set<String> NOT_RELOADABLE = Set.of(
            "java_api", "registry_type", "network_protocol"
    );

    private ReloadManager() {
    }

    public static boolean isHotReloadable(String category) {
        return category != null && HOT_RELOADABLE.contains(category);
    }

    public static boolean isNotReloadable(String category) {
        return category != null && NOT_RELOADABLE.contains(category);
    }

    /** 执行热重载：重读配置 + JSON 数据，并重新同步给客户端。 */
    public static void reload() {
        ThrexionCore.LOGGER.info("[threxioncore] Reloading ThrexionCore data (config + definitions + modifiers + transfers + integrations)...");
        ThrexionDataLoader.reload();
        NetworkManager.syncToAll();
        ThrexionCore.LOGGER.info("[threxioncore] Reload complete.");
    }
}
