package Threxion.tallergen.state;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Collection;
import java.util.Set;

/**
 * 玩家过敏数据 Capability 接口。数据随玩家 NBT 持久化，服务端裁决，客户端只读同步副本。
 */
@AutoRegisterCapability
public interface IPlayerAllergyData extends INBTSerializable<CompoundTag> {

    Collection<PlayerAllergenState> getStates();

    PlayerAllergenState getOrCreate(String allergenId);

    /** 玩家被分配的过敏原集合（默认空，即默认对任何过敏原不过敏）。 */
    Set<String> getPlayerAllergens();

    boolean hasAllergen(String allergenId);

    void addPlayerAllergen(String allergenId);

    void removePlayerAllergen(String allergenId);

    /** 获取某个过敏原的状态条目；不存在返回 null。 */
    PlayerAllergenState getState(String allergenId);

    /** 移除某个过敏原的状态条目（含其 Exposure / Reaction 数据）。 */
    void removeState(String allergenId);

    /** 是否已经执行过「首次随机分配过敏原」的判定（保证只判定一次，不随每次登录重掷）。 */
    boolean hasRolledRandomAllergen();

    void setRandomAllergenAssigned(boolean assigned);

    void copyFrom(IPlayerAllergyData other);

    /** 数据发生变化，标记需要同步到客户端。 */
    void markChanged();

    /** 每 tick 调用，递减同步冷却。 */
    void tickSyncCooldown();

    /** 冷却结束且有待同步变化时返回 true，并重置冷却。 */
    boolean shouldSync();
}
