package Threxion.talchemy.engine;
import net.minecraftforge.event.TickEvent; import net.minecraftforge.eventbus.api.SubscribeEvent; import net.minecraftforge.fml.common.Mod;
@Mod.EventBusSubscriber(modid="talchemy",bus=Mod.EventBusSubscriber.Bus.FORGE)
public final class AlchemyTickHandler { private AlchemyTickHandler(){} @SubscribeEvent public static void tick(TickEvent.ServerTickEvent e){if(e.phase==TickEvent.Phase.END)AlchemyService.tickAll();}}
