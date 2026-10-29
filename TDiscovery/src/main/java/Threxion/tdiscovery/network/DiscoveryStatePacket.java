package Threxion.tdiscovery.network;

import Threxion.tdiscovery.client.DiscoveryClientState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public final class DiscoveryStatePacket {
    private final Set<String> discovered;
    public DiscoveryStatePacket(Set<String> discovered) { this.discovered = Set.copyOf(discovered); }
    public DiscoveryStatePacket(FriendlyByteBuf buf) {
        int size = buf.readVarInt(); Set<String> set = new HashSet<>();
        for (int i=0;i<size;i++) set.add(buf.readUtf(256));
        this.discovered = Set.copyOf(set);
    }
    public void encode(FriendlyByteBuf buf) { buf.writeVarInt(discovered.size()); for (String id : discovered) buf.writeUtf(id, 256); }
    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> DiscoveryClientState.set(discovered)));
        ctx.setPacketHandled(true);
    }
}
