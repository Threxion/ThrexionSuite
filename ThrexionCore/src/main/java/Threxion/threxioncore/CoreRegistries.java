package Threxion.threxioncore;

import Threxion.threxioncore.api.data.DefinitionRegistry;
import Threxion.threxioncore.api.integration.IntegrationRegistry;
import Threxion.threxioncore.api.modifier.ModifierRegistry;
import Threxion.threxioncore.api.property.PropertyRegistry;
import Threxion.threxioncore.api.scan.ScanProviderRegistry;
import Threxion.threxioncore.api.transfer.TransferRegistry;

/**
 * 核心注册表持有者（规格 registry_system.registries）：
 * PropertyRegistry / DefinitionRegistry / ModifierRegistry / TransferRegistry /
 * ScanProviderRegistry / IntegrationRegistry。
 *
 * <p>各 Mod 在初始化时通过静态字段直接注册自己的内容。</p>
 */
public final class CoreRegistries {

    private CoreRegistries() {
    }

    public static final PropertyRegistry PROPERTIES = new PropertyRegistry();
    public static final DefinitionRegistry DEFINITIONS = new DefinitionRegistry();
    public static final ModifierRegistry MODIFIERS = new ModifierRegistry();
    public static final TransferRegistry TRANSFERS = new TransferRegistry();
    public static final ScanProviderRegistry SCAN_PROVIDERS = new ScanProviderRegistry();
    public static final IntegrationRegistry INTEGRATIONS = new IntegrationRegistry();
}
