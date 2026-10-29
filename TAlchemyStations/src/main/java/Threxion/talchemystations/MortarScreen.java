package Threxion.talchemystations;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class MortarScreen extends AbstractContainerScreen<TAlchemyStationsMenus.MortarMenu> {
    private static final ResourceLocation BG = new ResourceLocation("talchemystations", "textures/gui/mortar.png");
    public MortarScreen(TAlchemyStationsMenus.MortarMenu menu, Inventory inv, Component title){ super(menu,inv,title); imageWidth=176; imageHeight=166; }
    @Override protected void init(){
        super.init();
        addRenderableWidget(new net.minecraft.client.gui.components.Button(leftPos+68, topPos+58, 40, 20, Component.translatable("gui.talchemystations.start"), b -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId,0)));
    }

    @Override protected void renderBg(GuiGraphics g,float partial,int mx,int my){
        RenderSystem.setShaderTexture(0,BG);
        g.blit(BG,leftPos,topPos,0,0,imageWidth,imageHeight);

        // 原版熔炉式箭头：根据服务器同步的 tick 进度从左向右填充。
        int total = menu.getData().get(1);
        int progress = menu.getData().get(0);
        if (total > 0 && progress > 0) {
            int width = Math.min(24, Math.max(0, (progress * 24) / total));
            drawProgressArrow(g, leftPos + 96, topPos + 36, width);
        }
    }

    private void drawProgressArrow(GuiGraphics g, int x, int y, int width) {
        // 24x17 的像素箭头，结构与原版炉子进度箭头相似。
        if (width <= 0) return;
        int body = Math.min(width, 18);
        g.fill(x, y + 6, x + body, y + 11, 0xFFFFFFFF);
        if (width > 18) {
            int tip = width - 18;
            g.fill(x + 18, y + 4, x + 18 + Math.min(tip, 3), y + 13, 0xFFFFFFFF);
            g.fill(x + 18, y + 2, x + 18 + Math.min(tip, 2), y + 15, 0xFFFFFFFF);
        }
    }
    @Override protected void renderLabels(GuiGraphics g,int mx,int my){ g.drawString(font,title,8,6,0x404040,false); g.drawString(font,playerInventoryTitle,8,72,0x404040,false); }
}
