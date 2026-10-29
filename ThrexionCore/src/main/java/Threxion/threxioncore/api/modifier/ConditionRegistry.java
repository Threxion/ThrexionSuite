package Threxion.threxioncore.api.modifier;

import Threxion.threxioncore.api.PropertyId;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Condition 注册表（规格 modifier_condition.registry）。
 * Modifier 的 condition 字段是注册表键（{@code namespace:id}，如
 * {@code "tallergen:is_allergic"}），各 Mod 用 Java 代码注册判定逻辑，
 * Core 不解析脚本 / 表达式语言：
 *
 * <pre>{@code
 * ConditionRegistry.register("tallergen:is_allergic", (modifier, context) -> { ... });
 * }</pre>
 *
 * <p>Modifier JSON 中的 {@code "condition": "tallergen:is_allergic"} 会在执行管线中
 * 通过 {@link #getOrNull(String)} 查表判定。条件通过代码注册（Java API），不属于
 * JSON 数据，因此 /reload 不会清空本注册表（与「Java API 与 Registry 类型不可热重载」
 * 一致）。重复注册同一键时保留先注册者并返回 false。</p>
 */
public final class ConditionRegistry {

    private static final Map<String, Condition> CONDITIONS = new LinkedHashMap<>();

    private ConditionRegistry() {
    }

    /** 注册一个 Condition；同键已注册时返回 false（不覆盖）。 */
    public static synchronized boolean register(PropertyId key, Condition condition) {
        return register(key == null ? null : key.getString(), condition);
    }

    /** 便捷重载：键必须是合法 {@code namespace:id}，否则返回 false。 */
    public static synchronized boolean register(String key, Condition condition) {
        Objects.requireNonNull(condition, "condition");
        if (!PropertyId.isValid(key)) {
            return false;
        }
        if (CONDITIONS.containsKey(key)) {
            return false;
        }
        CONDITIONS.put(key, condition);
        return true;
    }

    public static synchronized Optional<Condition> get(PropertyId key) {
        return Optional.ofNullable(key == null ? null : CONDITIONS.get(key.getString()));
    }

    public static synchronized Condition getOrNull(String key) {
        return key == null ? null : CONDITIONS.get(key);
    }

    public static synchronized boolean contains(String key) {
        return key != null && CONDITIONS.containsKey(key);
    }

    /** 已注册的全部条件（键 → Condition，不可变视图）。 */
    public static synchronized Map<String, Condition> entries() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(CONDITIONS));
    }
}
