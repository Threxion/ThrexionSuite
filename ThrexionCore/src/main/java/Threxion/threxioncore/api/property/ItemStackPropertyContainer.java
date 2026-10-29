package Threxion.threxioncore.api.property;

import Threxion.threxioncore.api.PropertyId;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * 以 ItemStack NBT 为后端的可写属性容器（TAllergen 等下游 Mod 最常用的形态）。
 *
 * <p>数据布局（根键 {@link #NBT_KEY}）：</p>
 * <pre>
 * ItemStack.tag."ThrexionProperties" = CompoundTag {
 *     "tallergen:wheat"  = 0.8      (DoubleTag)
 *     "talchemy:potency" = 2        (LongTag)
 *     "tquality:grade"   = "A"      (StringTag)
 * }
 * </pre>
 *
 * <p>键使用属性 id（namespace:id），因此三个 T Mod 互不相识也能共享同一份数据；
 * 读写走 {@link PropertyValueCodec}（Persistence Adapter），容器本身不直接操作 NBT 细节。</p>
 */
public final class ItemStackPropertyContainer implements MutablePropertyContainer {

    /** 属性数据在 ItemStack NBT 中的根键名。 */
    public static final String NBT_KEY = "ThrexionProperties";

    private final ItemStack stack;

    public ItemStackPropertyContainer(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public boolean has(PropertyId id) {
        if (id == null) {
            return false;
        }
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getCompound(NBT_KEY).contains(id.getString());
    }

    @Override
    public Optional<PropertyValue> get(PropertyId id) {
        if (id == null) {
            return Optional.empty();
        }
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return Optional.empty();
        }
        CompoundTag properties = tag.getCompound(NBT_KEY);
        if (!properties.contains(id.getString())) {
            return Optional.empty();
        }
        PropertyValue value = PropertyValueCodec.fromTag(properties.get(id.getString()));
        return value == null ? Optional.empty() : Optional.of(value);
    }

    @Override
    public PropertyValue getOrDefault(PropertyId id) {
        return get(id).orElse(PropertyValue.ofNumber(0.0));
    }

    /** Numeric zero means "property absent"; zero is never persisted as a property. */
    @Override
    public void set(PropertyId id, PropertyValue value) {
        if (id == null || value == null) {
            return;
        }
        if (value.isNumeric() && value.asDouble() == 0.0) {
            remove(id);
            return;
        }
        CompoundTag tag = stack.getOrCreateTag();
        CompoundTag properties = tag.getCompound(NBT_KEY);
        properties.put(id.getString(), PropertyValueCodec.toTag(value));
        tag.put(NBT_KEY, properties);
        stack.setTag(tag);
    }

    @Override
    public void remove(PropertyId id) {
        if (id == null) {
            return;
        }
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return;
        }
        CompoundTag properties = tag.getCompound(NBT_KEY);
        if (properties.contains(id.getString())) {
            properties.remove(id.getString());
            tag.put(NBT_KEY, properties);
            stack.setTag(tag);
        }
    }

    @Override
    public Set<PropertyId> getPropertyIds() {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return Set.of();
        }
        CompoundTag properties = tag.getCompound(NBT_KEY);
        Set<PropertyId> ids = new HashSet<>();
        for (String key : properties.getAllKeys()) {
            if (PropertyId.isValid(key)) {
                ids.add(PropertyId.parse(key));
            }
        }
        return Set.copyOf(ids);
    }
}
