package Threxion.threxioncore.network;

import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.data.Definition;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 客户端数据缓存（server authoritative 模型下的只读镜像）。
 * 服务端通过 SyncDataPacket 推送，客户端仅读取，不能修改最终数据。
 */
public final class ClientDataCache {

    public static final ClientDataCache INSTANCE = new ClientDataCache();

    private volatile Map<PropertyId, Definition> definitions = Map.of();

    private ClientDataCache() {
    }

    public void replace(List<Definition> defs) {
        Map<PropertyId, Definition> map = new LinkedHashMap<>();
        if (defs != null) {
            for (Definition d : defs) {
                map.put(d.getId(), d);
            }
        }
        this.definitions = Map.copyOf(map);
    }

    public Optional<Definition> getDefinition(PropertyId id) {
        return Optional.ofNullable(definitions.get(id));
    }

    public Collection<Definition> getDefinitions() {
        return definitions.values();
    }

    public int size() {
        return definitions.size();
    }
}
