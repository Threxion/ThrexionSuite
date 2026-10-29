package Threxion.tallergen.state;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * IPlayerAllergyData 的默认实现。
 */
public class PlayerAllergyData implements IPlayerAllergyData {
    private final Map<String, PlayerAllergenState> states = new LinkedHashMap<>();
    private final Set<String> playerAllergens = new HashSet<>();
    private boolean randomAllergenAssigned;
    private boolean syncPending;
    private int syncCooldown;

    @Override
    public Collection<PlayerAllergenState> getStates() {
        return states.values();
    }

    @Override
    public PlayerAllergenState getOrCreate(String allergenId) {
        return states.computeIfAbsent(allergenId, PlayerAllergenState::new);
    }

    @Override
    public Set<String> getPlayerAllergens() {
        return Collections.unmodifiableSet(playerAllergens);
    }

    @Override
    public boolean hasAllergen(String allergenId) {
        return playerAllergens.contains(allergenId);
    }

    @Override
    public void addPlayerAllergen(String allergenId) {
        playerAllergens.add(allergenId);
    }

    @Override
    public void removePlayerAllergen(String allergenId) {
        playerAllergens.remove(allergenId);
    }

    @Override
    public PlayerAllergenState getState(String allergenId) {
        return states.get(allergenId);
    }

    @Override
    public void removeState(String allergenId) {
        states.remove(allergenId);
    }

    @Override
    public boolean hasRolledRandomAllergen() {
        return randomAllergenAssigned;
    }

    @Override
    public void setRandomAllergenAssigned(boolean assigned) {
        this.randomAllergenAssigned = assigned;
    }

    @Override
    public void copyFrom(IPlayerAllergyData other) {
        states.clear();
        for (PlayerAllergenState s : other.getStates()) {
            PlayerAllergenState copy = new PlayerAllergenState(s.getAllergenId());
            copy.copyFrom(s);
            states.put(copy.getAllergenId(), copy);
        }
        playerAllergens.clear();
        playerAllergens.addAll(other.getPlayerAllergens());
        randomAllergenAssigned = other.hasRolledRandomAllergen();
        syncPending = true;
        syncCooldown = 0;
    }

    @Override
    public void markChanged() {
        this.syncPending = true;
    }

    @Override
    public void tickSyncCooldown() {
        if (this.syncCooldown > 0) {
            this.syncCooldown--;
        }
    }

    @Override
    public boolean shouldSync() {
        if (this.syncPending && this.syncCooldown <= 0) {
            this.syncPending = false;
            this.syncCooldown = 10;
            return true;
        }
        return false;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        CompoundTag statesTag = new CompoundTag();
        for (PlayerAllergenState s : states.values()) {
            statesTag.put(s.getAllergenId(), s.serializeNBT());
        }
        tag.put("states", statesTag);
        ListTag playerAllergensTag = new ListTag();
        for (String id : playerAllergens) {
            playerAllergensTag.add(StringTag.valueOf(id));
        }
        tag.put("playerAllergens", playerAllergensTag);
        tag.putBoolean("randomAllergenAssigned", randomAllergenAssigned);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        states.clear();
        CompoundTag statesTag = tag.getCompound("states");
        for (String key : statesTag.getAllKeys()) {
            PlayerAllergenState s = new PlayerAllergenState(key);
            s.deserializeNBT(statesTag.getCompound(key));
            states.put(key, s);
        }
        playerAllergens.clear();
        ListTag playerAllergensTag = tag.getList("playerAllergens", Tag.TAG_STRING);
        for (Tag t : playerAllergensTag) {
            playerAllergens.add(t.getAsString());
        }
        randomAllergenAssigned = tag.getBoolean("randomAllergenAssigned");
    }
}
