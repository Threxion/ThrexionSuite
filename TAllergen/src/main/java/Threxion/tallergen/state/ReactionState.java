package Threxion.tallergen.state;

import net.minecraft.nbt.CompoundTag;

/**
 * 单个过敏原的 Reaction 状态机数据。
 */
public class ReactionState {
    private ReactionPhase phase = ReactionPhase.NONE;
    private int severity;
    private int latencyRemaining;
    private int latencyBase;
    private int activeRemaining;
    private int recoveryRemaining;

    public ReactionPhase getPhase() {
        return phase;
    }

    public void setPhase(ReactionPhase phase) {
        this.phase = phase;
    }

    public int getSeverity() {
        return severity;
    }

    public void setSeverity(int severity) {
        this.severity = severity;
    }

    public int getLatencyRemaining() {
        return latencyRemaining;
    }

    public void setLatencyRemaining(int latencyRemaining) {
        this.latencyRemaining = latencyRemaining;
    }

    public int getLatencyBase() {
        return latencyBase;
    }

    public void setLatencyBase(int latencyBase) {
        this.latencyBase = latencyBase;
    }

    public int getActiveRemaining() {
        return activeRemaining;
    }

    public void setActiveRemaining(int activeRemaining) {
        this.activeRemaining = activeRemaining;
    }

    public int getRecoveryRemaining() {
        return recoveryRemaining;
    }

    public void setRecoveryRemaining(int recoveryRemaining) {
        this.recoveryRemaining = recoveryRemaining;
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("phase", phase.name());
        tag.putInt("severity", severity);
        tag.putInt("latencyRemaining", latencyRemaining);
        tag.putInt("latencyBase", latencyBase);
        tag.putInt("activeRemaining", activeRemaining);
        tag.putInt("recoveryRemaining", recoveryRemaining);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        this.phase = ReactionPhase.fromName(tag.getString("phase"));
        this.severity = tag.getInt("severity");
        this.latencyRemaining = tag.getInt("latencyRemaining");
        this.latencyBase = tag.getInt("latencyBase");
        this.activeRemaining = tag.getInt("activeRemaining");
        this.recoveryRemaining = tag.getInt("recoveryRemaining");
    }
}
