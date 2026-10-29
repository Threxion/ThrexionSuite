package Threxion.tdiscovery.network;

import Threxion.tdiscovery.engine.DiscoveryService;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public final class DiscoverItemPacket {
    private final String itemId;
    public DiscoverItemPacket(String itemId) { this.itemId = itemId; }
    public DiscoverItemPacket(FriendlyByteBuf buf) { this.itemId = buf.readUtf(256); }
    public void encode(FriendlyByteBuf buf) { buf.writeUtf(itemId, 256); }
    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> { ServerPlayer player = ctx.getSender(); if (player != null) DiscoveryService.discoverItem(player, itemId); });
        ctx.setPacketHandled(true);
    }
}
