package Threxion.tvitality.engine;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

/** TV 玩家状态的持久化载体。直接读写只允许 TV 内部使用。 */
public final class TVitalityState {
    private static final String ROOT = "ThrexionTVitality";
    private static final String STAMINA = "stamina";
    private static final String LAST_REAL_TIME = "last_real_time_ms";
    private static final String MEAL_ID = "meal_id";
    private static final String MEAL_EXPIRES = "meal_expires_ms";
    private static final String SNACK_ID = "snack_id";
    private static final String SNACK_EXPIRES = "snack_expires_ms";

    private TVitalityState() {}

    private static CompoundTag root(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        if (!data.contains(ROOT)) data.put(ROOT, new CompoundTag());
        return data.getCompound(ROOT);
    }

    public static boolean initialized(ServerPlayer player) { return root(player).contains(STAMINA); }
    public static int stamina(ServerPlayer player, int fallback) { return root(player).contains(STAMINA) ? root(player).getInt(STAMINA) : fallback; }
    public static void stamina(ServerPlayer player, int value) { root(player).putInt(STAMINA, value); }
    public static long lastRealTime(ServerPlayer player) { return root(player).getLong(LAST_REAL_TIME); }
    public static void lastRealTime(ServerPlayer player, long value) { root(player).putLong(LAST_REAL_TIME, value); }
    public static long recoveryAccumulated(ServerPlayer player, String definitionId) { return root(player).getLong("recovery:" + definitionId); }
    public static void recoveryAccumulated(ServerPlayer player, String definitionId, long value) { root(player).putLong("recovery:" + definitionId, Math.max(0L, value)); }
    public static String foodId(ServerPlayer player, boolean meal) { return root(player).getString(meal ? MEAL_ID : SNACK_ID); }
    public static long foodExpires(ServerPlayer player, boolean meal) { return root(player).getLong(meal ? MEAL_EXPIRES : SNACK_EXPIRES); }

    public static void setFood(ServerPlayer player, boolean meal, String id, long expires) {
        CompoundTag r = root(player);
        if (meal) {
            r.putString(MEAL_ID, id);
            r.putLong(MEAL_EXPIRES, expires);
        } else {
            r.putString(SNACK_ID, id);
            r.putLong(SNACK_EXPIRES, expires);
        }
    }

    public static void clearFood(ServerPlayer player, boolean meal) {
        CompoundTag r = root(player);
        r.remove(meal ? MEAL_ID : SNACK_ID);
        r.remove(meal ? MEAL_EXPIRES : SNACK_EXPIRES);
    }
}
