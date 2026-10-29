package Threxion.tsmithing.engine;

public final class WeightService {
    public static final double DEFAULT_FACTOR = 0.002;
    private WeightService() {}
    public static double speedMultiplier(double totalWeight, double factor) {
        if (totalWeight <= 0) return 1.0;
        return 1.0 / (1.0 + factor * totalWeight * totalWeight);
    }
}
