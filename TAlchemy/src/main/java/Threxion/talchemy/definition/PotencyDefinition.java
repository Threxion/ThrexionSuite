package Threxion.talchemy.definition;

import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.data.Definition;

/**
 * 药力通道的语义定义。PropertySchema 负责实际数值存储与上限；这里负责单位尺度。
 * 默认 unitValue=100，即 100 点 = 1 个药力单位。
 */
public record PotencyDefinition(
        PropertyId id,
        String displayName,
        double unitValue
) implements Definition {
    @Override public PropertyId getId() { return id; }
}
