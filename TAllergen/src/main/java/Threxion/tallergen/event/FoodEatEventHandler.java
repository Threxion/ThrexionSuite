package Threxion.tallergen.event;

import Threxion.tallergen.CakeRegistry;
import Threxion.tallergen.TallergenMod;
import Threxion.tallergen.state.AllergyStateManager;
import Threxion.tallergen.property.AllergenPropertyService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 食用检测 + 玩家 tick。
 *
 * - 常规食物：LivingEntityUseItemEvent.Finish（完成食用动画即"实际食用"）。
 * - 蛋糕类方块（蛋糕等）：不经过物品食用流程，在 RightClickBlock 里做特殊检测。
 *   哪些方块算"蛋糕类"由 {@link CakeRegistry} 决定，默认含 MC 蛋糕，
 *   其他模组可把自家可放置式食用方块（如可放置的南瓜派）注册进去。
 * - 食物来源（合成/战利品/指令/其他 Mod）不影响检测，统一走 Food Allergen Database。
 */
@Mod.EventBusSubscriber(modid = TallergenMod.MOD_ID)
public class FoodEatEventHandler {

    @SubscribeEvent
    public static void onItemFinishedUsing(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ItemStack item = event.getItem();
            if (!item.isEmpty() && item.getItem().isEdible()) {
                AllergenPropertyService.annotate(item);
                AllergyStateManager.handleIngest(player, item);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRightClickCake(PlayerInteractEvent.RightClickBlock event) {
        if (event.isCanceled()) {
            return;
        }
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (event.getLevel().isClientSide) {
            return;
        }
        // 旁观者右键蛋糕不会触发方块 use（吃不到），排除误判
        if (player.isSpectator()) {
            return;
        }
        // 结算一次蛋糕食用：空手 + 能进食 + 目标方块是已注册的蛋糕类方块，才算实际吃了一口
        // （手持蜡烛/打火石会走插蜡烛/吹灭分支，不算食用，避免误判）。
        ItemStack eaten = CakeRegistry.resolveCakeBite(event.getLevel(), event.getPos(), event.getItemStack(), player);
        if (eaten != null) {
            AllergenPropertyService.annotate(eaten);
            AllergyStateManager.handleIngest(player, eaten);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (event.player instanceof ServerPlayer player) {
            AllergyStateManager.tickPlayer(player);
        }
    }
}
