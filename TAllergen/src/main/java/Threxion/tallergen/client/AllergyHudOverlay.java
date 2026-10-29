package Threxion.tallergen.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import Threxion.tallergen.TallergenMod;

import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 左上角过敏状态 HUD：展示客户端同步到的过敏原分配、Exposure / Reaction 阶段 / Severity。
 * 显示与否受 gamerule tAllergyStatsDisplay（服务端裁决，随同步包下发）控制。
 * 仅客户端加载（value = Dist.CLIENT）。
 */
@Mod.EventBusSubscriber(modid = TallergenMod.MOD_ID, value = Dist.CLIENT)
public class AllergyHudOverlay {

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        if (Minecraft.getInstance().player == null) {
            return;
        }
        if (!ClientAllergyData.isStatsDisplay()) {
            return;
        }
        List<ClientAllergenState> states = ClientAllergyData.getStates();
        Set<String> playerAllergens = ClientAllergyData.getPlayerAllergens();
        if (states.isEmpty() && playerAllergens.isEmpty()) {
            return;
        }
        GuiGraphics gui = event.getGuiGraphics();
        int y = 10;
        if (!playerAllergens.isEmpty()) {
            Component allergens = Component.translatable("tallergen.hud.allergens", String.join(", ", playerAllergens));
            gui.drawString(Minecraft.getInstance().font, allergens, 4, y, 0xFFFFFFAA);
            y += 10;
        }
        for (ClientAllergenState s : states) {
            Component line = Component.translatable("tallergen.hud.line", s.displayName, s.exposure,
                    phaseComponent(s.phase), s.severity);
            gui.drawString(Minecraft.getInstance().font, line, 4, y, 0xFFFFFFFF);
            y += 10;
        }
    }

    /** 把 Reaction 阶段名转成可翻译的文案（NONE/LATENT/ACTIVE/RECOVERY）。 */
    private static Component phaseComponent(String phase) {
        return Component.translatable("tallergen.hud.phase." + phase.toLowerCase(Locale.ROOT));
    }
}
