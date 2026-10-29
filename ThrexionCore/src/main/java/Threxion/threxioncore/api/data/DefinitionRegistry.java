package Threxion.threxioncore.api.data;

import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.property.PropertyValue;
import Threxion.threxioncore.api.registry.Registry;

/**
 * Definition 注册表：存放已加载的 Definition。
 * 对应规格 registry_system.registries 中的 DefinitionRegistry。
 */
public class DefinitionRegistry extends Registry<Definition> {

    public boolean hasDefinition(PropertyId id) {
        return contains(id);
    }
}
