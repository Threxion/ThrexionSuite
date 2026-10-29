package Threxion.threxioncore.api.modifier;

import Threxion.threxioncore.api.property.PropertyAccess;
import Threxion.threxioncore.api.random.RandomApi;

import java.util.Random;
import java.util.function.Predicate;

/**
 * Modifier 执行上下文（规格 modifier_context）：携带一次 {@link ModifierPipeline}
 * 计算所需的只读依赖——属性视图、随机源与可选的条件判定器。
 *
 * <ul>
 *   <li>{@link #properties()}：本次计算涉及的属性视图（{@link PropertyAccess}，
 *       如 RecipeContext / AlchemyContext / 目标容器）。供条件判定等读取场景使用；
 *       纯数值计算时可为 null。</li>
 *   <li>{@link #random()}：随机数源。RANDOM_ADD / RANDOM_MULTIPLY 用它生成随机量，
 *       同一 context 下管线结果可复现；未提供时回退全局 {@link RandomApi}。</li>
 *   <li>{@link #condition()}：可选条件判定器。Modifier 声明了 condition 时，执行管线
 *       优先查 {@link ConditionRegistry}（condition 是注册表键）；本判定器作为键未注册
 *       时的回退；两者都不可用则该 Modifier 被跳过。</li>
 * </ul>
 */
public final class ModifierContext {

    private final PropertyAccess properties;
    private final Random random;
    private final Predicate<Modifier> condition;

    public ModifierContext(PropertyAccess properties, Random random, Predicate<Modifier> condition) {
        this.properties = properties;
        this.random = random == null ? new Random() : random;
        this.condition = condition;
    }

    /** 仅携带属性视图的上下文（随机源随机、无条件判定器）。 */
    public static ModifierContext of(PropertyAccess properties) {
        return new ModifierContext(properties, null, null);
    }

    /** 仅携带随机源的上下文（常用于纯数值、确定性管线）。 */
    public static ModifierContext withRandom(Random random) {
        return new ModifierContext(null, random, null);
    }

    /** 空上下文：不使用属性视图、随机源随机、不裁决任何 condition。 */
    public static ModifierContext empty() {
        return new ModifierContext(null, null, null);
    }

    public PropertyAccess properties() {
        return properties;
    }

    public Random random() {
        return random;
    }

    public Predicate<Modifier> condition() {
        return condition;
    }
}
