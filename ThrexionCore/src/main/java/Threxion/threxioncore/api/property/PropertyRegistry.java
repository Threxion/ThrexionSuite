package Threxion.threxioncore.api.property;

import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.registry.Registry;

import java.util.Optional;

/**
 * Property 注册表：存放各 Mod 或 JSON 注册的 PropertySchema。
 * 对应规格 registry_system.registries 中的 PropertyRegistry。
 */
public class PropertyRegistry extends Registry<PropertySchema> {

    public Optional<PropertySchema> schema(PropertyId id) {
        return get(id);
    }

    public boolean isRegistered(PropertyId id) {
        return contains(id);
    }
}
