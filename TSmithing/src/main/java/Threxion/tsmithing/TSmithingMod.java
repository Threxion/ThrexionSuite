package Threxion.tsmithing;

import Threxion.threxioncore.CoreRegistries;
import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.data.DefinitionLoaderRegistry;
import Threxion.threxioncore.api.property.PropertySchema;
import Threxion.threxioncore.api.property.PropertyType;
import Threxion.tsmithing.definition.FormationDefinitionLoader;
import Threxion.tsmithing.definition.MaterialDefinitionLoader;
import Threxion.tsmithing.definition.RepairPackDefinitionLoader;
import Threxion.tsmithing.item.RepairPackItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Mod(TSmithingMod.MOD_ID)
public final class TSmithingMod {
    public static final String MOD_ID = "tsmithing";
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    public static final RegistryObject<Item> WILDERNESS_REPAIR_PACK = ITEMS.register("wilderness_repair_pack", () -> new RepairPackItem(new Item.Properties().stacksTo(1).durability(100), "tsmithing:wilderness_repair_pack"));
    public static final RegistryObject<Item> TRAVELER_REPAIR_PACK = ITEMS.register("traveler_repair_pack", () -> new RepairPackItem(new Item.Properties().stacksTo(1).durability(100), "tsmithing:traveler_repair_pack"));
    public static final RegistryObject<Item> WORKSHOP_REPAIR_PACK = ITEMS.register("workshop_repair_pack", () -> new RepairPackItem(new Item.Properties().stacksTo(1).durability(100), "tsmithing:workshop_repair_pack"));
    public TSmithingMod() {
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        DefinitionLoaderRegistry.register("tsmithing:material", new MaterialDefinitionLoader());
        DefinitionLoaderRegistry.register("tsmithing:formation", new FormationDefinitionLoader());
        DefinitionLoaderRegistry.register("tsmithing:repair_pack", new RepairPackDefinitionLoader());
        registerProperty("tsmithing:durability", "材料最大总可用耐久");
        registerProperty("tsmithing:softness", "护甲制作使用的柔软度");
        registerProperty("tsmithing:toughness", "护甲制作使用的韧性");
        registerProperty("tsmithing:hardness", "护甲制作使用的硬度");
        registerProperty("tsmithing:weight", "装备/手持物重量");
        registerProperty("tsmithing:current_durability", "工具/武器当前可用耐久；最大可用值为 durability/3");
        registerProperty("tsmithing:armor_durability", "护甲当前耐久百分比，范围0..100");
        registerProperty("tsmithing:temporary_protection", "护甲临时耐久百分比；显示为+N% 临时耐久，只受伤害消耗");
        registerProperty("tsmithing:repair_cost_multiplier", "维修材料消耗倍率；越高越耗材");
        copyDefaults();
    }
    private static void copyDefaults() {
        copyDefault("materials/iron.json");
        copyDefault("materials/wood.json");
        copyDefault("formations/iron_sword.json");
        copyDefault("formations/iron_helmet.json");
        copyDefault("formations/iron_chestplate.json");
        copyDefault("formations/iron_leggings.json");
        copyDefault("formations/iron_boots.json");
        copyDefault("repair_packs/wilderness.json");
        copyDefault("repair_packs/traveler.json");
        copyDefault("repair_packs/workshop.json");
        copyDefault("repair_packs/brick_over_spec_example.json");
    }
    private static void copyDefault(String relative) {
        Path target = FMLPaths.CONFIGDIR.get().resolve("threxioncore").resolve(MOD_ID).resolve(relative);
        if (Files.exists(target)) return;
        try (InputStream in = TSmithingMod.class.getResourceAsStream("/config_defaults/tsmithing/" + relative)) {
            if (in == null) return;
            Files.createDirectories(target.getParent());
            Files.copy(in, target);
        } catch (IOException ignored) { }
    }
    private static void registerProperty(String id, String description) {
        PropertyId pid = PropertyId.parse(id);
        CoreRegistries.PROPERTIES.register(pid, PropertySchema.builder(pid)
                .type(PropertyType.NUMBER).defaultValue(0).modifiable(false).persistent(true)
                .description(description).build());
    }
}
