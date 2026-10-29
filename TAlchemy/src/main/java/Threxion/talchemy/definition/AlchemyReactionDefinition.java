package Threxion.talchemy.definition;

import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.data.Definition;

import java.util.Objects;

/**
 * 药力反应：当物品上的某个药力属性达到 threshold 时，触发一个可配置药物效果。
 * 例如 talchemy:heal >= 90 -> talchemy:healing_1。
 */
public record AlchemyReactionDefinition(
        PropertyId id,
        String displayName,
        PropertyId potencyProperty,
        double threshold,
        PropertyId effect,
        int priority
) implements Definition {
    public AlchemyReactionDefinition {
        Objects.requireNonNull(id);
        Objects.requireNonNull(potencyProperty);
        Objects.requireNonNull(effect);
        displayName = displayName == null ? "" : displayName;
    }

    @Override
    public PropertyId getId() {
        return id;
    }
}
