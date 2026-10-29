package Threxion.threxioncore.api.property;

import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

/**
 * PropertyValue ↔ NBT 的持久化适配层（PropertyContainer → PropertyValue →
 * Persistence Adapter → NBT）。
 *
 * <p>编码规则：NUMBER → DoubleTag；INT → LongTag；BOOLEAN → ByteTag(0/1)；
 * STRING → StringTag。解码时按 Tag 具体类型还原，因此 ItemStack 属性
 * 数据的 NBT 键直接写 "tallergen:wheat" = 0.8 这类字面值即可，无需套额外的
 * 类型包装对象。</p>
 */
public final class PropertyValueCodec {

    private PropertyValueCodec() {
    }

    /** 把 PropertyValue 编码成 NBT Tag；null 编码为 0.0。 */
    public static Tag toTag(PropertyValue value) {
        if (value == null) {
            return DoubleTag.valueOf(0.0);
        }
        return switch (value.getType()) {
            case INT -> LongTag.valueOf(value.getInt());
            case BOOLEAN -> ByteTag.valueOf(value.getBoolean());
            case STRING -> StringTag.valueOf(value.getString());
            case NUMBER -> DoubleTag.valueOf(value.getNumber());
        };
    }

    /** 把 NBT Tag 解码成 PropertyValue；不支持的 Tag 类型返回 null。 */
    public static PropertyValue fromTag(Tag tag) {
        if (tag == null) {
            return null;
        }
        if (tag instanceof StringTag) {
            return PropertyValue.ofString(tag.getAsString());
        }
        if (tag instanceof ByteTag) {
            return PropertyValue.ofBoolean(((ByteTag) tag).getAsByte() != 0);
        }
        if (tag instanceof IntTag) {
            return PropertyValue.ofInt(((IntTag) tag).getAsInt());
        }
        if (tag instanceof LongTag) {
            return PropertyValue.ofInt(((LongTag) tag).getAsLong());
        }
        if (tag instanceof FloatTag) {
            return PropertyValue.ofNumber(((FloatTag) tag).getAsDouble());
        }
        if (tag instanceof DoubleTag) {
            return PropertyValue.ofNumber(((DoubleTag) tag).getAsDouble());
        }
        if (tag instanceof NumericTag) {
            return PropertyValue.ofNumber(((NumericTag) tag).getAsDouble());
        }
        return null;
    }
}
