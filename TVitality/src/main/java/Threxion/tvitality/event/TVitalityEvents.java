package Threxion.tvitality.event;

import Threxion.tvitality.TVitalityMod;
import Threxion.tvitality.engine.VitalityService;
import Threxion.tvitality.definition.FoodDefinition;
import Threxion.threxioncore.api.PropertyId;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.resources.ResourceLocation;

/** TV 精力、食物与休息事件入口。 */
@Mod.EventBusSubscriber(modid = TVitalityMod.MOD_ID)
public final class TVitalityEvents {
    private TVitalityEvents() {}

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) VitalityService.settleLogin(player);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // 离线恢复从最后一次真实时间戳继续计算；不在下线瞬间消耗精力。
            Threxion.tvitality.engine.TVitalityState.lastRealTime(player, System.currentTimeMillis());
            VitalityService.clearExternalResting(player);
        }
    }

    @SubscribeEvent
    public static void onTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player instanceof ServerPlayer player) {
            VitalityService.tick(player);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onAttack(AttackEntityEvent event) {
        if (!event.isCanceled() && event.getEntity() instanceof ServerPlayer player) VitalityService.consumeAction(player, "attack");
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBreak(BlockEvent.BreakEvent event) {
        if (!event.isCanceled() && event.getPlayer() instanceof ServerPlayer player) VitalityService.consumeAction(player, "break_block");
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onJump(LivingJumpEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) VitalityService.consumeAction(player, "jump");
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onArrow(ArrowLooseEvent event) {
        if (!event.isCanceled() && event.getEntity() instanceof ServerPlayer player) VitalityService.consumeAction(player, "arrow");
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onHoe(PlayerInteractEvent.RightClickBlock event) {
        if (event.isCanceled() || event.getLevel().isClientSide) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof HoeItem)) return;
        BlockState before = event.getLevel().getBlockState(event.getPos());
        BlockState after = before.getToolModifiedState(new UseOnContext(player, event.getHand(), event.getHitVec()), net.minecraftforge.common.ToolActions.HOE_TILL, false);
        if (after != null && !after.equals(before)) VitalityService.consumeAction(player, "farm");
    }

    @SubscribeEvent
    public static void onFoodFinished(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ItemStack eaten = event.getItem();
        if (eaten.isEmpty()) return;
        ResourceLocation itemId = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(eaten.getItem());
        if (itemId == null) return;
        FoodDefinition food = Threxion.tvitality.engine.VitalityDefinitionIndex.food(itemId.toString());
        if (food != null) VitalityService.applyFood(player, food);
    }
}
