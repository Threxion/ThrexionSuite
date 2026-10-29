package Threxion.tdiscovery.engine;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ServerPlayer;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public final class DiscoveryState {
    private static final String ROOT = "ThrexionDiscovery";
    private static final String DISCOVERED = "Discovered";
    private static final String STATUS = "Status";
    private DiscoveryState() {}

    public static boolean discover(ServerPlayer player, String entryId) {
        if (player == null || entryId == null || entryId.isBlank()) return false;
        CompoundTag root = player.getPersistentData().getCompound(ROOT);
        ListTag list = root.getList(DISCOVERED, 8);
        for (int i = 0; i < list.size(); i++) if (entryId.equals(list.getString(i))) return false;
        list.add(StringTag.valueOf(entryId));
        root.put(DISCOVERED, list);
        CompoundTag statuses = root.getCompound(STATUS);
        statuses.putString(entryId, DiscoveryStatus.DISCOVERED.name());
        root.put(STATUS, statuses);
        player.getPersistentData().put(ROOT, root);
        return true;
    }

    public static boolean setStatus(ServerPlayer player, String entryId, DiscoveryStatus status) {
        if (player == null || entryId == null || status == null || !hasDiscovered(player, entryId)) return false;
        CompoundTag root = player.getPersistentData().getCompound(ROOT);
        CompoundTag statuses = root.getCompound(STATUS);
        statuses.putString(entryId, status.name());
        root.put(STATUS, statuses);
        player.getPersistentData().put(ROOT, root);
        return true;
    }

    public static DiscoveryStatus status(ServerPlayer player, String entryId) {
        if (!hasDiscovered(player, entryId)) return null;
        String raw = player.getPersistentData().getCompound(ROOT).getCompound(STATUS).getString(entryId);
        try { return DiscoveryStatus.valueOf(raw); } catch (IllegalArgumentException ignored) { return DiscoveryStatus.DISCOVERED; }
    }

    public static boolean hasDiscovered(ServerPlayer player, String entryId) {
        return discovered(player).contains(entryId);
    }

    public static Set<String> discovered(ServerPlayer player) {
        Set<String> out = new LinkedHashSet<>();
        if (player == null) return out;
        CompoundTag root = player.getPersistentData().getCompound(ROOT);
        ListTag list = root.getList(DISCOVERED, 8);
        for (int i = 0; i < list.size(); i++) out.add(list.getString(i));
        return out;
    }

    public static Map<String, DiscoveryStatus> statuses(ServerPlayer player) {
        Map<String, DiscoveryStatus> out = new LinkedHashMap<>();
        for (String id : discovered(player)) {
            DiscoveryStatus s = status(player, id);
            if (s != null) out.put(id, s);
        }
        return out;
    }

    public static int count(ServerPlayer player) { return discovered(player).size(); }
}
