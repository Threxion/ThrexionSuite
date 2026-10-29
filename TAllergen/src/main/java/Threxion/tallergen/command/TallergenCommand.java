package Threxion.tallergen.command;

import Threxion.tallergen.TallergenMod;
import Threxion.tallergen.config.AllergenConfig;
import Threxion.tallergen.config.AllergenScope;
import Threxion.tallergen.config.ConfigManager;
import Threxion.tallergen.database.FoodAllergenDatabase;
import Threxion.tallergen.state.AllergyStateManager;
import Threxion.tallergen.state.ModCapabilities;
import Threxion.tallergen.state.PlayerAllergenState;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 管理员命令（权限 2）：
 *  /tallergen scan                         重新加载配置并重新扫描配方、生成过敏原数据库（不需要 reload）
 *  /tallergen inspect [目标]               查看物品的过敏原 / 过敏原关联的物品 / 玩家的过敏状态（缺省查自己）
 *  /tallergen allergen add <allergen> <item>     给过敏原增加一个来源（直接携带者）
 *  /tallergen allergen remove <allergen> <item>  移除过敏原的一个来源
 *  /tallergen allergen list                      列出全部过敏原及其来源
 *  /tallergen player <玩家> add <allergen>       为玩家分配过敏原
 *  /tallergen player <玩家> remove <allergen>    为玩家移除过敏原
 *  /tallergen player <玩家> list                 显示玩家已分配的过敏原
 *  /tallergen blacklist/whitelist add/remove/list <item>  管理 Scanner 过滤规则
 *
 * 修改过敏原来源或黑白名单后会自动重新扫描数据库。
 */
@Mod.EventBusSubscriber(modid = TallergenMod.MOD_ID)
public class TallergenCommand {

    private static final SuggestionProvider<CommandSourceStack> ITEM_SUGGESTIONS =
            (ctx, builder) -> suggestStrings(
                    ForgeRegistries.ITEMS.getKeys().stream().map(ResourceLocation::toString), builder);

    private static final SuggestionProvider<CommandSourceStack> ALLERGEN_SUGGESTIONS =
            (ctx, builder) -> suggestStrings(
                    ConfigManager.get().getAllergens().stream().map(a -> a.id), builder);

