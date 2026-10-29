package Threxion.threxioncore.api.debug;

/**
 * Debug 追踪链上的一步：{source, operation, value}（规格 debug_api.tracking_chain）。
 */
public final class DebugTrace {

    private final String source;
    private final String operation;
    private final double value;

    public DebugTrace(String source, String operation, double value) {
        this.source = source == null ? "" : source;
        this.operation = operation == null ? "" : operation;
        this.value = value;
    }

    public String getSource() {
        return source;
    }

    public String getOperation() {
        return operation;
    }

    public double getValue() {
        return value;
    }

    @Override
    public String toString() {
        return source + " [" + operation + "] -> " + value;
    }
}
