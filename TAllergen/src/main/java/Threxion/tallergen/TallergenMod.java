package Threxion.tallergen;

import Threxion.tallergen.config.ConfigManager;
import Threxion.tallergen.network.ModNetwork;
import Threxion.tallergen.definition.AllergenDefinitionLoader;
import Threxion.tallergen.definition.ReactionDefinitionLoader;
import Threxion.threxioncore.CoreRegistries;
import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.data.DefinitionLoaderRegistry;
import Threxion.tallergen.scan.AllergenScanProvider;
import com.mojang.logging.LogUtils;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(TallergenMod.MOD_ID)
public class TallergenMod {
    public static final String MOD_ID = "tallergen";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TallergenMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(ModNetwork::onCommonSetup);

        // 必须在任何 GameRules 实例创建（世界加载）之前注册自定义 gamerule。
        // 引用 TallergenGameRules 即触发其类加载，完成 GameRules.register。
        final GameRules.Key<GameRules.BooleanValue> statsDisplayRule = TallergenGameRules.ALLERGY_STATS_DISPLAY;
        if (statsDisplayRule == null) {
            throw new IllegalStateException("[Tallergen] gamerule 注册失败");
        }

        // 首启复制缺省配置文件（allergens/ / reactions/ / blacklist / whitelist / player.json），
        // 然后加载配置。这里只碰 FMLPaths.CONFIGDIR，是安全的。
        ConfigManager.get().init();

        // Register TAllergen Definition loaders before ThrexionCore performs its data load.
        DefinitionLoaderRegistry.register("tallergen:allergen", new AllergenDefinitionLoader());
        DefinitionLoaderRegistry.register("tallergen:reaction", new ReactionDefinitionLoader());

        // Each configured allergen is also a persistent Core Property on ItemStacks.
        CoreRegistries.SCAN_PROVIDERS.register(PropertyId.parse("tallergen:scan"), new AllergenScanProvider());

        Threxion.tallergen.property.AllergenPropertySchemaService.sync();
    }
}
