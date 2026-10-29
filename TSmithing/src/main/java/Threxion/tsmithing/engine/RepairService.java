package Threxion.tsmithing.engine;

/** Pure repair math shared by repair-pack interactions. */
public final class RepairService {
    private RepairService() {}

    /** Weapon/tool: repair points are paid against the usable 1/3 durability scale. */
    public static long toolRepairCost(long repairPoints, double costMultiplier) {
        if (repairPoints <= 0 || costMultiplier <= 0) return 0;
        return (long) (repairPoints * costMultiplier);
    }

    /** Armor: repair percentage consumes the same percentage multiplied by the repair cost multiplier. */
    public static double armorRepairCost(double repairPercent, double costMultiplier) {
        if (repairPercent <= 0 || costMultiplier <= 0) return 0;
        return repairPercent * costMultiplier;
    }

    /** Excess armor repair becomes temporary protection; armor itself remains capped at 100%. */
    public static double splitArmorRepair(double currentPercent, double repairPercent) {
        if (repairPercent <= 0) return 0;
        return Math.max(0.0, currentPercent + repairPercent - 100.0);
    }

    /** Applies armor repair and returns the new temporary-protection value. */
    public static double armorRepairResult(double currentPercent, double temporaryProtection, double repairPercent, boolean overSpec) {
        if (repairPercent <= 0) return temporaryProtection;
        double newPercent = Math.min(100.0, Math.max(0.0, currentPercent) + repairPercent);
        double excess = overSpec ? Math.max(0.0, Math.max(0.0, currentPercent) + repairPercent - 100.0) : 0.0;
        return Math.max(0.0, temporaryProtection) + excess;
    }
}
