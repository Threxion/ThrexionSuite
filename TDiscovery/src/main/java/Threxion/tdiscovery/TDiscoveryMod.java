package Threxion.tdiscovery;

import Threxion.tdiscovery.definition.DiscoveryEntryDefinitionLoader;
import Threxion.tdiscovery.network.DiscoveryNetwork;
import Threxion.threxioncore.api.data.DefinitionLoaderRegistry;
import net.minecraftforge.fml.common.Mod;

@Mod(TDiscoveryMod.MOD_ID)
public final class TDiscoveryMod {
    public static final String MOD_ID = "tdiscovery";
    public TDiscoveryMod() {
        DefinitionLoaderRegistry.register("tdiscovery:entry", new DiscoveryEntryDefinitionLoader());
        DiscoveryNetwork.register();
    }
}
