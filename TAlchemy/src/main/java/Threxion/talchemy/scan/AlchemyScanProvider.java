package Threxion.talchemy.engine;

import Threxion.threxioncore.CoreRegistries;
import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.property.ItemStackPropertyContainer;
import Threxion.threxioncore.api.property.PropertyValue;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * TAlchemy 的按游戏日过期系统。
 *
 * <p>不会注册 Tick，也不会主动扫描背包；只有调用 inspect/applyOnReference 时才计算当前游戏日。</p>
 */
public final class AlchemyExpirationService {
    public static final String META = "ThrexionAlchemy";
    public static final String CREATION_DAY = "CreationDay";
    public static final String EXPIRATION_DAYS = "ExpirationDays";
    public static final String EXPIRED_BEHAVIOR = "ExpiredBehavior";
    public static final String EXPIRED_PROPERTY = "ExpiredPotencyProperty";
    public static final String EXPIRED_AMOUNT = "ExpiredPotencyAmount";
    public static final String EXPIRED_HANDLED = "ExpiredHandled";

    private AlchemyExpirationService() {}

    public static ExpirationStatus inspect(ItemStack stack, Level level) {
        if (stack == null || stack.isEmpty() || level == null || !stack.hasTag())
            return new ExpirationStatus(false, false, 0, Long.MAX_VALUE, -2, "none", false);
        CompoundTag meta = stack.getTag().getCompound(META);
        if (!meta.contains(EXPIRATION_DAYS))
            return new ExpirationStatus(false, false, 0, Long.MAX_VALUE, -2, "none", false);
        int days = meta.getInt(EXPIRATION_DAYS);
        long creation = meta.getLong(CREATION_DAY);
        long expiration = days < 0 ? Long.MAX_VALUE : creation + days;
        long current = Math.floorDiv(level.getDayTime(), 24000L);
        return new ExpirationStatus(true, days >= 0 && current >= expiration, creation, expiration,
                days, meta.getString(EXPIRED_BEHAVIOR), meta.getBoolean(EXPIRED_HANDLED));
    }

    /**
     * 在“引用”药物时执行过期策略。返回 true 表示本次引用允许继续执行药效。
     * invalidate 会阻止使用；add_potency 只执行一次，并把指定药力写回 ItemStack。
     */
    public static boolean applyOnReference(ItemStack stack, Level level) {
        ExpirationStatus status = inspect(stack, level);
        if (!status.configured() || !status.expired()) return true;
        CompoundTag meta = stack.getOrCreateTag().getCompound(META);
        String behavior = status.behavior();
        if ("invalidate".equals(behavior)) return false;
        if ("add_potency".equals(behavior) && !status.handled()) {
            String property = meta.getString(EXPIRED_PROPERTY);
            double amount = meta.getDouble(EXPIRED_AMOUNT);
            if (!property.isBlank() && amount != 0.0) {
                PropertyId id = PropertyId.parse(property);
                ItemStackPropertyContainer properties = new ItemStackPropertyContainer(stack);
                double current = properties.get(id).filter(PropertyValue::isNumeric).map(PropertyValue::asDouble).orElse(0.0);
                double value = current + amount;
                var schema = CoreRegistries.PROPERTIES.schema(id).orElse(null);
                if (schema != null) value = Math.max(schema.getMin(), Math.min(schema.getMax(), value));
                properties.set(id, PropertyValue.ofNumber(value));
            }
            meta.putBoolean(EXPIRED_HANDLED, true);
            stack.getOrCreateTag().put(META, meta);
        }
        return true;
    }
}
