package Threxion.tdiscovery.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import java.util.Set;

public final class DiscoveryNetwork {
    private static final String PROTOCOL = "2";
    private static SimpleChannel CHANNEL;
    private DiscoveryNetwork() {}
    public static void register() {
        CHANNEL = NetworkRegistry.newSimpleChannel(ResourceLocation.fromNamespaceAndPath("tdiscovery", "main"), () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);
        int id=0;
        CHANNEL.messageBuilder(DiscoverHeldItemPacket.class,id++,NetworkDirection.PLAY_TO_SERVER).encoder(DiscoverHeldItemPacket::encode).decoder(DiscoverHeldItemPacket::new).consumerMainThread(DiscoverHeldItemPacket::handle).add();
        CHANNEL.messageBuilder(DiscoverItemPacket.class,id++,NetworkDirection.PLAY_TO_SERVER).encoder(DiscoverItemPacket::encode).decoder(DiscoverItemPacket::new).consumerMainThread(DiscoverItemPacket::handle).add();
        CHANNEL.messageBuilder(RequestDiscoveryStatePacket.class,id++,NetworkDirection.PLAY_TO_SERVER).encoder(RequestDiscoveryStatePacket::encode).decoder(RequestDiscoveryStatePacket::new).consumerMainThread(RequestDiscoveryStatePacket::handle).add();
        CHANNEL.messageBuilder(DiscoveryStatePacket.class,id++,NetworkDirection.PLAY_TO_CLIENT).encoder(DiscoveryStatePacket::encode).decoder(DiscoveryStatePacket::new).consumerMainThread(DiscoveryStatePacket::handle).add();
    }
    public static SimpleChannel channel() { return CHANNEL; }
    public static void sendState(ServerPlayer player, Set<String> state) { CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new DiscoveryStatePacket(state)); }
}
