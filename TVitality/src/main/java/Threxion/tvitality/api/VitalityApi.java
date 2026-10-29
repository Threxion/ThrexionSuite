package Threxion.tvitality.api;

import Threxion.tvitality.engine.VitalityService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/** TV 对外公开的最小玩家精力/休息 API。 */
public final class VitalityApi {
    private VitalityApi() {}

    public static int getStamina(Player player) { return VitalityService.getStamina(player); }
    public static int getMaxStamina(Player player) { return VitalityService.getMaxStamina(player); }
    public static int setStamina(ServerPlayer player, int value) { return VitalityService.setStamina(player, value); }
    public static int addStamina(ServerPlayer player, int amount) { return VitalityService.addStamina(player, amount); }
    public static int consumeStamina(ServerPlayer player, int amount) { return VitalityService.consumeStamina(player, amount); }
    public static boolean isResting(Player player) { return VitalityService.isResting(player); }
    public static void markExternalResting(Player player, String sourceId, boolean resting) {
        VitalityService.markExternalResting(player, sourceId, resting);
    }
}
