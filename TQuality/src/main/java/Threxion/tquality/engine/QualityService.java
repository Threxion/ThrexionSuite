package Threxion.tquality.engine;

import Threxion.tquality.definition.QualityAssignmentDefinition;
import Threxion.tquality.definition.QualityDefinition;
import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.property.ItemStackPropertyContainer;
import Threxion.threxioncore.api.property.PropertyValue;
import net.minecraft.world.item.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

/** TQuality assignment engine: roll once, convert to a grade, then discard the roll. */
public final class QualityService {
    public static final PropertyId GRADE = PropertyId.parse("tquality:grade");
    private QualityService() {}

    public static boolean hasAssignment(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        String itemId = stack.getItem().builtInRegistryHolder().key().location().toString();
        return QualityDefinitionIndex.assignment(itemId) != null;
    }

    public static boolean assignIfNeeded(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        ItemStackPropertyContainer props = new ItemStackPropertyContainer(stack);
        if (props.has(GRADE)) return false;

        String itemId = stack.getItem().builtInRegistryHolder().key().location().toString();
        QualityDefinitionIndex.Assignment indexed = QualityDefinitionIndex.assignment(itemId);
        if (indexed != null) {
            QualityAssignmentDefinition assignment = indexed.definition();
            QualityDefinition qualityDefinition = indexed.quality();

            int roll = ThreadLocalRandom.current().nextInt(assignment.rollMin(), assignment.rollMax() + 1);
            var range = assignment.ranges().stream()
                    .filter(r -> roll >= r.min() && roll <= r.max())
                    .findFirst().orElse(null);
            if (range == null) return false;

            var grade = qualityDefinition.grades().stream()
                    .filter(g -> g.id().equals(range.grade()))
                    .findFirst().orElse(null);
            if (grade == null) return false;

            props.set(GRADE, PropertyValue.ofString(grade.id()));
            for (var e : grade.properties().entrySet()) props.set(e.getKey(), e.getValue());
            return true;
        }
        return false;
    }

    /** Assign one grade to every unit and return grouped stacks by grade. */
    public static java.util.List<ItemStack> splitByQuality(ItemStack source) {
        if (source == null || source.isEmpty()) return java.util.List.of();
        if (new ItemStackPropertyContainer(source).has(GRADE)) return java.util.List.of(source.copy());

        java.util.LinkedHashMap<String, ItemStack> groups = new java.util.LinkedHashMap<>();
        for (int i = 0; i < source.getCount(); i++) {
            ItemStack one = source.copy();
            one.setCount(1);
            assignIfNeeded(one);
            String grade = new ItemStackPropertyContainer(one).get(GRADE).map(PropertyValue::getString).orElse("");
            String key = grade.isEmpty() ? "" : grade;
            ItemStack group = groups.get(key);
            if (group == null) groups.put(key, one);
            else group.grow(1);
        }
        return java.util.List.copyOf(groups.values());
    }
}
