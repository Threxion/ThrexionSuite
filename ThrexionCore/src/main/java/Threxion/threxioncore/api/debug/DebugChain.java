package Threxion.threxioncore.api.debug;

import Threxion.threxioncore.api.PropertyId;

import java.util.List;

/**
 * 完整属性链路（规格 debug_api.display_fields：Property / Base / Transfer /
 * Modifier / Final），用于排查属性从 Base 到 Final 的全部变化。
 */
public final class DebugChain {

    private final PropertyId property;
    private final double base;
    private final List<DebugTrace> transferSteps;
    private final List<DebugTrace> modifierSteps;
    private final double finalValue;

    public DebugChain(PropertyId property, double base,
                      List<DebugTrace> transferSteps, List<DebugTrace> modifierSteps,
                      double finalValue) {
        this.property = property;
        this.base = base;
        this.transferSteps = List.copyOf(transferSteps);
        this.modifierSteps = List.copyOf(modifierSteps);
        this.finalValue = finalValue;
    }

    public PropertyId getProperty() {
        return property;
    }

    public double getBase() {
        return base;
    }

    public List<DebugTrace> getTransferSteps() {
        return transferSteps;
    }

    public List<DebugTrace> getModifierSteps() {
        return modifierSteps;
    }

    public double getFinalValue() {
        return finalValue;
    }

    public String format() {
        StringBuilder sb = new StringBuilder();
        sb.append("Property: ").append(property).append('\n');
        sb.append("Base: ").append(trim(base)).append('\n');
        if (transferSteps.isEmpty()) {
            sb.append("Transfer: (none)\n");
        } else {
            sb.append("Transfer:\n");
            for (DebugTrace t : transferSteps) {
                sb.append("  ").append(t).append('\n');
            }
        }
        if (modifierSteps.isEmpty()) {
            sb.append("Modifier: (none)\n");
        } else {
            sb.append("Modifier:\n");
            for (DebugTrace m : modifierSteps) {
                sb.append("  ").append(m).append('\n');
            }
        }
        sb.append("Final: ").append(trim(finalValue));
        return sb.toString();
    }

    private static String trim(double v) {
        if (v == Math.floor(v) && !Double.isInfinite(v) && Math.abs(v) < 1e15) {
            return Long.toString(Math.round(v));
        }
        return Double.toString(v);
    }
}
