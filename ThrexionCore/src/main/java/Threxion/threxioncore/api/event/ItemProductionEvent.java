package Threxion.threxioncore.api.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nullable;

/**
 * 当 Threxion 系列 Mod 产生一个新的 ItemStack 时派发。
 * 用于 TQuality/TAllergen 等下游 Mod 在不互相依赖的情况下处理新产物。
 */
public final class ItemProductionEvent extends ThrexionEvent {
    private final ItemStack stack;
    private final String cause;
    private final ServerPlayer player;

    public ItemProductionEvent(ItemStack stack, String cause, @Nullable ServerPlayer player) {
        super("item_production");
        this.stack = stack;
        this.cause = cause == null ? "unknown" : cause;
        this.player = player;
    }

    public ItemStack getStack() { return stack; }
    public String getCause() { return cause; }
    @Nullable public ServerPlayer getPlayer() { return player; }
}
