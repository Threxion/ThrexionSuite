package Threxion.tdiscovery.client;

import Threxion.tdiscovery.definition.DiscoveryEntryDefinition;
import Threxion.tdiscovery.network.DiscoveryNetwork;
import Threxion.tdiscovery.network.DiscoverItemPacket;
import Threxion.tdiscovery.network.RequestDiscoveryStatePacket;
import Threxion.threxioncore.CoreRegistries;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import java.util.ArrayList;
import java.util.List;

public final class TDiscoveryScreen extends Screen {
    private enum Page { CODEX, SUBMIT, DETAIL }
    private Page page = Page.CODEX;
    private DiscoveryEntryDefinition selected;
    private final List<DiscoveryEntryDefinition> entries = new ArrayList<>();
    private final List<ItemStack> submitStacks = new ArrayList<>();

    public TDiscoveryScreen() { super(Component.translatable("screen.tdiscovery.codex")); }
    @Override protected void init() {
        refreshEntries();
        DiscoveryNetwork.channel().sendToServer(new RequestDiscoveryStatePacket());
        rebuild();
    }
    private void rebuild() {
        clearWidgets();
        int left = width / 2 - 150;
        if (page == Page.CODEX) {
            int y = 45;
            for (DiscoveryEntryDefinition d : entries) {
                String id = d.getId().getString();
                boolean known = DiscoveryClientState.has(id);
                String label = known ? d.displayName() : "???";
                addRenderableWidget(Button.builder(Component.literal(label), b -> { if (known) { selected=d; page=Page.DETAIL; rebuild(); } })
                        .bounds(left, y, 300, 20).build());
                y += 23; if (y > height - 65) break;
            }
            addRenderableWidget(Button.builder(Component.translatable("button.tdiscovery.submit"), b -> { page=Page.SUBMIT; rebuild(); })
                    .bounds(left, height - 45, 145, 20).build());
            addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> onClose()).bounds(left+155,height-45,145,20).build());
        } else if (page == Page.SUBMIT) {
            refreshSubmitStacks();
            int y=45;
            for (ItemStack stack : submitStacks) {
                String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
                addRenderableWidget(Button.builder(Component.literal(stack.getHoverName().getString()+" ×"+stack.getCount()), b -> {
                    DiscoveryNetwork.channel().sendToServer(new DiscoverItemPacket(id));
                    DiscoveryClientState.add("item:"+id);
                }).bounds(left,y,300,20).build());
                y+=23; if(y>height-65) break;
            }
            addRenderableWidget(Button.builder(Component.translatable("button.tdiscovery.back_codex"), b -> { page=Page.CODEX; rebuild(); }).bounds(left,height-45,145,20).build());
            addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> onClose()).bounds(left+155,height-45,145,20).build());
        } else {
            if (selected != null) {
                addRenderableWidget(Button.builder(Component.translatable("button.tdiscovery.back_codex"), b -> { page=Page.CODEX; rebuild(); }).bounds(left,height-45,300,20).build());
            }
        }
    }
    private void refreshEntries() {
        entries.clear();
        for (var def : CoreRegistries.DEFINITIONS.values()) if (def instanceof DiscoveryEntryDefinition d) entries.add(d);
    }
    private void refreshSubmitStacks() {
        submitStacks.clear();
        if (minecraft == null || minecraft.player == null) return;
        java.util.LinkedHashMap<String,ItemStack> groups=new java.util.LinkedHashMap<>();
        for(ItemStack s:minecraft.player.getInventory().items){
            if(s.isEmpty()) continue;
            String id=BuiltInRegistries.ITEM.getKey(s.getItem()).toString();
            String entry="item:"+id;
            boolean known=DiscoveryClientState.has(entry);
            boolean definable=entries.stream().anyMatch(d -> d.getId().getString().equals(entry));
            if(!known && definable){ ItemStack g=groups.get(id); if(g==null) groups.put(id,s.copy()); else g.grow(s.getCount()); }
        }
        submitStacks.addAll(groups.values());
    }
    @Override public void render(GuiGraphics g,int mouseX,int mouseY,float partialTick){
        renderBackground(g);
        g.drawCenteredString(font, page==Page.CODEX?Component.translatable("screen.tdiscovery.codex"):page==Page.SUBMIT?Component.translatable("screen.tdiscovery.submit"):selected==null?title:selected.displayName(), width/2,20,0xFFFFFF);
        if(page==Page.DETAIL && selected!=null){
            g.drawCenteredString(font, Component.literal(selected.category()), width/2,48,0xAAAAAA);
            g.drawWordWrap(font, Component.literal(selected.description()), width/2-145,70,290,0xFFFFFF);
        } else if(page==Page.SUBMIT && submitStacks.isEmpty()) g.drawCenteredString(font,Component.translatable("screen.tdiscovery.no_submit"),width/2,80,0xAAAAAA);
        super.render(g,mouseX,mouseY,partialTick);
    }
    @Override public boolean isPauseScreen(){return false;}
}
