package Threxion.tsmithing.api;

/** Public station contract; concrete blocks remain an implementation detail of addons/content. */
public interface ISmithingStation {
    default int processingTimeTicks() { return -1; }
    default double timeMultiplier() { return 1.0; }
}
