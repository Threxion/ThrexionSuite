package Threxion.tvitality.event;

import Threxion.tvitality.command.TVitalityCommands;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.InteractionResult;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import net.minecraft.server.level.ServerPlayer;

@Mod.EventBusSubscriber(modid = "tvitality")
public final class DoctorNpcInteraction {
    private DoctorNpcInteraction() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onVillagerInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.isCanceled()) return;
        if (!(event.getTarget() instanceof Villager villager)) return;
        if (villager.getVillagerData().getProfession() != VillagerProfession.LIBRARIAN) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        TVitalityCommands.openDoctor(player, player);
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }
}
