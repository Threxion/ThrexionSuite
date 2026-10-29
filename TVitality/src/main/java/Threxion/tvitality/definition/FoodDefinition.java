package Threxion.tvitality.definition;

import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.data.Definition;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public final class FoodDefinition implements Definition {
    public enum Category { MEAL, SNACK }
    public record Effect(String id, int amplifier, int durationTicks) {}

    private final PropertyId id;
    private final boolean enabled;
    private final String item;
    private final Category category;
    private final long durationSeconds;
    private final int staminaRestore;
    private final List<Effect> effects;

    public FoodDefinition(String id, boolean enabled, String item, Category category, long durationSeconds, int staminaRestore, List<Effect> effects) {
        this.id = PropertyId.parse(id);
        this.enabled = enabled;
        this.item = item;
        this.category = category;
        this.durationSeconds = durationSeconds;
        this.staminaRestore = staminaRestore;
        this.effects = List.copyOf(effects);
    }

    @Override public PropertyId getId() { return id; }
    public boolean enabled() { return enabled; }
    public String item() { return item; }
    public ResourceLocation itemId() { return ResourceLocation.tryParse(item); }
    public Category category() { return category; }
    public long durationSeconds() { return durationSeconds; }
    public int staminaRestore() { return staminaRestore; }
    public List<Effect> effects() { return effects; }
}
