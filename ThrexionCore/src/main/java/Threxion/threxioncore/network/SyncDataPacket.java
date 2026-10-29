package Threxion.threxioncore.network;

import Threxion.threxioncore.CoreRegistries;
import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.data.CoreDefinition;
import Threxion.threxioncore.api.data.Definition;
import Threxion.threxioncore.api.property.PropertyValue;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * 服务端 → 客户端的数据同步包（server authoritative）。
 * 携带全部 Definition 的属性快照；客户端仅接收存储，不能回传修改。
 */
public class SyncDataPacket {

    private final List<Definition> definitions;

    /** 发送端构造：从当前服务端注册表取快照。 */
    public SyncDataPacket() {
        this.definitions = List.copyOf(CoreRegistries.DEFINITIONS.values());
    }

    /** 客户端解码构造。 */
    public SyncDataPacket(FriendlyByteBuf buf) {
        int n = buf.readVarInt();
        List<Definition> defs = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            String defId = buf.readUtf();
            int m = buf.readVarInt();
            CoreDefinition.Builder builder = CoreDefinition.builder(PropertyId.parse(defId));
            for (int j = 0; j < m; j++) {
                PropertyId pid = PropertyId.parse(buf.readUtf());
                PropertyValue value = readValue(buf);
                builder.property(pid, value);
            }
            defs.add(builder.build());
        }
        this.definitions = defs;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(definitions.size());
        for (Definition def : definitions) {
            buf.writeUtf(def.getId().toString());
            var map = def.getProperties().asMap();
            buf.writeVarInt(map.size());
            map.forEach((pid, value) -> {
                buf.writeUtf(pid.toString());
                writeValue(buf, value);
            });
        }
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        ClientPacketHandler.handleSyncData(this)
                )
        );
        ctx.setPacketHandled(true);
        return true;
    }

    public List<Definition> getDefinitions() {
        return definitions;
    }

    private static void writeValue(FriendlyByteBuf buf, PropertyValue value) {
        buf.writeByte(value.getType().ordinal());
        switch (value.getType()) {
            case NUMBER, INT -> buf.writeDouble(value.asDouble());
            case BOOLEAN -> buf.writeBoolean(value.getBoolean());
            case STRING -> buf.writeUtf(value.getString());
        }
    }

    private static PropertyValue readValue(FriendlyByteBuf buf) {
        byte type = buf.readByte();
        return switch (type) {
            case 0 -> PropertyValue.ofNumber(buf.readDouble());
            case 1 -> PropertyValue.ofInt((long) buf.readDouble());
            case 2 -> PropertyValue.ofBoolean(buf.readBoolean());
            case 3 -> PropertyValue.ofString(buf.readUtf());
            default -> PropertyValue.ofNumber(0.0);
        };
    }
}
