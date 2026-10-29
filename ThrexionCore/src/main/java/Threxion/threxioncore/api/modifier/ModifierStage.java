package Threxion.threxioncore.api.modifier;

/**
 * Domain-neutral final calculation stages for properties that receive several
 * modifier sources. The order is fixed: Recipe → Catalyst → Station.
 *
 * <p>Quality is intentionally not a ModifierStage. TQuality resolves a concrete
 * item's quality into its actual properties before domain calculation begins.</p>
 */
public enum ModifierStage {
    RECIPE(0),
    CATALYST(1),
    STATION(2);

    private final int order;

    ModifierStage(int order) { this.order = order; }

    public int getOrder() { return order; }
}
