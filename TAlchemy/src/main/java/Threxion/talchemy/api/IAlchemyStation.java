package Threxion.talchemy.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

/** TAlchemy 工作方块接口。具体工作方块由附属模组实现，TAlchemy 不依赖具体方块。 */
public interface IAlchemyStation {
    String stationId();
    String blockId();
    BlockPos pos();
    ItemStack getInput();
    void setInput(ItemStack stack);
    ItemStack getCatalyst();
    void setCatalyst(ItemStack stack);
    ItemStack getContainerInput();
    void setContainerInput(ItemStack stack);
    ItemStack getOutput();
    void setOutput(ItemStack stack);
    ItemStack getByproduct();
    void setByproduct(ItemStack stack);
    boolean isUsable();

    /**
     * 工作方块自己的处理时长（tick）。-1 表示完全使用 TAlchemy Process 的时长。
     * 具体附属模组可以用它让某个工作方块拥有类似熔炉的固定加工时间。
     */
    default int processingTimeTicks() { return -1; }

    /** 工作方块自己的加工时间倍率；与 Recipe 的 time_multiplier 相乘。 */
    default double processingTimeMultiplier() { return 1.0; }
}
