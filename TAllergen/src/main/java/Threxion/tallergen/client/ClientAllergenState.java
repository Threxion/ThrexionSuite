package Threxion.tallergen.client;

/**
 * 客户端只读副本（服务端同步来的快照），只用于展示，不参与过敏判定。
 */
public class ClientAllergenState {
    public final String allergenId;
    public final String displayName;
    public final double exposure;
    public final String phase;
    public final int severity;

    public ClientAllergenState(String allergenId, String displayName, double exposure, String phase, int severity) {
        this.allergenId = allergenId;
        this.displayName = displayName == null || displayName.isBlank() ? allergenId : displayName;
        this.exposure = exposure;
        this.phase = phase;
        this.severity = severity;
    }
}
