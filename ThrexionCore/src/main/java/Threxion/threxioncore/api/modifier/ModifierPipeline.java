package Threxion.threxioncore.api.modifier;

import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.data.Definition;
import Threxion.threxioncore.api.property.PropertyValue;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Modifier 执行管线。
 * 排序规则（规格 modifier_execution_order / priority_rule），共三层：
 * <ol>
 *   <li>Operation Stage：set → add → multiply → random_add → random_multiply；</li>
 *   <li>Priority：同阶段内 priority 高者优先；</li>
 *   <li>注册顺序：前两层相同时保持传入集合的原始顺序（Registry 的 values()
 *       即注册顺序；流式排序是稳定排序）。</li>
 * </ol>
 */
public final class ModifierPipeline {

    private static final Comparator<Modifier> COMPARATOR =
            Comparator.comparingInt((Modifier m) -> m.getOperation().getOrder())
                    .thenComparing(Comparator.comparingInt(Modifier::getPriority).reversed());

    private ModifierPipeline() {
    }

    /** 按执行顺序排序（过滤 disabled）。 */
    public static List<Modifier> sort(Collection<Modifier> modifiers) {
        if (modifiers == null) {
            return List.of();
        }
        return modifiers.stream()
                .filter(Modifier::isEnabled)
                .sorted(COMPARATOR)
                .collect(Collectors.toList());
    }

    /**
     * 对纯数值按一组 Modifier 依序计算最终结果（不写回、不触发事件）。
     * 排序见 {@link #sort(Collection)}：先按 Operation Stage、再按 Priority、
     * 最后按注册顺序（传入集合的原始顺序）。
     *
     * <p>例：base=1.0，两个 multiply（0.8 与 0.9）→ 1.0 × 0.8 × 0.9 = 0.72；
     * 若混入 add -0.2 → 按阶段先 ADD 再 MULTIPLY：1.0 - 0.2 = 0.8。</p>
     */
    public static double apply(double base, Collection<Modifier> modifiers) {
        double result = base;
        for (Modifier modifier : sort(modifiers)) {
            result = modifier.compute(result);
        }
        return result;
    }

    /**
     * Apply explicitly separated modifier stages in the fixed order
     * Recipe → Catalyst → Station. Quality is not a stage: TQuality should have
     * already materialized the item's concrete property values before this point.
     */
    public static double applyStages(double base,
                                     Collection<Modifier> recipe,
                                     Collection<Modifier> catalyst,
                                     Collection<Modifier> station) {
        double result = apply(base, recipe);
        result = apply(result, catalyst);
        return apply(result, station);
    }

    /** 兼容别名：等价于 {@link #apply(double, Collection)}。 */
    public static double preview(double base, Collection<Modifier> modifiers) {
        return apply(base, modifiers);
    }

    /**
     * 对 PropertyValue 应用一组 Modifier 依序计算最终值（不写回、不触发事件）。
     *
     * <p>处理流程：收集 → enabled 检查 → condition 检查（condition 是
     * {@link ConditionRegistry} 注册表键，见 {@link #passesCondition}）→
     * 按 Operation Stage 排序 →
     * 同阶段按 priority 排序 → 依次计算 → 最终 PropertyValue。</p>
     *
     * <p>RANDOM_ADD / RANDOM_MULTIPLY 使用 context 提供的随机源（同一 context 下
     * 可复现）；context 为 null 时回退全局 {@link Threxion.threxioncore.api.random.RandomApi}。
     * 非数值（STRING / BOOLEAN）属性不参与计算，原样返回。</p>
     *
     * <p>例：base=1.0，ADD -0.2 → 0.8，MULTIPLY 0.5 → 0.4，
     * RANDOM_ADD [-0.19, -0.01] → 0.21~0.39。</p>
     */
    public static PropertyValue apply(PropertyValue base, Collection<Modifier> modifiers, ModifierContext context) {
        PropertyValue result = base == null ? PropertyValue.ofNumber(0.0) : base;
        for (Modifier modifier : collect(modifiers, context)) {
            if (!result.isNumeric()) {
                continue;
            }
            Random random = context == null ? null : context.random();
            result = result.withNumber(modifier.compute(result.asDouble(), random));
        }
        return result;
    }

    /** 按执行顺序排序（过滤 disabled + condition），供 context 版 apply 使用。 */
    private static List<Modifier> collect(Collection<Modifier> modifiers, ModifierContext context) {
        if (modifiers == null) {
            return List.of();
        }
        return modifiers.stream()
                .filter(Modifier::isEnabled)
                .filter(m -> passesCondition(m, context))
                .sorted(COMPARATOR)
                .collect(Collectors.toList());
    }

    /**
     * condition 检查：Modifier 未声明 condition 直接通过；声明了则优先按
     * {@link ConditionRegistry} 查表判定（condition 为注册表键），键未注册时
     * 回退到 context 提供的判定器；两者都不可用则该 Modifier 被跳过。
     */
    private static boolean passesCondition(Modifier modifier, ModifierContext context) {
        String condition = modifier.getCondition();
        if (condition == null || condition.isBlank()) {
            return true;
        }
        Condition resolved = ConditionRegistry.getOrNull(condition);
        if (resolved != null) {
            return resolved.test(modifier, context);
        }
        return context != null && context.condition() != null && context.condition().test(modifier);
    }

    /**
     * 对一个 Definition 的某个属性应用一组 Modifier。
     * 每个 Modifier 通过 PropertyContainer.modify 执行（触发 PropertyModifyEvent，
     * 可被取消），返回值表示是否发生了至少一次修改。
     */
    public static boolean apply(Definition definition, PropertyId property, Collection<Modifier> modifiers) {
        if (definition == null) {
            return false;
        }
        boolean changed = false;
        for (Modifier m : sort(modifiers)) {
            if (!property.equals(m.getTarget())) {
                continue;
            }
            if (definition.getProperties().modify(property, m)) {
                changed = true;
            }
        }
        return changed;
    }
}
