package Threxion.tdiscovery.network;

import Threxion.tdiscovery.engine.DiscoveryState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public final class RequestDiscoveryStatePacket {
    public RequestDiscoveryStatePacket() {}
    public RequestDiscoveryStatePacket(FriendlyByteBuf ignored) {}
    public void encode(FriendlyByteBuf buf) {}
    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> { ServerPlayer player = ctx.getSender(); if (player != null) DiscoveryNetwork.sendState(player, DiscoveryState.discovered(player)); });
        ctx.setPacketHandled(true);
    }
}
