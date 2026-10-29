package Threxion.talchemystations;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class TAlchemyStationsMenus {
    private TAlchemyStationsMenus() {}
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, TAlchemyStationsMod.MOD_ID);
    public static final RegistryObject<MenuType<MortarMenu>> MORTAR = MENUS.register("mortar",
            () -> IForgeMenuType.create(MortarMenu::new));

    public static final class MortarMenu extends AbstractContainerMenu {
        private final MortarBlockEntity station;
        private final BlockPos pos;

        public MortarMenu(int id, Inventory inv, FriendlyByteBuf buf) {
            this(id, inv, getStation(inv, buf));
        }

        public MortarMenu(int id, Inventory inv, MortarBlockEntity station) {
            super(MORTAR.get(), id);
            this.station = station;
            this.pos = station.getBlockPos();
            addSlot(new Slot(station, 0, 20, 35));       // 材料输入
            addSlot(new Slot(station, 1, 50, 35));       // 催化剂
            addSlot(new Slot(station, 2, 80, 35));       // 容器输入
            addSlot(new OutputSlot(station, 3, 125, 35)); // 主产物
            addSlot(new OutputSlot(station, 4, 125, 58)); // 副产物

            // 与原版熔炉菜单一样，把服务器端加工进度同步到客户端 GUI。
            addDataSlot(new DataSlot() {
                private int clientValue;
                @Override public int get() {
                    return playerLevelIsClient(inv) ? clientValue : Threxion.talchemy.engine.AlchemyService.getProgress(pos);
                }
                @Override public void set(int value) { clientValue = value; }
            });
            addDataSlot(new DataSlot() {
                private int clientValue;
                @Override public int get() {
                    return playerLevelIsClient(inv) ? clientValue : Threxion.talchemy.engine.AlchemyService.getProgressTotal(pos);
                }
                @Override public void set(int value) { clientValue = value; }
            });
            for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++)
                addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            for (int col = 0; col < 9; col++) addSlot(new Slot(inv, col, 8 + col * 18, 142));
        }

        private static MortarBlockEntity getStation(Inventory inv, FriendlyByteBuf buf) {
            BlockPos p = buf.readBlockPos();
            var be = inv.player.level().getBlockEntity(p);
            if (!(be instanceof MortarBlockEntity mortar)) throw new IllegalStateException("Mortar station missing at " + p);
            return mortar;
        }

        private static boolean playerLevelIsClient(Inventory inv) {
            return inv.player.level().isClientSide;
        }

        public MortarBlockEntity station() { return station; }
        public BlockPos pos() { return pos; }

        @Override public boolean stillValid(Player player) {
            return player.level().getBlockEntity(pos) == station && player.distanceToSqr(pos.getX()+0.5,pos.getY()+0.5,pos.getZ()+0.5) <= 64.0;
        }

        @Override public boolean clickMenuButton(Player player, int id) {
            if (id == 0 && player.level().getBlockEntity(pos) == station && player instanceof net.minecraft.server.level.ServerPlayer sp) {
                return Threxion.talchemy.engine.AlchemyService.start(sp, station);
            }
            return false;
        }

        @Override public ItemStack quickMoveStack(Player player, int index) {
            Slot slot = slots.get(index);
            if (!slot.hasItem()) return ItemStack.EMPTY;
            ItemStack source = slot.getItem();
            ItemStack copy = source.copy();
            if (index < 5) {
                if (!moveItemStackTo(source, 5, slots.size(), true)) return ItemStack.EMPTY;
            } else if (!moveItemStackTo(source, 0, 1, false) && !moveItemStackTo(source, 1, 2, false)
                    && !moveItemStackTo(source, 2, 3, false)) {
                return ItemStack.EMPTY;
            }
            if (source.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
            return copy;
        }

        private static final class OutputSlot extends Slot {
            OutputSlot(net.minecraft.world.Container c, int i, int x, int y) { super(c,i,x,y); }
            @Override public boolean mayPlace(ItemStack stack) { return false; }
        }
    }
}
