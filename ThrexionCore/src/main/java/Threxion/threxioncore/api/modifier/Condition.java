package Threxion.threxioncore.api.modifier;

/**
 * Modifier 条件判定器（规格 modifier_condition）。
 * 由各 Mod 通过 {@link ConditionRegistry} 按 {@code namespace:id} 键注册，
 * Core 在 Modifier 执行管线中按 Modifier 声明的 condition 键查表并调用——
 * 不做脚本 / 表达式语言，判定逻辑一律由注册的 Java 实现提供。
 *
 * <p>典型注册：</p>
 * <pre>{@code
 * ConditionRegistry.register("tallergen:is_allergic", (modifier, context) -> {
 *     PropertyAccess props = context == null ? null : context.properties();
 *     return props != null && props.get(PropertyId.parse("tallergen:allergic")).map(
 *             v -> v.isBoolean() && v.asBoolean()).orElse(false);
 * });
 * }</pre>
 */
@FunctionalInterface
public interface Condition {

    /**
     * 判定某个 Modifier 在当前执行上下文下是否应当生效。
     *
     * @param modifier 被判定是否执行的 Modifier（可读其 id / target / condition 等）
     * @param context  本次管线执行上下文（属性视图 {@link ModifierContext#properties()} /
     *                 随机源），可能为 null（纯数值计算且未提供 context 时不会调用本方法）
     * @return true 表示该 Modifier 生效并参与计算
     */
    boolean test(Modifier modifier, ModifierContext context);
}
