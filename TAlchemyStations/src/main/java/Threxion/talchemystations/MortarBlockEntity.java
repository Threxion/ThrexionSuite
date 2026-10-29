package Threxion.talchemystations;

import Threxion.talchemy.api.IAlchemyStation;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.Containers;

public class MortarBlockEntity extends BlockEntity implements IAlchemyStation, Container, MenuProvider {
    private final ItemStack[] items = new ItemStack[]{ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY};

    public MortarBlockEntity(BlockPos p, BlockState s) { super(TAlchemyStationsBlockEntities.MORTAR.get(), p, s); }
    @Override public String stationId() { return "talchemystations:mortar"; }
    @Override public String blockId() { return "talchemystations:mortar"; }
    @Override public BlockPos pos() { return getBlockPos(); }
    @Override public ItemStack getInput() { return items[0]; }
    @Override public void setInput(ItemStack s) { setItem(0, s); }
    @Override public ItemStack getCatalyst() { return items[1]; }
    @Override public void setCatalyst(ItemStack s) { setItem(1, s); }
    @Override public ItemStack getContainerInput() { return items[2]; }
    @Override public void setContainerInput(ItemStack s) { setItem(2, s); }
    @Override public ItemStack getOutput() { return items[3]; }
    @Override public void setOutput(ItemStack s) { setItem(3, s); }
    @Override public ItemStack getByproduct() { return items[4]; }
    @Override public void setByproduct(ItemStack s) { setItem(4, s); }
    @Override public boolean isUsable() { return !isRemoved() && level != null; }

    /** 研钵采用类似原版熔炉的固定 8 秒加工时间：160 tick。 */
    @Override public int processingTimeTicks() { return 160; }

    @Override public int getContainerSize() { return 5; }
    @Override public boolean isEmpty() { return java.util.Arrays.stream(items).allMatch(ItemStack::isEmpty); }
    @Override public ItemStack getItem(int slot) { return items[slot]; }
    @Override public ItemStack removeItem(int slot, int amount) {
        ItemStack result = net.minecraft.world.ContainerHelper.removeItem(items, slot, amount);
        if (!result.isEmpty()) setChanged();
        return result;
    }
    @Override public ItemStack removeItemNoUpdate(int slot) {
        ItemStack result = items[slot]; items[slot] = ItemStack.EMPTY; return result;
    }
    @Override public void setItem(int slot, ItemStack stack) {
        items[slot] = stack == null ? ItemStack.EMPTY : stack;
        if ((slot == 3 || slot == 4) && items[slot].getCount() > getMaxStackSize()) items[slot].setCount(getMaxStackSize());
        setChanged();
    }
    @Override public boolean stillValid(Player player) { return !isRemoved() && player.distanceToSqr(worldPosition.getX()+0.5,worldPosition.getY()+0.5,worldPosition.getZ()+0.5) <= 64.0; }
    @Override public void clearContent() { for (int i=0;i<items.length;i++) items[i]=ItemStack.EMPTY; setChanged(); }
    @Override public int getMaxStackSize() { return 64; }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) { return slot != 3 && slot != 4; }
    @Override public Component getDisplayName() { return Component.translatable("block.talchemystations.mortar"); }
    @Override public AbstractContainerMenu createMenu(int id, net.minecraft.world.entity.player.Inventory inv, Player player) { return new TAlchemyStationsMenus.MortarMenu(id, inv, this); }

    @Override protected void saveAdditional(CompoundTag t) {
        super.saveAdditional(t);
        net.minecraft.world.ContainerHelper.saveAllItems(t, java.util.Arrays.asList(items));
    }
    @Override public void load(CompoundTag t) {
        super.load(t);
        java.util.List<ItemStack> loaded = new java.util.ArrayList<>(5);
        net.minecraft.world.ContainerHelper.loadAllItems(t, loaded);
        for (int i=0;i<items.length;i++) items[i] = i < loaded.size() && loaded.get(i)!=null ? loaded.get(i) : ItemStack.EMPTY;
    }

    public void dropContents(Level l, BlockPos p) { for (ItemStack s: items) Containers.dropItemStack(l,p.getX(),p.getY(),p.getZ(),s); }
}
