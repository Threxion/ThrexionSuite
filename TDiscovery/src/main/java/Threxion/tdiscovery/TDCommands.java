package Threxion.tdiscovery;

import Threxion.tdiscovery.engine.DiscoveryService;
import Threxion.tdiscovery.engine.DiscoveryState;
import Threxion.tdiscovery.engine.DiscoveryStatus;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=TDiscoveryMod.MOD_ID, bus=Mod.EventBusSubscriber.Bus.FORGE)
public final class TDCommands {
    private TDCommands() {}
    @SubscribeEvent public static void register(RegisterCommandsEvent e) {
        e.getDispatcher().register(Commands.literal("tdiscovery")
                .requires(s -> s.hasPermission(2))
                .then(Commands.literal("discover").executes(c -> {
                    var p = c.getSource().getPlayerOrException();
                    boolean changed = DiscoveryService.discoverHeldItem(p);
                    c.getSource().sendSuccess(() -> Component.literal(changed ? "已记录手中物品。" : "没有产生新的发现。"), false);
                    return changed ? 1 : 0;
                }))
                .then(Commands.literal("count").executes(c -> {
                    var p = c.getSource().getPlayerOrException();
                    int n = DiscoveryState.count(p);
                    c.getSource().sendSuccess(() -> Component.literal("已发现：" + n), false);
                    return n;
                }))
                .then(Commands.literal("status").executes(c -> {
                    var p = c.getSource().getPlayerOrException();
                    var map = DiscoveryState.statuses(p);
                    c.getSource().sendSuccess(() -> Component.literal(map.toString()), false);
                    return map.size();
                })));
    }
}
