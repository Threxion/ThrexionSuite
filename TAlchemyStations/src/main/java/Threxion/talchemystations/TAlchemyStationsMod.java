package Threxion.talchemystations;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.fml.loading.FMLPaths;
import java.io.*;
import java.nio.file.*;

@Mod(TAlchemyStationsMod.MOD_ID)
public final class TAlchemyStationsMod {
    public static final String MOD_ID="talchemystations";
    public static final DeferredRegister<Block> BLOCKS=DeferredRegister.create(ForgeRegistries.BLOCKS,MOD_ID);
    public static final DeferredRegister<Item> ITEMS=DeferredRegister.create(ForgeRegistries.ITEMS,MOD_ID);
    public static final RegistryObject<Block> MORTAR=BLOCKS.register("mortar",()->new MortarBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(1.5F)));
    public static final RegistryObject<Item> MORTAR_ITEM=ITEMS.register("mortar",()->new BlockItem(MORTAR.get(),new Item.Properties()));
    public TAlchemyStationsMod(){
        var bus=FMLJavaModLoadingContext.get().getModEventBus();
        BLOCKS.register(bus); ITEMS.register(bus); TAlchemyStationsBlockEntities.BLOCK_ENTITIES.register(bus); TAlchemyStationsMenus.MENUS.register(bus);
        bus.addListener(this::clientSetup); copyDefaults();
    }
    private void clientSetup(final FMLClientSetupEvent event){ event.enqueueWork(() -> MenuScreens.register(TAlchemyStationsMenus.MORTAR.get(), MortarScreen::new)); }
    private static void copyDefaults(){ Path p=FMLPaths.CONFIGDIR.get().resolve("threxioncore/talchemy/stations/mortar.json"); if(Files.exists(p))return; try(InputStream in=TAlchemyStationsMod.class.getResourceAsStream("/config_defaults/talchemy/stations/mortar.json")){ if(in!=null){Files.createDirectories(p.getParent());Files.copy(in,p);} }catch(IOException ignored){} }
}
