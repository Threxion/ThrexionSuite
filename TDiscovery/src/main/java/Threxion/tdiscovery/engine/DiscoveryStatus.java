package Threxion.tdiscovery.engine;

public enum DiscoveryStatus {
    DISCOVERED("已发现"),
    CONFIRMED("已确认"),
    PARTIAL("部分了解"),
    UNKNOWN_USE("用途未知"),
    FURTHER_RESEARCH("需要进一步研究");

    private final String displayName;
    DiscoveryStatus(String displayName) { this.displayName = displayName; }
    public String displayName() { return displayName; }
}
