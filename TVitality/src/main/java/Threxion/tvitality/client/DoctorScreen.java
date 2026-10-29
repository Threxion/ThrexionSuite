package Threxion.tvitality.client;

import Threxion.tvitality.definition.InjuryDefinition;
import Threxion.tvitality.menu.TVitalityMenus;
import Threxion.threxioncore.CoreRegistries;
import Threxion.threxioncore.api.PropertyId;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Phase 20 医师诊断书 GUI。 */
public final class DoctorScreen extends AbstractContainerScreen<TVitalityMenus.DoctorMenu> {
    private static final ResourceLocation BG = new ResourceLocation("tvitality", "textures/gui/doctor.png");
    private static final int PAGE_SIZE = 10;
    private final boolean[] selected = new boolean[PAGE_SIZE];
    private int page = 0;

    public DoctorScreen(TVitalityMenus.DoctorMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        imageWidth = 176;
        imageHeight = 190;
        inventoryLabelY = 999;
        titleLabelX = 999;
    }

    @Override protected void init() {
        super.init();
    }

    private void confirm() {
        int mask = 0;
        for (int i = 0; i < PAGE_SIZE; i++) if (selected[i]) mask |= (1 << i);
        if (minecraft != null && minecraft.gameMode != null)
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 1000 + page * 2048 + mask);
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);
        double x = mouseX - leftPos;
        double y = mouseY - topPos;
        int pageStart = page * PAGE_SIZE;
        int visible = Math.min(PAGE_SIZE, Math.max(0, menu.injuryIds().size() - pageStart));
        // 诊断书右上方按钮：确认治疗。
        if (x >= 99 && x < 151 && y >= 50 && y < 63) {
            confirm();
            return true;
        }
        // 第一行大框：全部治疗。
        if (x >= 56 && x < 69 && y >= 24 && y < 37) {
            boolean value = !allSelected(visible);
            for (int i = 0; i < visible; i++) selected[i] = value;
            return true;
        }
        // 其余行：每个实际伤口占一行。
        for (int i = 0; i < PAGE_SIZE; i++) {
            if (i >= visible) break;
            int rowY = 32 + i * 7;
            if (x >= 56 && x < 69 && y >= rowY && y < rowY + 8) {
                selected[i] = !selected[i];
                return true;
            }
        }
        // 分页：仅在确实存在下一页/上一页时响应。
        if (x >= 158 && x < 176 && y >= 164 && y < 188 && (page + 1) * PAGE_SIZE < menu.injuryIds().size()) {
            page++;
            clearSelection();
            return true;
        }
        if (x >= 0 && x < 18 && y >= 164 && y < 188 && page > 0) {
            page--;
            clearSelection();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean allSelected(int visible) {
        if (visible <= 0) return false;
        for (int i = 0; i < visible; i++) if (!selected[i]) return false;
        return true;
    }

    private void clearSelection() { for (int i = 0; i < selected.length; i++) selected[i] = false; }

    @Override protected void renderBg(GuiGraphics g, float partial, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, BG);
        g.blit(BG, leftPos, topPos, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
        drawChecks(g);
        drawInjuries(g);
        drawCost(g);
        drawPageArrows(g);
    }

    private void drawChecks(GuiGraphics g) {
        int pageStart = page * PAGE_SIZE;
        int visible = Math.min(PAGE_SIZE, Math.max(0, menu.injuryIds().size() - pageStart));
        if (allSelected(visible)) drawCheck(g, leftPos + 58, topPos + 27);
        for (int i = 0; i < visible; i++) {
            if (!selected[i]) continue;
            drawCheck(g, leftPos + 58, topPos + 34 + i * 7);
        }
    }

    private void drawCheck(GuiGraphics g, int x, int y) {
        g.fill(x + 2, y + 4, x + 4, y + 7, 0xFF35A853);
        g.fill(x + 4, y + 5, x + 6, y + 7, 0xFF35A853);
        g.fill(x + 6, y + 1, x + 8, y + 6, 0xFF35A853);
    }

    private void drawInjuries(GuiGraphics g) {
        g.drawString(font, Component.translatable("gui.tvitality.all"), leftPos + 10, topPos + 25, 0xFF202020, false);
        int pageStart = page * PAGE_SIZE;
        int visible = Math.min(PAGE_SIZE, Math.max(0, menu.injuryIds().size() - pageStart));
        for (int i = 0; i < visible; i++) {
            String id = menu.injuryIds().get(pageStart + i);
            String name = id;
            var def = CoreRegistries.DEFINITIONS.get(PropertyId.parse(id)).orElse(null);
            if (def instanceof InjuryDefinition injury) name = injury.displayName();
            int maxWidth = 50;
            if (font.width(name) > maxWidth) name = font.plainSubstrByWidth(name, maxWidth, true);
            g.drawString(font, name, leftPos + 10, topPos + 32 + i * 7, 0xFF202020, false);
        }
        if (visible == 0 && page == 0) g.drawString(font, "—", leftPos + 10, topPos + 25, 0xFF555555, false);
    }

    private void drawCost(GuiGraphics g) {
        Map<String, Integer> costs = new LinkedHashMap<>();
        int start = page * PAGE_SIZE;
        for (int i = 0; i < PAGE_SIZE; i++) {
            if (!selected[i]) continue;
            int idx = start + i;
            if (idx < 0 || idx >= menu.injuryIds().size()) continue;
            var def = CoreRegistries.DEFINITIONS.get(PropertyId.parse(menu.injuryIds().get(idx))).orElse(null);
            if (def instanceof InjuryDefinition injury && injury.doctorTreatmentEnabled())
                costs.merge(injury.costItem(), injury.costCount(), Integer::sum);
        }
        if (costs.isEmpty()) return;
        int x = leftPos + 80;
        int y = topPos + 47;
        int drawn = 0;
        for (Map.Entry<String,Integer> e : costs.entrySet()) {
            Item item = ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(e.getKey()));
            if (item == null || item == net.minecraft.world.item.Items.AIR) continue;
            ItemStack stack = new ItemStack(item, Math.min(e.getValue(), 99));
            g.renderItem(stack, x + drawn * 22, y);
            g.renderItemDecorations(font, stack, x + drawn * 22, y, String.valueOf(e.getValue()));
            drawn++;
            if (drawn >= 4) break;
        }
    }

    private void drawPageArrows(GuiGraphics g) {
        boolean next = (page + 1) * PAGE_SIZE < menu.injuryIds().size();
        boolean prev = page > 0;
        if (next) {
            int x = leftPos + 162, y = topPos + 169;
            g.fill(x, y + 4, x + 8, y + 6, 0xFF303030);
            g.fill(x + 6, y + 2, x + 9, y + 8, 0xFF303030);
        }
        if (prev) {
            int x = leftPos + 5, y = topPos + 169;
            g.fill(x + 1, y + 4, x + 9, y + 6, 0xFF303030);
            g.fill(x, y + 2, x + 3, y + 8, 0xFF303030);
        }
    }

    @Override protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawCenteredString(font, Component.translatable("gui.tvitality.confirm"), 125, 52, 0xFFFFFFFF);
    }
}
