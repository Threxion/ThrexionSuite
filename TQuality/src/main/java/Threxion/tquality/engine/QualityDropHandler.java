package Threxion.tquality.engine;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Vanilla drop integration. Quality is assigned when an ItemEntity is created,
 * so hoppers and other automation do not depend on player pickup events.
 */
@Mod.EventBusSubscriber(modid = "tquality", bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class QualityDropHandler {
    private static final ThreadLocal<Boolean> SPAWNING_SPLIT = ThreadLocal.withInitial(() -> false);

    private QualityDropHandler() {}

    @SubscribeEvent
    public static void onItemEntityJoin(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel)) return;
        if (!(event.getEntity() instanceof ItemEntity entity)) return;
        if (SPAWNING_SPLIT.get()) return;

        ItemStack source = entity.getItem();
        if (!QualityService.hasAssignment(source)) return;

        if (source.getCount() <= 1) {
            QualityService.assignIfNeeded(source);
            return;
        }

        var groups = QualityService.splitByQuality(source);
        if (groups.isEmpty()) return;
        if (groups.size() == 1) {
            entity.setItem(groups.get(0));
            return;
        }

        // Keep one group on the original entity so vanilla pickup/age metadata is preserved.
        entity.setItem(groups.get(0));
        SPAWNING_SPLIT.set(true);
        try {
            for (int i = 1; i < groups.size(); i++) {
                ItemEntity extra = new ItemEntity(
                        event.getLevel(), entity.getX(), entity.getY(), entity.getZ(), groups.get(i));
                extra.setPickUpDelay(entity.getPickUpDelay());
                extra.setDeltaMovement(entity.getDeltaMovement());
                event.getLevel().addFreshEntity(extra);
            }
        } finally {
            SPAWNING_SPLIT.set(false);
        }
    }
}
