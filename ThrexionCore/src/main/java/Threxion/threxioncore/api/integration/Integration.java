package Threxion.threxioncore.api.integration;

import Threxion.threxioncore.api.PropertyId;

/**
 * Integration 接口（规格 integration_api）：各 Mod 通过实现本接口
 * 声明可选联动。接口方法：getId / isAvailable / initialize。
 *
 * <p>JSON 声明字段：{"id": "namespace:id", "enabled": true, "required": false}，
 * default_required=false 避免硬依赖。required=true 且不可用时会记录错误日志。</p>
 */
public interface Integration {

    /** 唯一标识（namespace:id），须与 integrations/ 下 JSON 中的 id 对应。 */
    PropertyId getId();

    /** 该集成是否满足可用条件（如对应 mod 已加载）。 */
    boolean isAvailable();

    /** 默认 required=false（可选联动）。 */
    default boolean isRequired() {
        return false;
    }

    /** 初始化逻辑（幂等）。 */
    default void initialize() {
    }
}
