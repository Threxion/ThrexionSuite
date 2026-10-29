package Threxion.tallergen.network;

import Threxion.tallergen.TallergenGameRules;
import Threxion.tallergen.TallergenMod;
import Threxion.tallergen.network.packet.AllergySyncPacket;
import Threxion.tallergen.state.IPlayerAllergyData;
import Threxion.tallergen.state.ModCapabilities;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModNetwork {
    private static SimpleChannel INSTANCE;
    private static int id = 0;
    private static final String PROTOCOL_VERSION = "2";

    private ModNetwork() {
    }

    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(ModNetwork::register);
    }

    private static void register() {
        SimpleChannel channel = NetworkRegistry.newSimpleChannel(
                ResourceLocation.fromNamespaceAndPath(TallergenMod.MOD_ID, "main"),
                () -> PROTOCOL_VERSION,
                PROTOCOL_VERSION::equals,
                PROTOCOL_VERSION::equals
        );
        channel.messageBuilder(AllergySyncPacket.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(AllergySyncPacket::encode)
                .decoder(AllergySyncPacket::new)
                .consumerMainThread(AllergySyncPacket::handle)
                .add();
        INSTANCE = channel;
    }

    /** 同步玩家过敏状态 + 客户端 HUD 的 gamerule 开关值。 */
    public static void sendAllergySync(ServerPlayer player, IPlayerAllergyData data) {
        if (INSTANCE == null || player.connection == null) {
            return;
        }
        boolean display = player.level().getGameRules().getBoolean(TallergenGameRules.ALLERGY_STATS_DISPLAY);
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new AllergySyncPacket(data, display));
    }

    /** 通过 Capability 取数据后同步（用于登录/重生/换维度及 gamerule 变化广播）。 */
    public static void sendAllergySync(ServerPlayer player) {
        if (INSTANCE == null || player.connection == null) {
            return;
        }
        player.getCapability(ModCapabilities.PLAYER_ALLERGY).ifPresent(data -> sendAllergySync(player, data));
    }

    private static int nextId() {
        return id++;
    }
}
