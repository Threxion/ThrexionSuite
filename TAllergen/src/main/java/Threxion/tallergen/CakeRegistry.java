package Threxion.tallergen;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * 蛋糕类：专门应对"方块式食物"（如蛋糕）的食用检测。
 *
 * 方块式食物不经过物品食用流程（LivingEntityUseItemEvent），只能靠右键方块事件检测。
 * 哪些方块算"蛋糕类"由本注册表决定，默认注册 MC 的蛋糕（普通蛋糕 + 全部蜡烛蛋糕）。
 * 其他模组若也有此类可放置式食用方块（如可放置的南瓜派），可调用 {@link #registerCake(Block)}
 * 或 {@link #registerCake(Block, Item)} 主动接入本模组的过敏检测。
 *
 * TAllergen 不主动探测/兼容其他模组的方块，只提供注册入口，等待其他模组显式接入。
 */
public final class CakeRegistry {

    private static final Map<Block, Item> CAKES = new HashMap<>();

    static {
        // 默认添加 MC 的蛋糕。
        // 蜡烛蛋糕方块没有对应物品（asItem() 返回 AIR），食用时按普通蛋糕物品查过敏原。
        registerCake(Blocks.CAKE, Items.CAKE);
        registerCake(Blocks.CANDLE_CAKE, Items.CAKE);
        registerCake(Blocks.WHITE_CANDLE_CAKE, Items.CAKE);
        registerCake(Blocks.ORANGE_CANDLE_CAKE, Items.CAKE);
        registerCake(Blocks.MAGENTA_CANDLE_CAKE, Items.CAKE);
        registerCake(Blocks.LIGHT_BLUE_CANDLE_CAKE, Items.CAKE);
        registerCake(Blocks.YELLOW_CANDLE_CAKE, Items.CAKE);
        registerCake(Blocks.LIME_CANDLE_CAKE, Items.CAKE);
        registerCake(Blocks.PINK_CANDLE_CAKE, Items.CAKE);
        registerCake(Blocks.GRAY_CANDLE_CAKE, Items.CAKE);
        registerCake(Blocks.LIGHT_GRAY_CANDLE_CAKE, Items.CAKE);
        registerCake(Blocks.CYAN_CANDLE_CAKE, Items.CAKE);
        registerCake(Blocks.PURPLE_CANDLE_CAKE, Items.CAKE);
        registerCake(Blocks.BLUE_CANDLE_CAKE, Items.CAKE);
        registerCake(Blocks.BROWN_CANDLE_CAKE, Items.CAKE);
        registerCake(Blocks.GREEN_CANDLE_CAKE, Items.CAKE);
        registerCake(Blocks.RED_CANDLE_CAKE, Items.CAKE);
        registerCake(Blocks.BLACK_CANDLE_CAKE, Items.CAKE);
    }

    private CakeRegistry() {
    }

    /**
     * 注册可放置式食用方块；食用时按方块对应物品查过敏原。
     *
     * @param block 可放置式食用方块（如可放置的南瓜派）
     */
    public static void registerCake(Block block) {
        registerCake(block, block.asItem());
    }

    /**
     * 注册可放置式食用方块，并显式指定食用时查过敏原所用的食物物品。
     * 方块对应物品不可食用（如蜡烛蛋糕方块没有对应物品）时必须用此重载。
     *
     * @param block    可放置式食用方块
     * @param foodItem 食用时查过敏原所用的食物物品
     */
    public static void registerCake(Block block, Item foodItem) {
        CAKES.put(block, foodItem);
    }

    /** 该 BlockState 是否为已注册的"蛋糕类"可放置式食用方块。 */
    public static boolean isCake(BlockState state) {
        return CAKES.containsKey(state.getBlock());
    }

    /**
     * 该物品是否为已注册蛋糕类方块的对应物品（BlockItem）。
     * 供 Recipe Scanner 判断"经方块交互吃的食物"是否算可食用，从而接收过敏原传播。
     */
    public static boolean isCakeItem(Item item) {
        return CAKES.containsKey(Block.byItem(item));
    }

    /**
     * 结算一次蛋糕食用：仅当空手、玩家可进食且目标方块是已注册的蛋糕类方块时，
     * 返回查过敏原所用的食物 ItemStack（即实际吃到一口），否则返回 null。
     *
     * 空手限定与 MC 蛋糕一致：手持蜡烛/打火石会走插蜡烛/吹灭分支，不算食用。
     *
     * @param level    目标方块所在的世界（服务端）
     * @param pos      目标方块位置
     * @param handItem 右键时手中的物品
     * @param player   右键方块的玩家
     */
    @Nullable
    public static ItemStack resolveCakeBite(Level level, BlockPos pos, ItemStack handItem, Player player) {
        Item foodItem = CAKES.get(level.getBlockState(pos).getBlock());
        if (foodItem == null) {
            return null;
        }
        if (!handItem.isEmpty()) {
            return null;
        }
        if (!player.canEat(false)) {
            return null;
        }
        return new ItemStack(foodItem);
    }
}
