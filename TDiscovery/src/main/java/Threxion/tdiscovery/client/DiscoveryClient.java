package Threxion.tdiscovery.client;

import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid="tdiscovery", value=net.minecraftforge.api.distmarker.Dist.CLIENT, bus=Mod.EventBusSubscriber.Bus.MOD)
public final class DiscoveryClient {
    public static final KeyMapping OPEN = new KeyMapping("key.tdiscovery.open", net.minecraft.client.InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, "key.categories.tdiscovery");
    private DiscoveryClient() {}
    @SubscribeEvent public static void registerKeys(RegisterKeyMappingsEvent e) { e.register(OPEN); }

    @Mod.EventBusSubscriber(modid="tdiscovery", value=net.minecraftforge.api.distmarker.Dist.CLIENT)
    public static final class Gameplay {
        @SubscribeEvent public static void key(net.minecraftforge.client.event.InputEvent.Key e) {
            if (OPEN.consumeClick()) net.minecraft.client.Minecraft.getInstance().setScreen(new TDiscoveryScreen());
        }
        @SubscribeEvent public static void inventoryButton(ScreenEvent.Init.Post e) {
            if (!(e.getScreen() instanceof InventoryScreen)) return;
            Screen s = e.getScreen();
            int x = s.width / 2 - 86;
            int y = s.height / 2 - 78;
            e.addListener(Button.builder(net.minecraft.network.chat.Component.literal("TD"), b -> net.minecraft.client.Minecraft.getInstance().setScreen(new TDiscoveryScreen()))
                    .bounds(x, y, 20, 20).build());
        }
        @SubscribeEvent public static void hideRecipeBook(ScreenEvent.Init.Post e) {
            e.getListenersList().removeIf(w -> w.getClass().getSimpleName().toLowerCase(java.util.Locale.ROOT).contains("recipebutton"));
        }
    }
}
