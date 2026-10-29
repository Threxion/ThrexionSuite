package Threxion.talchemy;

import Threxion.talchemy.definition.AlchemyDefinitionLoader;
import Threxion.talchemy.definition.ProcessDefinitionLoader;
import Threxion.talchemy.definition.AlchemyEffectDefinitionLoader;
import Threxion.talchemy.definition.AlchemyReactionDefinitionLoader;
import Threxion.talchemy.definition.PotencyDefinitionLoader;
import Threxion.talchemy.definition.ProcessTierDefinitionLoader;
import Threxion.talchemy.definition.StationDefinitionLoader;
import Threxion.talchemy.definition.CatalystDefinitionLoader;
import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.data.DefinitionLoaderRegistry;
import Threxion.threxioncore.api.property.PropertySchema;
import Threxion.threxioncore.api.property.PropertyType;
import Threxion.talchemy.scan.AlchemyScanProvider;
import Threxion.threxioncore.CoreRegistries;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.level.GameRules;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import net.minecraftforge.fml.loading.FMLPaths;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Mod(TAlchemyMod.MOD_ID)
public final class TAlchemyMod {
    public static final String MOD_ID = "talchemy";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    public static final RegistryObject<Item> HERBAL_POWDER = ITEMS.register("herbal_powder",
            () -> new Item(new Item.Properties().stacksTo(64)));
    public static final RegistryObject<Item> ALCHEMY_POTION = ITEMS.register("alchemy_potion",
            () -> new PotionItem(new Item.Properties().stacksTo(1)));

    /** 过期输入参与炼金后，成品是否立即过期；默认开启。 */
    public static final GameRules.Key<GameRules.BooleanValue> EXPIRED_INPUT_IMMEDIATE_EXPIRATION =
            GameRules.register("talchemyExpiredInputImmediateExpiration", GameRules.Category.MISC,
                    GameRules.BooleanValue.create(true));

    public TAlchemyMod() {
        ITEMS.register(net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus());
        DefinitionLoaderRegistry.register("talchemy:process", new ProcessDefinitionLoader());
        DefinitionLoaderRegistry.register("talchemy:recipe", new AlchemyDefinitionLoader());
        DefinitionLoaderRegistry.register("talchemy:effect", new AlchemyEffectDefinitionLoader());
        DefinitionLoaderRegistry.register("talchemy:reaction", new AlchemyReactionDefinitionLoader());
        DefinitionLoaderRegistry.register("talchemy:potency", new PotencyDefinitionLoader());
        DefinitionLoaderRegistry.register("talchemy:process_tier", new ProcessTierDefinitionLoader());
        DefinitionLoaderRegistry.register("talchemy:station", new StationDefinitionLoader());
        DefinitionLoaderRegistry.register("talchemy:catalyst", new CatalystDefinitionLoader());
        CoreRegistries.SCAN_PROVIDERS.register(PropertyId.parse("talchemy:scan"), new AlchemyScanProvider());
        copyDefaults();
        CoreRegistries.PROPERTIES.register(PropertyId.parse("talchemy:process_tier"),
                PropertySchema.builder(PropertyId.parse("talchemy:process_tier"))
                        .type(PropertyType.STRING).defaultValue("unknown").modifiable(false).persistent(true)
                        .description("产生该物品时使用的炼金工艺等级")
                        .build());
        CoreRegistries.PROPERTIES.register(PropertyId.parse("talchemy:toxicity"),
                PropertySchema.builder(PropertyId.parse("talchemy:toxicity"))
                        .type(PropertyType.NUMBER).defaultValue(0).modifiable(true).persistent(true)
                        .description("TAlchemy 毒性；数值越高代表负面毒性越强，具体阈值由整合包定义")
                        .build());
    }

    private static void copyDefaults() {
        String[] files = {"processes/grinding.json", "processes/distillation.json", "processes/infusion.json",
                "recipes/wheat_powder.json", "recipes/wheat_infusion.json", "recipes/wheat_refined.json",
                "effects/healing_1.json", "effects/healing_2.json", "effects/healing_3.json", "effects/anti_wheat.json",
                "reactions/heal_1.json", "reactions/heal_2.json", "reactions/heal_3.json", "reactions/anti_allergy_1.json",
                "potencies/heal.json", "potencies/anti_allergy.json",
                "properties/talchemy_heal.json", "properties/talchemy_anti_allergy.json",
                "tiers/wilderness.json", "tiers/traveler.json", "tiers/pioneer.json", "tiers/workshop.json", "tiers/overspec.json", "catalysts/salt.json"};
        for (String file : files) {
            Path target = file.startsWith("properties/")
                    ? FMLPaths.CONFIGDIR.get().resolve("threxioncore").resolve(file)
                    : FMLPaths.CONFIGDIR.get().resolve("threxioncore").resolve(MOD_ID).resolve(file);
            if (Files.exists(target)) continue;
            try (InputStream in = TAlchemyMod.class.getResourceAsStream("/config_defaults/talchemy/" + file)) {
                if (in == null) continue;
                Files.createDirectories(target.getParent());
                Files.copy(in, target);
            } catch (IOException e) {
                LOGGER.error("Failed to create TAlchemy default config {}", target, e);
            }
        }
    }
}
