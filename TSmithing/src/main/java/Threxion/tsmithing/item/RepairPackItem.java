package Threxion.tsmithing.item;

import Threxion.threxioncore.CoreRegistries;
import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.property.ItemStackPropertyContainer;
import Threxion.tsmithing.definition.RepairPackDefinition;
import Threxion.tsmithing.engine.RepairService;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.level.Level;

/** Repair pack. Activation intentionally uses vanilla item-use timing until a custom animation exists. */
public final class RepairPackItem extends Item {
    private static final PropertyId DURABILITY = PropertyId.parse("tsmithing:durability");
    private static final PropertyId CURRENT_DURABILITY = PropertyId.parse("tsmithing:current_durability");
    private static final PropertyId ARMOR_DURABILITY = PropertyId.parse("tsmithing:armor_durability");
    private static final PropertyId TEMPORARY_PROTECTION = PropertyId.parse("tsmithing:temporary_protection");
    private final PropertyId definitionId;

    public RepairPackItem(Properties properties, String definitionId) {
        super(properties);
        this.definitionId = PropertyId.parse(definitionId);
    }

    private RepairPackDefinition definition() {
        return CoreRegistries.DEFINITIONS.get(definitionId)
                .filter(RepairPackDefinition.class::isInstance)
                .map(RepairPackDefinition.class::cast).orElse(null);
    }

    @Override public int getUseDuration(ItemStack stack) { return 32; }

    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack pack = player.getItemInHand(hand);
        if (definition() == null) return InteractionResultHolder.fail(pack);
        return InteractionResultHolder.consume(pack);
    }

    @Override public ItemStack finishUsingItem(ItemStack stack, Level level, net.minecraft.world.entity.LivingEntity entity) {
        if (!(entity instanceof Player player) || level.isClientSide) return stack;
        RepairPackDefinition def = definition();
        if (def == null) return stack;

        // Armor: repair the first damaged armor piece. No Tick-based repair logic is used.
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack armor = player.getItemBySlot(slot);
            if (!(armor.getItem() instanceof ArmorItem)) continue;
            ItemStackPropertyContainer props = new ItemStackPropertyContainer(armor);
            if (!props.has(ARMOR_DURABILITY)) continue;
            double current = props.getNumber(ARMOR_DURABILITY, 0.0);
            double temporary = props.getNumber(TEMPORARY_PROTECTION, 0.0);
            if (current >= 100.0 && (!def.overSpecArmor() || def.repairAmount() <= 0)) continue;
            double repair = def.repairAmount();
            double cost = RepairService.armorRepairCost(repair, def.repairCostMultiplier());
            int damage = (int) cost;
            if (damage <= 0 || damage > stack.getMaxDamage() - stack.getDamageValue()) return stack;
            double newTemp = RepairService.armorRepairResult(current, temporary, repair, def.overSpecArmor());
            props.set(ARMOR_DURABILITY, Threxion.threxioncore.api.property.PropertyValue.ofNumber(Math.min(100.0, current + repair)));
            if (newTemp > 0) props.set(TEMPORARY_PROTECTION, Threxion.threxioncore.api.property.PropertyValue.ofNumber(newTemp));
            stack.hurtAndBreak(damage, player, p -> p.broadcastBreakEvent(player.getUsedItemHand()));
            return stack;
        }

        // Tool/weapon: target the offhand item, matching the agreed temporary control scheme.
        ItemStack target = player.getOffhandItem();
        if (!target.isEmpty()) {
            ItemStackPropertyContainer props = new ItemStackPropertyContainer(target);
            if (props.has(DURABILITY)) {
                double max = props.getNumber(DURABILITY, 0.0);
                long usableMax = (long) Math.floor(max / 3.0);
                long current = props.has(CURRENT_DURABILITY) ? (long) props.getNumber(CURRENT_DURABILITY, usableMax) : usableMax;
                long repair = Math.min((long) Math.floor(def.repairAmount()), Math.max(0L, usableMax - current));
                long cost = RepairService.toolRepairCost(repair, def.repairCostMultiplier());
                if (repair > 0 && cost > 0 && cost <= stack.getMaxDamage() - stack.getDamageValue()) {
                    props.set(CURRENT_DURABILITY, Threxion.threxioncore.api.property.PropertyValue.ofNumber(current + repair));
                    stack.hurtAndBreak((int) cost, player, p -> p.broadcastBreakEvent(player.getUsedItemHand()));
                }
            }
        }
        return stack;
    }
}
