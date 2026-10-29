package Threxion.tallergen.network.packet;

import Threxion.tallergen.client.ClientPacketHandler;
import Threxion.tallergen.state.IPlayerAllergyData;
import Threxion.tallergen.state.PlayerAllergenState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * 服务端 -> 客户端：同步玩家当前过敏状态。
 * 包含：玩家被分配的过敏原集合（playerAllergens）、各过敏原的 exposure/reaction phase/severity，
 * 以及控制 HUD 是否显示的 gamerule 值（tAllergyStatsDisplay）。
 * 客户端只读展示，不参与判定（完全服务端裁决）。
 */
public class AllergySyncPacket {
    private final boolean statsDisplay;
    private final List<String> playerAllergens;
    private final List<Entry> entries;

    public AllergySyncPacket(IPlayerAllergyData data, boolean statsDisplay) {
        this.statsDisplay = statsDisplay;
        this.playerAllergens = new ArrayList<>();
        for (String id : data.getPlayerAllergens()) {
            var config = Threxion.tallergen.config.ConfigManager.get().getAllergen(id);
            this.playerAllergens.add(config == null || config.displayName == null || config.displayName.isBlank()
                    ? id : config.displayName);
        }
        entries = new ArrayList<>();
        for (PlayerAllergenState s : data.getStates()) {
            var config = Threxion.tallergen.config.ConfigManager.get().getAllergen(s.getAllergenId());
            String displayName = config == null ? s.getAllergenId() : config.displayName;
            entries.add(new Entry(s.getAllergenId(), displayName, s.getExposure(),
                    s.getReaction().getPhase().name(), s.getReaction().getSeverity()));
        }
    }

    public AllergySyncPacket(FriendlyByteBuf buf) {
        this.statsDisplay = buf.readBoolean();
        int allergenCount = buf.readVarInt();
        this.playerAllergens = new ArrayList<>(allergenCount);
        for (int i = 0; i < allergenCount; i++) {
            this.playerAllergens.add(buf.readUtf());
        }
        int size = buf.readVarInt();
        entries = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            entries.add(new Entry(buf.readUtf(), buf.readUtf(), buf.readDouble(), buf.readUtf(), buf.readInt()));
        }
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(this.statsDisplay);
        buf.writeVarInt(this.playerAllergens.size());
        for (String id : this.playerAllergens) {
            buf.writeUtf(id);
        }
        buf.writeVarInt(entries.size());
        for (Entry e : entries) {
            buf.writeUtf(e.allergenId);
            buf.writeUtf(e.displayName);
            buf.writeDouble(e.exposure);
            buf.writeUtf(e.phase);
            buf.writeInt(e.severity);
        }
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.handleSync(this)));
        ctx.get().setPacketHandled(true);
        return true;
    }

    public boolean isStatsDisplay() {
        return statsDisplay;
    }

    public List<String> getPlayerAllergens() {
        return playerAllergens;
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public static class Entry {
        public final String allergenId;
        public final String displayName;
        public final double exposure;
        public final String phase;
        public final int severity;

        public Entry(String allergenId, String displayName, double exposure, String phase, int severity) {
            this.allergenId = allergenId;
            this.displayName = displayName == null || displayName.isBlank() ? allergenId : displayName;
            this.exposure = exposure;
            this.phase = phase;
            this.severity = severity;
        }
    }
}
