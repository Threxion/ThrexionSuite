package Threxion.tvitality.engine;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

/** Phase 20 的最小伤口持久化骨架；严重程度与生命周期算法留待后续阶段。 */
public final class TVInjuryState {
    private static final String KEY = "ThrexionTVInjuries";
    private static final String IDS = "ids";
    private TVInjuryState() {}

    public static List<String> get(ServerPlayer player) {
        CompoundTag root = player.getPersistentData();
        List<String> out = new ArrayList<>();
        if (!root.contains(KEY)) return out;
        ListTag list = root.getList(KEY, 8);
        for (int i = 0; i < list.size(); i++) out.add(list.getString(i));
        return out;
    }

    public static void add(ServerPlayer player, String id) {
        CompoundTag root = player.getPersistentData();
        ListTag list = root.contains(KEY) ? root.getList(KEY, 8) : new ListTag();
        list.add(StringTag.valueOf(id));
        root.put(KEY, list);
    }

    public static void removeSelected(ServerPlayer player, List<String> current, int page, int mask) {
        CompoundTag root = player.getPersistentData();
        ListTag next = new ListTag();
        int start = page * 10;
        for (int i = 0; i < current.size(); i++) {
            int local = i - start;
            boolean selected = local >= 0 && local < 10 && (mask & (1 << local)) != 0;
            if (!selected) next.add(StringTag.valueOf(current.get(i)));
        }
        root.put(KEY, next);
    }

    public static void removeAll(ServerPlayer player, List<String> selectedIds) {
        CompoundTag root = player.getPersistentData();
        if (!root.contains(KEY)) return;
        ListTag old = root.getList(KEY, 8);
        ListTag next = new ListTag();
        for (int i = 0; i < old.size(); i++) {
            String id = old.getString(i);
            if (!selectedIds.contains(id)) next.add(StringTag.valueOf(id));
        }
        root.put(KEY, next);
    }
}
