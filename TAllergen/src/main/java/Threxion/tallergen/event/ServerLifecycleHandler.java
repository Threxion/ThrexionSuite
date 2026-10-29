package Threxion.tallergen.event;

import Threxion.tallergen.TallergenMod;
import Threxion.tallergen.config.AllergenConfig;
import Threxion.tallergen.config.ConfigManager;
import Threxion.tallergen.config.OptionConfig;
import Threxion.tallergen.config.PlayerConfig;
import Threxion.tallergen.database.FoodAllergenDatabase;
import Threxion.tallergen.network.ModNetwork;
import Threxion.tallergen.property.AllergenPropertySchemaService;
import Threxion.threxioncore.api.event.DefinitionReloadEvent;
import Threxion.tallergen.state.ModCapabilities;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * 服务端生命周期：
 *  - 启动时读取数据库缓存（没有缓存才重新扫描）。
 *  - 登录/重生/换维度时向客户端同步过敏状态。
 */
@Mod.EventBusSubscriber(modid = TallergenMod.MOD_ID)
public class ServerLifecycleHandler {

    @SubscribeEvent
    public static void onDefinitionReload(DefinitionReloadEvent event) {
        ConfigManager.get().loadAll();
        AllergenPropertySchemaService.sync();
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        // 每次启动重读配置（管理员可能在游戏外改过 json）
        ConfigManager.get().loadAll();
        MinecraftServer server = event.getServer();
        boolean loaded = FoodAllergenDatabase.get().loadCache();
        if (!loaded) {
            FoodAllergenDatabase.get().rebuild(server.getRecipeManager(), server.registryAccess());
            FoodAllergenDatabase.get().saveCache();
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            assignRandomAllergenIfEnabled(player);
            syncPlayer(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            syncPlayer(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            syncPlayer(player);
        }
    }

    private static void syncPlayer(ServerPlayer player) {
        ModNetwork.sendAllergySync(player);
    }

    /**
     * 首次登录时按配置随机分配过敏原（每个玩家只判定一次）。
     * 玩家默认无过敏原；配置 player.json 的 random_allergen_enabled 关闭后不分配。
     *
     * 分配流程（配置项均在 option.json 与各过敏原 JSON 中可调）：
     *  1. 先按 option.json 的 allergen_assignment_chance（默认 15%）判定该玩家是否进入分配。
     *  2. 命中后逐个过敏原按其 initial_allergy_chance（默认 5%）独立判定是否命中，命中即分配；
     *     总数封顶 max_allergens（默认 1），不足 min_allergens（默认 0）时从未命中者中强制补足。
     */
    private static void assignRandomAllergenIfEnabled(ServerPlayer player) {
        PlayerConfig pc = ConfigManager.get().getPlayerConfig();
        if (!pc.randomAllergenEnabled) {
            return;
        }
        player.getCapability(ModCapabilities.PLAYER_ALLERGY).ifPresent(data -> {
            if (data.hasRolledRandomAllergen()) {
                return;
            }
            data.setRandomAllergenAssigned(true);
            OptionConfig opt = ConfigManager.get().getOptionConfig();
            List<AllergenConfig> allergens = ConfigManager.get().getAllergens();
            if (allergens.isEmpty()) {
                data.markChanged();
                return;
            }
            if (player.getRandom().nextDouble() >= opt.allergenAssignmentChance) {
                data.markChanged();
                return;
            }
            int min = Math.max(0, opt.minAllergens);
            int max = Math.min(allergens.size(), Math.max(min, opt.maxAllergens));
            List<AllergenConfig> candidates = new ArrayList<>(allergens);
            Collections.shuffle(candidates, new Random(player.getRandom().nextLong()));
            int assigned = 0;
            for (AllergenConfig a : candidates) {
                if (assigned >= max) {
                    break;
                }
                if (player.getRandom().nextDouble() < a.initialAllergyChance) {
                    data.addPlayerAllergen(a.id);
                    assigned++;
                    TallergenMod.LOGGER.info("[Tallergen] 玩家 {} 随机分配到过敏原 {}", player.getScoreboardName(), a.id);
                }
            }
            if (assigned < min) {
                for (AllergenConfig a : candidates) {
                    if (assigned >= min) {
                        break;
                    }
                    if (!data.hasAllergen(a.id)) {
                        data.addPlayerAllergen(a.id);
                        assigned++;
                        TallergenMod.LOGGER.info("[Tallergen] 玩家 {} 随机分配到过敏原 {}", player.getScoreboardName(), a.id);
                    }
                }
            }
            data.markChanged();
        });
    }
}
