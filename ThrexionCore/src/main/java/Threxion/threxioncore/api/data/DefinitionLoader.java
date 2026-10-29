package Threxion.threxioncore.api.data;

import Threxion.threxioncore.api.PropertyId;
import com.google.gson.JsonObject;

import java.nio.file.Path;
import java.util.Set;

/**
 * Definition JSON 加载器接口（规格 definition_system）。
 * 每个 Definition 类型（Core 自带格式 {@link CoreDefinitionLoader}、TAllergen、
 * 其他 T Mod 的自有格式）都有自己的加载器实现，并注册到
 * {@link DefinitionLoaderRegistry}；Core 加载定义文件时按 JSON 的 "type" 字段
 * 分发到对应加载器，不负责解析所有 Definition 的自有字段。
 *
 * <p>{@link #getType()} 返回的 type 必须与定义 JSON 中的 "type" 字段一致
 * （如 {@code tallergen:allergen}），Registry 以它作为注册键。</p>
 *
 * <p>解析（load）与一致性校验（validate）分离：load 做格式解析并产出 T，
 * validate 做需要注册表等外部状态参与的一致性校验。strict 语义（校验失败抛异常
 * 还是仅记警告）由各实现决定。</p>
 *
 * @param <T> 该加载器产出的 Definition 子类型
 */
public interface DefinitionLoader<T extends Definition> {

    /** 该加载器处理的 Definition 类型的标识（如 {@code threxioncore:definition}）。 */
    PropertyId getType();

    /**
     * 返回该 Definition Loader 负责的数据目录（相对于 config/threxioncore/）。
     * 默认使用 definitions/，下游 T Mod 可以声明自己的目录，例如：
     * tallergen/allergens/、talchemy/processes/。
     * Core 只负责遍历这些目录并按 type 分发，不理解目录中的业务语义。
     */
    default Set<String> getDirectories() {
        return Set.of("definitions");
    }

    /**
     * 从 JSON 解析结果加载一个 Definition。
     *
     * @param file 来源文件（用于文件名与 id 匹配等校验）
     * @param json 已去除 JSONC 注释的 JSON 对象
     * @return 加载出的 Definition
     * @throws IllegalArgumentException 格式 / 必填字段等错误
     */
    T load(Path file, JsonObject json);

    /**
     * 对已加载的 Definition 做一致性校验。
     *
     * @param definition 待校验对象
     * @throws IllegalArgumentException 校验失败（strict 模式下）
     */
    void validate(T definition);
}
