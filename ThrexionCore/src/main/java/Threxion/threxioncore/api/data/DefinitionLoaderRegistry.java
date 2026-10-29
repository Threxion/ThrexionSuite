package Threxion.threxioncore.api.data;

import Threxion.threxioncore.api.PropertyId;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * DefinitionLoader 注册表（规格 registry_system.registries 的 DefinitionLoaderRegistry）。
 * Core 只负责按 JSON 中的 "type" 字段把 Definition 文件分发给对应加载器，
 * 完全不需要理解各类型的自有字段（initial_allergy_chance / foods / reaction_id 等）。
 *
 * <p>各 T Mod 在初始化时注册自己的加载器，例如：</p>
 *
 * <pre>{@code
 * DefinitionLoaderRegistry.register(
 *     PropertyId.parse("tallergen:allergen"),
 *     new AllergenDefinitionLoader()
 * );
 * }</pre>
 *
 * <p>加载器通过代码注册（Java API），不属于 JSON 数据，因此 /reload 不会清空本注册表
 * （与「Java API 与 Registry 类型不可热重载」一致）。重复注册同一 type 时保留先注册者
 * 并返回 false。</p>
 */
public final class DefinitionLoaderRegistry {

    private static final Map<PropertyId, DefinitionLoader<?>> LOADERS = new LinkedHashMap<>();

    private DefinitionLoaderRegistry() {
    }

    /** 注册一个 DefinitionLoader；同 type 已注册时返回 false（不覆盖）。 */
    public static synchronized boolean register(PropertyId type, DefinitionLoader<?> loader) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(loader, "loader");
        if (LOADERS.containsKey(type)) {
            return false;
        }
        LOADERS.put(type, loader);
        return true;
    }

    /** 便捷重载：type 字符串必须为合法 namespace:id。 */
    public static synchronized boolean register(String type, DefinitionLoader<?> loader) {
        return PropertyId.isValid(type) && register(PropertyId.parse(type), loader);
    }

    public static synchronized Optional<DefinitionLoader<?>> get(PropertyId type) {
        return Optional.ofNullable(type == null ? null : LOADERS.get(type));
    }

    public static synchronized DefinitionLoader<?> getOrNull(PropertyId type) {
        return type == null ? null : LOADERS.get(type);
    }

    public static synchronized boolean contains(PropertyId type) {
        return type != null && LOADERS.containsKey(type);
    }

    /** 已注册的全部加载器（type → loader，不可变视图）。 */
    public static synchronized Map<PropertyId, DefinitionLoader<?>> entries() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(LOADERS));
    }
}
