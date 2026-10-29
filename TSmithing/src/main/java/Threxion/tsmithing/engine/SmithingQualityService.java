package Threxion.tsmithing.engine;

import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.property.ItemStackPropertyContainer;
import net.minecraft.world.item.ItemStack;
import java.util.List;

/** Resolves the already-existing TQuality grade multiplier for TSmithing. */
public final class SmithingQualityService {
    public static final PropertyId QUALITY_MULTIPLIER = PropertyId.parse("tquality:quality_multiplier");
    private SmithingQualityService() {}

    /** Returns the stored TQuality multiplier. A stack without TQuality data is not a quality input. */
    public static double qualityMultiplier(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return 1.0;
        ItemStackPropertyContainer props = new ItemStackPropertyContainer(stack);
        return props.has(QUALITY_MULTIPLIER) ? props.getNumber(QUALITY_MULTIPLIER, 1.0) : 1.0;
    }

    /** Applies the agreed efficacy rule: 100 = full quality effect, 50 = half effect. */
    public static double applyTqeff(double qualityMultiplier, double tqeffPercent) {
        return 1.0 + (qualityMultiplier - 1.0) * (tqeffPercent / 100.0);
    }

    /**
     * Averages only materials that actually carry TQuality data. Materials without a
     * quality property are excluded from the quality average rather than treated as 1.0 inputs.
     * The caller supplies one stack per consumed material unit when quantities differ.
     */
    public static double qualityEffectMultiplier(List<ItemStack> materialStacks) {
        if (materialStacks == null || materialStacks.isEmpty()) return 1.0;
        double sum = 0.0;
        int qualified = 0;
        for (ItemStack stack : materialStacks) {
            if (stack == null || stack.isEmpty()) continue;
            ItemStackPropertyContainer props = new ItemStackPropertyContainer(stack);
            if (!props.has(QUALITY_MULTIPLIER)) continue;
            sum += props.getNumber(QUALITY_MULTIPLIER, 1.0);
            qualified++;
        }
        return qualified == 0 ? 1.0 : sum / qualified;
    }
}
