package Threxion.tvitality.client;

import Threxion.tvitality.menu.TVitalityMenus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = "tvitality", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class TVitalityClient {
    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> MenuScreens.register(TVitalityMenus.DOCTOR.get(), DoctorScreen::new));
    }
}
