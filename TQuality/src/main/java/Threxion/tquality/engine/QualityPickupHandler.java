package Threxion.tquality.engine;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import Threxion.threxioncore.api.event.ItemProductionEvent;
import net.minecraftforge.fml.common.Mod;

/** TQuality 的非掉落产物入口。拾取不再负责随机品质，避免自动化路径与玩家拾取产生不同结果。 */
@Mod.EventBusSubscriber(modid="tquality", bus=Mod.EventBusSubscriber.Bus.FORGE)
public final class QualityPickupHandler {
    private QualityPickupHandler() {}

    @SubscribeEvent
    public static void onCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (event.getEntity() instanceof ServerPlayer)
            QualityService.assignIfNeeded(event.getCrafting());
    }

    @SubscribeEvent
    public static void onProduced(ItemProductionEvent event) {
        QualityService.assignIfNeeded(event.getStack());
    }
}
