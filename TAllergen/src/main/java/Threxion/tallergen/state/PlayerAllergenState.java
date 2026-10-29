package Threxion.tallergen.state;

import net.minecraft.nbt.CompoundTag;

/**
 * 单个过敏原在一个玩家身上的全部数据：Exposure + Reaction。
 *
 * Exposure 独立于 Reaction 状态机：始终按配置的 interval/amount 衰减，最低 0；
 * Reaction 结束后（Recovery 结束）Reaction 归零，但 Exposure 保留并继续衰减。
 */
public class PlayerAllergenState {
    private final String allergenId;
    private double exposure;
    private int decayTicks;
    private final ReactionState reaction = new ReactionState();

    public PlayerAllergenState(String allergenId) {
        this.allergenId = allergenId;
    }

    public String getAllergenId() {
        return allergenId;
    }

    public double getExposure() {
        return exposure;
    }

    public void setExposure(double exposure) {
        this.exposure = exposure;
    }

    public int getDecayTicks() {
        return decayTicks;
    }

    public void setDecayTicks(int decayTicks) {
        this.decayTicks = decayTicks;
    }

    public ReactionState getReaction() {
        return reaction;
    }

    public void copyFrom(PlayerAllergenState other) {
        this.exposure = other.exposure;
        this.decayTicks = other.decayTicks;
        this.reaction.setPhase(other.reaction.getPhase());
        this.reaction.setSeverity(other.reaction.getSeverity());
        this.reaction.setLatencyRemaining(other.reaction.getLatencyRemaining());
        this.reaction.setLatencyBase(other.reaction.getLatencyBase());
        this.reaction.setActiveRemaining(other.reaction.getActiveRemaining());
        this.reaction.setRecoveryRemaining(other.reaction.getRecoveryRemaining());
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("exposure", exposure);
        tag.putInt("decayTicks", decayTicks);
        tag.put("reaction", reaction.serializeNBT());
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        this.exposure = tag.getDouble("exposure");
        this.decayTicks = tag.getInt("decayTicks");
        if (tag.contains("reaction")) {
            this.reaction.deserializeNBT(tag.getCompound("reaction"));
        }
    }
}