    private static final SuggestionProvider<CommandSourceStack> INSPECT_SUGGESTIONS =
            (ctx, builder) -> suggestStrings(
                    Stream.concat(
                            ForgeRegistries.ITEMS.getKeys().stream().map(ResourceLocation::toString),
                            ConfigManager.get().getAllergens().stream().map(a -> a.id)),
                    builder);

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("tallergen")
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("scan").executes(ctx -> scan(ctx.getSource())))
                .then(Commands.literal("inspect")
                        .executes(ctx -> inspectTarget(ctx.getSource(), null))
                        .then(Commands.argument("target", StringArgumentType.greedyString())
                                .suggests(INSPECT_SUGGESTIONS)
                                .executes(ctx -> inspectTarget(ctx.getSource(), StringArgumentType.getString(ctx, "target")))))
                .then(allergenCommands())
                .then(playerCommands())
                .then(listFilterCommands("blacklist", true))
                .then(listFilterCommands("whitelist", false))
        );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> allergenCommands() {
        return Commands.literal("allergen")
                .then(Commands.literal("add")
                        .then(Commands.argument("allergen", StringArgumentType.word())
                                .suggests(ALLERGEN_SUGGESTIONS)
                                .then(Commands.argument("item", ResourceLocationArgument.id())
                                        .suggests(ITEM_SUGGESTIONS)
                                        .executes(ctx -> allergenModify(ctx.getSource(),
                                                StringArgumentType.getString(ctx, "allergen"),
                                                ResourceLocationArgument.getId(ctx, "item"),
                                                true)))))
                .then(Commands.literal("remove")
                        .then(Commands.argument("allergen", StringArgumentType.word())
                                .suggests(ALLERGEN_SUGGESTIONS)
                                .then(Commands.argument("item", ResourceLocationArgument.id())
                                        .suggests(ITEM_SUGGESTIONS)
                                        .executes(ctx -> allergenModify(ctx.getSource(),
                                                StringArgumentType.getString(ctx, "allergen"),
                                                ResourceLocationArgument.getId(ctx, "item"),
                                                false)))))
                .then(Commands.literal("list").executes(ctx -> allergenList(ctx.getSource())));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> playerCommands() {
        return Commands.literal("player")
                .then(Commands.argument("target", EntityArgument.player())
                        .then(Commands.literal("add")
                                .then(Commands.argument("allergen", StringArgumentType.word())
                                        .suggests(ALLERGEN_SUGGESTIONS)
                                        .executes(ctx -> playerModify(ctx, true))))
                        .then(Commands.literal("remove")
                                .then(Commands.argument("allergen", StringArgumentType.word())
                                        .suggests(ALLERGEN_SUGGESTIONS)
                                        .executes(ctx -> playerModify(ctx, false))))
                        .then(Commands.literal("list").executes(ctx -> playerList(ctx))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> listFilterCommands(String name, boolean isBlacklist) {
        return Commands.literal(name)
                .then(Commands.literal("add")
                        .then(Commands.argument("item", ResourceLocationArgument.id())
                                .suggests(ITEM_SUGGESTIONS)
                                .executes(ctx -> listFilterModify(ctx.getSource(), isBlacklist,
                                        ResourceLocationArgument.getId(ctx, "item"), true))))
                .then(Commands.literal("remove")
                        .then(Commands.argument("item", ResourceLocationArgument.id())
                                .suggests(ITEM_SUGGESTIONS)
                                .executes(ctx -> listFilterModify(ctx.getSource(), isBlacklist,
                                        ResourceLocationArgument.getId(ctx, "item"), false))))
                .then(Commands.literal("list").executes(ctx -> listFilterList(ctx.getSource(), isBlacklist)));
    }

    private static int scan(CommandSourceStack source) {
        // 不需要 /tallergen reload：scan 先重载配置再重新扫描并写缓存
        ConfigManager.get().loadAll();
        MinecraftServer server = source.getServer();
        FoodAllergenDatabase db = FoodAllergenDatabase.get();
        db.rebuild(server.getRecipeManager(), server.registryAccess());
        db.saveCache();
        int items = db.getDatabase().size();
        int total = 0;
        for (Set<String> set : db.getDatabase().values()) {
            total += set.size();
        }
        final int finalTotal = total;
        source.sendSuccess(() -> Component.translatable("tallergen.command.scan.done", items, finalTotal), false);
        return 1;
    }

    private static int inspectTarget(CommandSourceStack source, String target) {
        if (target == null || target.trim().isEmpty()) {
            if (source.getEntity() instanceof ServerPlayer p) {
                return inspectPlayer(source, p);
            }
            source.sendFailure(Component.translatable("tallergen.command.inspect.none"));
            return 0;
        }
        // 1) 按物品 id 解析
        ResourceLocation rl = ResourceLocation.tryParse(target.trim());
        if (rl != null) {
            Item item = ForgeRegistries.ITEMS.getValue(rl);
            if (item != null && item != Items.AIR) {
                Set<String> allergens = FoodAllergenDatabase.get().getAllergens(item);
                if (allergens.isEmpty()) {
                    source.sendSuccess(() -> Component.translatable("tallergen.command.inspect.item.none", rl), false);
                } else {
                    source.sendSuccess(() -> Component.translatable("tallergen.command.inspect.item.has", rl, String.join(", ", allergens)), false);
                }
                return 1;
            }
        }
        // 2) 按过敏原 id 解析：列出携带该过敏原的所有物品
        AllergenConfig allergenCfg = ConfigManager.get().getAllergen(target.trim());
        if (allergenCfg != null) {
            List<String> items = new ArrayList<>();
            for (Map.Entry<ResourceLocation, Set<String>> e : FoodAllergenDatabase.get().getDatabase().entrySet()) {
                if (e.getValue().contains(allergenCfg.id)) {
                    items.add(e.getKey().toString());
                }
            }
            if (items.isEmpty()) {
                source.sendSuccess(() -> Component.translatable("tallergen.command.inspect.allergen.none", allergenCfg.id), false);
            } else {
                source.sendSuccess(() -> Component.translatable("tallergen.command.inspect.allergen.has", allergenCfg.id, String.join(", ", items)), false);
            }
            return 1;
        }
        // 3) 按玩家名解析
        ServerPlayer player = source.getServer().getPlayerList().getPlayerByName(target.trim());
        if (player != null) {
            return inspectPlayer(source, player);
        }
        source.sendFailure(Component.translatable("tallergen.command.inspect.notfound", target));
        return 0;
    }

    private static int inspectPlayer(CommandSourceStack source, ServerPlayer player) {
        List<Component> lines = new ArrayList<>();
        player.getCapability(ModCapabilities.PLAYER_ALLERGY).ifPresent(data -> {
            if (!data.getPlayerAllergens().isEmpty()) {
                lines.add(Component.translatable("tallergen.command.inspect.allergen.list", String.join(", ", data.getPlayerAllergens())));
            }
            for (PlayerAllergenState s : data.getStates()) {
                lines.add(Component.translatable("tallergen.command.inspect.player.line",
                        s.getAllergenId(), s.getExposure(),
                        s.getReaction().getPhase().name(), s.getReaction().getSeverity()));
            }
        });
        if (lines.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("tallergen.command.inspect.player.none", player.getScoreboardName()), false);
        } else {
            source.sendSuccess(() -> {
                MutableComponent msg = Component.translatable("tallergen.command.inspect.player.header", player.getScoreboardName());
                for (Component line : lines) {
                    msg.append("\n").append(line);
                }
                return msg;
            }, false);
        }
        return 1;
    }

    private static int allergenModify(CommandSourceStack source, String allergenId, ResourceLocation item, boolean add) {
        AllergenConfig cfg = ConfigManager.get().getAllergen(allergenId);
        if (cfg == null) {
            source.sendFailure(Component.translatable("tallergen.command.allergen.notfound", allergenId));
            return 0;
        }
        boolean ok;
        if (add) {
            if (cfg.foods.contains(item.toString())) {
                source.sendSuccess(() -> Component.translatable("tallergen.command.allergen.already.has", allergenId, item), false);
                return 1;
            }
            ok = ConfigManager.get().addAllergenFood(allergenId, item);
        } else {
            if (!cfg.foods.contains(item.toString())) {
                source.sendFailure(Component.translatable("tallergen.command.allergen.no.source", allergenId, item));
                return 0;
            }
            ok = ConfigManager.get().removeAllergenFood(allergenId, item);
        }
        if (!ok) {
            source.sendFailure(Component.translatable("tallergen.command.allergen.modify.fail"));
            return 0;
        }
        rescan(source);
        String key = add ? "tallergen.command.allergen.added" : "tallergen.command.allergen.removed";
        source.sendSuccess(() -> Component.translatable(key, allergenId, item), false);
        return 1;
    }

    private static int allergenList(CommandSourceStack source) {
        List<AllergenConfig> allergens = ConfigManager.get().getAllergens();
        if (allergens.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("tallergen.command.allergen.list.empty"), false);
            return 1;
        }
        source.sendSuccess(() -> {
            MutableComponent msg = Component.translatable("tallergen.command.allergen.list.header");
            boolean first = true;
            for (AllergenConfig a : allergens) {
                if (!first) {
                    msg.append("\n");
                }
                first = false;
                msg.append(a.id).append(" -> ");
                if (a.foods.isEmpty()) {
                    msg.append(Component.translatable("tallergen.command.no.source"));
                } else {
                    msg.append(String.join(", ", a.foods));
                }
            }
            return msg;
        }, false);
        return 1;
    }

    private static int playerModify(CommandContext<CommandSourceStack> ctx, boolean add) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "target");
        String allergenId = StringArgumentType.getString(ctx, "allergen");
        if (ConfigManager.get().getAllergen(allergenId) == null) {
            ctx.getSource().sendFailure(Component.translatable("tallergen.command.allergen.notfound", allergenId));
            return 0;
        }
        if (add) {
            AllergyStateManager.addAllergen(player, allergenId);
            ctx.getSource().sendSuccess(() -> Component.translatable("tallergen.command.player.added", player.getScoreboardName(), allergenId), false);
        } else {
            AllergyStateManager.removeAllergen(player, allergenId);
            ctx.getSource().sendSuccess(() -> Component.translatable("tallergen.command.player.removed", player.getScoreboardName(), allergenId), false);
        }
        return 1;
    }

    private static int playerList(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "target");
        List<String> lines = new ArrayList<>();
        player.getCapability(ModCapabilities.PLAYER_ALLERGY).ifPresent(data -> {
            for (String id : data.getPlayerAllergens()) {
                lines.add(id);
            }
        });
        if (lines.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.translatable("tallergen.command.player.list.none", player.getScoreboardName()), false);
        } else {
            ctx.getSource().sendSuccess(() -> Component.translatable("tallergen.command.player.list.has", player.getScoreboardName(), String.join(", ", lines)), false);
        }
        return 1;
    }

    private static int listFilterModify(CommandSourceStack source, boolean isBlacklist, ResourceLocation item, boolean add) {
        Component listName = Component.translatable(isBlacklist
                ? "tallergen.command.list.blacklist" : "tallergen.command.list.whitelist");
        AllergenScope scope = ConfigManager.get().getScope();
        Set<ResourceLocation> entries = isBlacklist ? scope.getBlacklist() : scope.getWhitelist();
        boolean present = entries.contains(item);
        if (add && present) {
            source.sendSuccess(() -> Component.translatable("tallergen.command.list.already.has", listName, item), false);
            return 1;
        }
        if (!add && !present) {
            source.sendFailure(Component.translatable("tallergen.command.list.not.contains", listName, item));
            return 0;
        }
        boolean ok = ConfigManager.get().modifyListEntry(isBlacklist, item, add);
        if (!ok) {
            source.sendFailure(Component.translatable("tallergen.command.list.modify.fail", listName));
            return 0;
        }
        rescan(source);
        String key = add ? "tallergen.command.list.added" : "tallergen.command.list.removed";
        source.sendSuccess(() -> Component.translatable(key, listName, item), false);
        return 1;
    }

    private static int listFilterList(CommandSourceStack source, boolean isBlacklist) {
        Component listName = Component.translatable(isBlacklist
                ? "tallergen.command.list.blacklist" : "tallergen.command.list.whitelist");
        AllergenScope scope = ConfigManager.get().getScope();
        Set<ResourceLocation> entries = isBlacklist ? scope.getBlacklist() : scope.getWhitelist();
        boolean enabled = isBlacklist ? scope.isBlacklistEnabled() : scope.isWhitelistEnabled();
        if (entries.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("tallergen.command.list.empty", listName, enabled), false);
        } else {
            String joined = entries.stream().map(ResourceLocation::toString).collect(Collectors.joining(", "));
            source.sendSuccess(() -> Component.translatable("tallergen.command.list.has", listName, enabled, joined), false);
        }
        return 1;
    }

    /** 修改过敏原来源或黑白名单后重新扫描并写缓存。 */
    private static void rescan(CommandSourceStack source) {
        MinecraftServer server = source.getServer();
        FoodAllergenDatabase.get().rebuild(server.getRecipeManager(), server.registryAccess());
        FoodAllergenDatabase.get().saveCache();
    }

    private static CompletableFuture<Suggestions> suggestStrings(Stream<String> values, SuggestionsBuilder builder) {
        String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
        values.filter(s -> s.toLowerCase(Locale.ROOT).contains(remaining)).forEach(builder::suggest);
        return builder.buildFuture();
    }
}
