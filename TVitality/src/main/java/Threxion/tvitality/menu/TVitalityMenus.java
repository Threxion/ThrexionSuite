package Threxion.tvitality.menu;

import Threxion.tvitality.definition.InjuryDefinition;
import Threxion.tvitality.engine.TVInjuryState;
import Threxion.threxioncore.CoreRegistries;
import Threxion.threxioncore.api.PropertyId;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class TVitalityMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, "tvitality");
    public static final RegistryObject<MenuType<DoctorMenu>> DOCTOR = MENUS.register("doctor", () -> IForgeMenuType.create(DoctorMenu::new));

    private TVitalityMenus() {}

    public static final class DoctorMenu extends AbstractContainerMenu {
        private final SimpleContainer payment = new SimpleContainer(10);
        private final UUID patientId;
        private final List<String> injuryIds;

        public DoctorMenu(int id, Inventory viewerInventory, FriendlyByteBuf buf) {
            this(id, viewerInventory, buf.readUUID(), readInjuries(buf));
        }

        public DoctorMenu(int id, Inventory viewerInventory, UUID patientId, List<String> injuryIds) {
            super(DOCTOR.get(), id);
            this.patientId = patientId;
            this.injuryIds = List.copyOf(injuryIds);
            for (int row = 0; row < 2; row++) for (int col = 0; col < 5; col++)
                addSlot(new Slot(payment, col + row * 5, 79 + col * 18, 66 + row * 18));
            for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++)
                addSlot(new Slot(viewerInventory, col + row * 9 + 9, 8 + col * 18, 108 + row * 18));
            for (int col = 0; col < 9; col++) addSlot(new Slot(viewerInventory, col, 8 + col * 18, 162));
        }

        private static List<String> readInjuries(FriendlyByteBuf buf) {
            int size = buf.readVarInt();
            List<String> ids = new ArrayList<>(Math.max(0, Math.min(size, 4096)));
            for (int i = 0; i < size; i++) ids.add(buf.readUtf(256));
            return ids;
        }

        public UUID patientId() { return patientId; }
        public List<String> injuryIds() { return injuryIds; }
        public SimpleContainer payment() { return payment; }

        @Override public boolean stillValid(Player player) { return player.isAlive(); }

        @Override public boolean clickMenuButton(Player viewer, int id) {
            if (!(viewer instanceof ServerPlayer viewerServer)) return false;
            if (id < 1000) return false;
            int encoded = id - 1000;
            int page = encoded / 2048;
            int mask = encoded % 2048;
            if (mask == 0) return false;

            ServerPlayer patient = viewerServer.server.getPlayerList().getPlayer(patientId);
            if (patient == null) return false;
            List<String> current = TVInjuryState.get(patient);
            int start = page * 10;
            if (start >= current.size()) return false;

            java.util.Map<String, Integer> required = new java.util.LinkedHashMap<>();
            for (int i = 0; i < 10 && start + i < current.size(); i++) {
                if ((mask & (1 << i)) == 0) continue;
                var def = CoreRegistries.DEFINITIONS.get(PropertyId.parse(current.get(start + i))).orElse(null);
                if (def instanceof InjuryDefinition injury && injury.doctorTreatmentEnabled())
                    required.merge(injury.costItem(), injury.costCount(), Integer::sum);
            }
            if (required.isEmpty()) return false;

            for (var entry : required.entrySet()) {
                int found = 0;
                var item = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.tryParse(entry.getKey()));
                for (int i = 0; i < payment.getContainerSize(); i++) {
                    ItemStack stack = payment.getItem(i);
                    if (stack.getItem() == item) found += stack.getCount();
                }
                if (found < entry.getValue()) return false;
            }

            for (var entry : required.entrySet()) {
                int remaining = entry.getValue();
                var item = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.tryParse(entry.getKey()));
                for (int i = 0; i < payment.getContainerSize() && remaining > 0; i++) {
                    ItemStack stack = payment.getItem(i);
                    if (stack.getItem() != item) continue;
                    int take = Math.min(remaining, stack.getCount());
                    stack.shrink(take);
                    remaining -= take;
                    payment.setItem(i, stack);
                }
            }

            // 服务器以玩家当前真实伤口列表为准；GUI 只提交选择，不直接提交伤口结果。
            List<String> selectedIds = new ArrayList<>();
            for (int i = 0; i < 10 && start + i < current.size(); i++) {
                if ((mask & (1 << i)) != 0) selectedIds.add(current.get(start + i));
            }
            TVInjuryState.removeAll(patient, selectedIds);
            return true;
        }

        @Override public void removed(Player viewer) {
            super.removed(viewer);
            if (!viewer.level().isClientSide) {
                for (int i = 0; i < payment.getContainerSize(); i++) {
                    ItemStack stack = payment.removeItemNoUpdate(i);
                    if (!stack.isEmpty()) viewer.drop(stack, false);
                }
            }
        }

        @Override public ItemStack quickMoveStack(Player player, int index) {
            Slot slot = slots.get(index);
            if (!slot.hasItem()) return ItemStack.EMPTY;
            ItemStack source = slot.getItem();
            ItemStack copy = source.copy();
            if (index < 10) {
                if (!moveItemStackTo(source, 10, slots.size(), true)) return ItemStack.EMPTY;
            } else if (!moveItemStackTo(source, 0, 10, false)) return ItemStack.EMPTY;
            if (source.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
            return copy;
        }
    }
}
