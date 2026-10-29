package Threxion.tvitality.command;

import Threxion.tvitality.engine.TVInjuryState;
import Threxion.tvitality.menu.TVitalityMenus;
import Threxion.tvitality.definition.InjuryDefinition;
import Threxion.threxioncore.CoreRegistries;
import Threxion.threxioncore.api.PropertyId;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkHooks;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = "tvitality")
public final class TVitalityCommands {
    private TVitalityCommands() {}

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("tv")
                .requires(s -> s.hasPermission(2))
                .then(Commands.literal("doctor")
                        .executes(ctx -> openDoctor(ctx.getSource().getPlayerOrException(), ctx.getSource().getPlayerOrException())))
                .then(Commands.literal("check")
                        .then(Commands.argument("player", GameProfileArgument.gameProfile())
                                .executes(ctx -> check(ctx.getSource(), GameProfileArgument.getGameProfiles(ctx, "player")))))
                .then(Commands.literal("injury")
                        .then(Commands.literal("add")
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .executes(ctx -> addInjury(ctx.getSource().getPlayerOrException(), StringArgumentType.getString(ctx, "id")))))));
    }

    private static int check(CommandSourceStack source, Collection<com.mojang.authlib.GameProfile> profiles) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer viewer = source.getPlayerOrException();
        if (profiles.isEmpty()) return 0;
        com.mojang.authlib.GameProfile profile = profiles.iterator().next();
        ServerPlayer target = viewer.server.getPlayerList().getPlayer(profile.getId());
        if (target == null) {
            source.sendFailure(Component.translatable("command.tvitality.player_offline"));
            return 0;
        }
        openDoctor(viewer, target);
        return 1;
    }

    public static void openDoctor(ServerPlayer viewer, ServerPlayer patient) {
        List<String> injuries = TVInjuryState.get(patient);
        NetworkHooks.openScreen(viewer, new net.minecraft.world.MenuProvider() {
            @Override public Component getDisplayName() { return Component.translatable("container.tvitality.doctor"); }
            @Override public net.minecraft.world.inventory.AbstractContainerMenu createMenu(int id, net.minecraft.world.entity.player.Inventory inv, net.minecraft.world.entity.player.Player p) {
                return new TVitalityMenus.DoctorMenu(id, inv, patient.getUUID(), injuries);
            }
        }, buf -> {
            buf.writeUUID(patient.getUUID());
            buf.writeVarInt(injuries.size());
            for (String id : injuries) buf.writeUtf(id);
        });
    }

    private static int addInjury(ServerPlayer player, String raw) {
        String id = raw.contains(":") ? raw : "tvitality:" + raw;
        if (!(CoreRegistries.DEFINITIONS.get(PropertyId.parse(id)).orElse(null) instanceof InjuryDefinition)) {
            player.sendSystemMessage(Component.translatable("command.tvitality.injury.unknown", id));
            return 0;
        }
        TVInjuryState.add(player, id);
        player.sendSystemMessage(Component.translatable("command.tvitality.injury.added", id));
        return 1;
    }
}
