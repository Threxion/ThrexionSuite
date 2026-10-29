package Threxion.tallergen.event;

import Threxion.tallergen.TallergenMod;
import Threxion.tallergen.property.AllergenPropertyService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import Threxion.threxioncore.api.event.ItemProductionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** 将 Recipe Scanner 结果写入实际 ItemStack，使后续 TCore Transfer 可以跨 Mod 传递。 */
@Mod.EventBusSubscriber(modid = TallergenMod.MOD_ID)
public final class AllergenItemPropertyHandler {
    private AllergenItemPropertyHandler() {}

    @SubscribeEvent
    public static void onCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer)) return;
        ItemStack stack = event.getCrafting();
        AllergenPropertyService.annotate(stack);
    }

    @SubscribeEvent
    public static void onPickup(EntityItemPickupEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer)) return;
        ItemStack stack = event.getItem().getItem();
        AllergenPropertyService.annotate(stack);
    }

    @SubscribeEvent
    public static void onProduced(ItemProductionEvent event) {
        AllergenPropertyService.annotate(event.getStack());
    }
}
