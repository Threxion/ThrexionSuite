package Threxion.talchemy.definition;

import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.data.Definition;
import Threxion.threxioncore.api.modifier.ModifierOperation;

import java.util.List;
import java.util.Objects;

/**
 * 可复用的药物效果定义。TAlchemy 只负责执行，具体效果由 JSON 决定。
 * 一个效果可以同时修改 Threxion Property，并在药水类物品上附加原版 MobEffect。
 */
public final class AlchemyEffectDefinition implements Definition {
    public record PropertyModifier(PropertyId property, ModifierOperation operation,
                                    double amount, double min, double max) {}

    public record PotionEffect(String effect, int durationTicks, int amplifier,
                               boolean ambient, boolean showParticles, boolean showIcon) {}

    private final PropertyId id;
    private final String displayName;
    private final List<PropertyModifier> propertyModifiers;
    private final List<PotionEffect> potionEffects;

    public AlchemyEffectDefinition(PropertyId id, String displayName,
                                    List<PropertyModifier> propertyModifiers,
                                    List<PotionEffect> potionEffects) {
        this.id = Objects.requireNonNull(id);
        this.displayName = displayName == null ? "" : displayName;
        this.propertyModifiers = List.copyOf(propertyModifiers);
        this.potionEffects = List.copyOf(potionEffects);
    }

    @Override public PropertyId getId() { return id; }
    public String displayName() { return displayName; }
    public List<PropertyModifier> propertyModifiers() { return propertyModifiers; }
    public List<PotionEffect> potionEffects() { return potionEffects; }
}
