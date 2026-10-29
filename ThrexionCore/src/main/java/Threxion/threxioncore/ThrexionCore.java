package Threxion.threxioncore;

import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.data.DefaultScanProvider;
import Threxion.threxioncore.data.ThrexionDataLoader;
import Threxion.threxioncore.network.NetworkManager;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * ThrexionCore —— Threxion 系列 Mod 通用前置与数据框架。
 * Core 不理解具体玩法，只提供统一数据框架（Property / Modifier / Transfer /
 * Scan / 事件 / Registry / 网络同步 / 配置加载 / Integration）。
 *
 * <p>加载流程（规格 load_flow）：minecraft_start → threxioncore_load →
 * read_config → load_property_schema → load_definition → load_modifier →
 * load_transfer → detect_mod → load_integration → sync_data → mods_running。</p>
 */
@Mod(ThrexionCore.MODID)
public class ThrexionCore {

    public static final String MODID = ThrexionConstants.MODID;
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public ThrexionCore() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::commonSetup);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            NetworkManager.register();
            ThrexionDataLoader.initialLoad();
            if (!CoreRegistries.SCAN_PROVIDERS.contains(PropertyId.of(ThrexionConstants.CORE_NAMESPACE, "definitions"))) {
                CoreRegistries.SCAN_PROVIDERS.register(PropertyId.of(ThrexionConstants.CORE_NAMESPACE, "definitions"),
                        new DefaultScanProvider());
            }
        });
    }
}
