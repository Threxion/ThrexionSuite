package Threxion.threxioncore.api.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * 通用炼金效果事件。
 *
 * <p>Core 不依赖 TAlchemy。TAlchemy 通过这个事件把一个“已解析的效果”
 * 广播给其他 T Mod；事件只携带稳定的字符串/数值数据。</p>
 */
public final class AlchemyEffectEvent extends ThrexionEvent {
    public record PropertyModifier(String property, String operation,
                                    double amount, double min, double max) {}

    private final LivingEntity entity;
    private final ItemStack source;
    private final String effectId;
    private final String displayName;
    private final List<PropertyModifier> propertyModifiers;

    public AlchemyEffectEvent(LivingEntity entity, ItemStack source,
                              String effectId, String displayName,
                              List<PropertyModifier> propertyModifiers) {
        super("alchemy_effect");
        this.entity = entity;
        this.source = source;
        this.effectId = effectId;
        this.displayName = displayName == null ? "" : displayName;
        this.propertyModifiers = List.copyOf(propertyModifiers == null ? List.of() : propertyModifiers);
    }

    public LivingEntity getEntity() { return entity; }
    public ItemStack getSource() { return source; }
    public String getEffectId() { return effectId; }
    public String getDisplayName() { return displayName; }
    public List<PropertyModifier> getPropertyModifiers() { return propertyModifiers; }
}
