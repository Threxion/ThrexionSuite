package Threxion.tdiscovery.engine;

import Threxion.tdiscovery.network.DiscoveryNetwork;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;

public final class DiscoveryService {
    private DiscoveryService() {}
    public static boolean discoverHeldItem(ServerPlayer player) {
        if (player == null) return false;
        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) return false;
        ResourceLocation id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
        return discoverItem(player, id.toString());
    }
    public static boolean discoverItem(ServerPlayer player, String itemId) {
        if (player == null || itemId == null || itemId.isBlank()) return false;
        boolean changed = DiscoveryState.discover(player, "item:" + itemId);
        if (changed) DiscoveryNetwork.sendState(player, DiscoveryState.discovered(player));
        return changed;
    }
}
