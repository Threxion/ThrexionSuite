package Threxion.threxioncore.command;

import Threxion.threxioncore.CoreRegistries;
import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.debug.DebugApi;
import Threxion.threxioncore.api.debug.DebugChain;
import Threxion.threxioncore.api.data.Definition;
import Threxion.threxioncore.api.reload.ReloadManager;
import Threxion.threxioncore.config.JsoncLoader;
import Threxion.threxioncore.config.ThrexionConfig;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.item.ItemStack;
import Threxion.threxioncore.api.scan.ScanContext;
import Threxion.threxioncore.api.scan.ScanManager;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * /threxion 调试命令（权限等级 2）：
 *   reload                    —— 热重载配置与数据
 *   config                    —— 显示当前配置
 *   list <category>           —— 列出注册表条目（properties/definitions/modifiers/transfers/integrations）
 *   chain <definition> <prop> —— 展示指定属性的完整链路 Base→Transfer→Modifier→Final
 *   debug on|off              —— 运行时切换 debug（写回 config.json 并重载）
 */
public final class ThrexionCommand {

    private ThrexionCommand() {
    }

    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("scan")
                .requires(src -> src.hasPermission(2))
                .executes(ctx -> doScan(ctx.getSource())));

        dispatcher.register(Commands.literal("threxion")
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("reload")
                        .executes(ctx -> doReload(ctx.getSource())))
                .then(Commands.literal("config")
                        .executes(ctx -> doConfig(ctx.getSource())))
                .then(Commands.literal("list")
                        .then(Commands.literal("properties")
                                .executes(ctx -> doList(ctx.getSource(), "properties")))
                        .then(Commands.literal("definitions")
                                .executes(ctx -> doList(ctx.getSource(), "definitions")))
                        .then(Commands.literal("modifiers")
                                .executes(ctx -> doList(ctx.getSource(), "modifiers")))
                        .then(Commands.literal("transfers")
                                .executes(ctx -> doList(ctx.getSource(), "transfers")))
                        .then(Commands.literal("integrations")
                                .executes(ctx -> doList(ctx.getSource(), "integrations")))
                        .then(Commands.literal("scanproviders")
                                .executes(ctx -> doList(ctx.getSource(), "scanproviders"))))
                .then(Commands.literal("chain")
                        .then(Commands.argument("definition", StringArgumentType.string())
                                .then(Commands.argument("property", StringArgumentType.string())
                                        .executes(ctx -> doChain(ctx.getSource(),
                                                StringArgumentType.getString(ctx, "definition"),
                                                StringArgumentType.getString(ctx, "property"))))))
                .then(Commands.literal("scan")
                        .executes(ctx -> doScan(ctx.getSource())))
                .then(Commands.literal("debug")
                        .then(Commands.literal("on").executes(ctx -> doDebug(ctx.getSource(), true)))
                        .then(Commands.literal("off").executes(ctx -> doDebug(ctx.getSource(), false)))));
    }

    private static int doReload(CommandSourceStack src) {
        send(src, "Reloading ThrexionCore...");
        ReloadManager.reload();
        send(src, "Reload complete.");
        return 1;
    }

    private static int doConfig(CommandSourceStack src) {
        send(src, ThrexionConfig.get().toString());
        return 1;
    }

    private static int doList(CommandSourceStack src, String category) {
        StringBuilder sb = new StringBuilder();
        switch (category) {
            case "properties" -> {
                sb.append("Property schemas (").append(CoreRegistries.PROPERTIES.size()).append("):\n");
                CoreRegistries.PROPERTIES.entries().forEach((id, schema) ->
                        sb.append("  ").append(id).append(" type=").append(schema.getType())
                                .append(" default=").append(schema.getDefaultValue()).append('\n'));
            }
            case "definitions" -> {
                sb.append("Definitions (").append(CoreRegistries.DEFINITIONS.size()).append("):\n");
                CoreRegistries.DEFINITIONS.values().forEach(def ->
                        sb.append("  ").append(def.getId()).append(" props=").append(def.getProperties().size()).append('\n'));
            }
            case "modifiers" -> {
                sb.append("Modifiers (").append(CoreRegistries.MODIFIERS.size()).append("):\n");
                CoreRegistries.MODIFIERS.values().forEach(m ->
                        sb.append("  ").append(m.getId()).append(" -> ").append(m.getTarget())
                                .append(" ").append(m.getOperation()).append(" pri=").append(m.getPriority()).append('\n'));
            }
            case "transfers" -> {
                sb.append("Transfers (").append(CoreRegistries.TRANSFERS.size()).append("):\n");
                CoreRegistries.TRANSFERS.values().forEach(t ->
                        sb.append("  ").append(t.getId()).append(" ").append(t.getSource()).append(" -> ")
                                .append(t.getTarget()).append(" ").append(t.getOperation()).append('\n'));
            }
            case "integrations" -> {
                sb.append("Integrations (").append(CoreRegistries.INTEGRATIONS.size()).append("):\n");
                CoreRegistries.INTEGRATIONS.values().forEach(i ->
                        sb.append("  ").append(i.getId()).append(" available=").append(i.isAvailable()).append('\n'));
            }
            case "scanproviders" -> {
                sb.append("ScanProviders (").append(CoreRegistries.SCAN_PROVIDERS.size()).append("):\n");
                CoreRegistries.SCAN_PROVIDERS.values().forEach(p ->
                        sb.append("  ").append(p.getId()).append(" available=").append(p.isAvailable()).append('\n'));
            }
            default -> {
                send(src, "Unknown category: " + category);
                return 0;
            }
        }
        send(src, sb.toString());
        return 1;
    }

    private static int doChain(CommandSourceStack src, String definitionId, String propertyId) {
        if (!PropertyId.isValid(definitionId)) {
            send(src, "Invalid definition id: " + definitionId);
            return 0;
        }
        if (!PropertyId.isValid(propertyId)) {
            send(src, "Invalid property id: " + propertyId);
            return 0;
        }
        PropertyId defId = PropertyId.parse(definitionId);
        Optional<Definition> defOpt = CoreRegistries.DEFINITIONS.get(defId);
        if (defOpt.isEmpty()) {
            send(src, "Definition not found: " + definitionId);
            return 0;
        }
        DebugChain chain = DebugApi.computeChain(defOpt.get(), PropertyId.parse(propertyId));
        send(src, chain.format());
        return 1;
    }

    private static int doScan(CommandSourceStack src) {
        if (src.getEntity() == null) {
            send(src, "Scan requires an entity source.");
            return 0;
        }
        ItemStack stack = src.getEntity() instanceof net.minecraft.world.entity.player.Player player
                ? player.getMainHandItem() : ItemStack.EMPTY;
        if (stack.isEmpty()) {
            send(src, "Hold an item in your main hand to scan it.");
            return 0;
        }
        var result = ScanManager.scan(new ScanContext(stack).with("level", src.getLevel()));
        if (result.isEmpty()) {
            send(src, "No Threxion properties detected.");
            return 1;
        }
        send(src, result.format());
        return 1;
    }

    private static int doDebug(CommandSourceStack src, boolean on) {
        try {
            Path path = ThrexionConfig.configPath();
            JsonObject obj = JsoncLoader.parseObject(Files.readString(path));
            if (obj == null) {
                obj = new JsonObject();
            }
            obj.addProperty("debug", on);
            if (obj.has("debug_config") && obj.get("debug_config").isJsonObject()) {
                obj.getAsJsonObject("debug_config").addProperty("debug", on);
            } else {
                JsonObject dc = new JsonObject();
                dc.addProperty("debug", on);
                obj.add("debug_config", dc);
            }
            Files.write(path, new GsonBuilder().setPrettyPrinting().create().toJson(obj).getBytes());
            ThrexionConfig.load();
            send(src, "Debug " + (on ? "enabled" : "disabled") + " and config reloaded.");
            return 1;
        } catch (IOException e) {
            send(src, "Failed to update config: " + e.getMessage());
            return 0;
        }
    }

    private static void send(CommandSourceStack src, String message) {
        src.sendSuccess(() -> Component.literal(message), false);
    }
}
