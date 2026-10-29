package Threxion.threxioncore.api.event;

import Threxion.threxioncore.api.integration.Integration;
import com.google.gson.JsonObject;

/**
 * 一个 Integration 初始化后触发（load_integration 阶段）。
 * 权限：read —— 可读取 Integration 实例、JSON 原始字段与 enabled/required 状态。
 */
public class IntegrationLoadEvent extends ThrexionEvent {

    private final Integration integration;
    private final JsonObject raw;
    private final boolean enabled;
    private final boolean required;

    public IntegrationLoadEvent(Integration integration, JsonObject raw, boolean enabled, boolean required) {
        super("integration_load");
        this.integration = integration;
        this.raw = raw;
        this.enabled = enabled;
        this.required = required;
    }

    public Integration getIntegration() {
        return integration;
    }

    public JsonObject getRaw() {
        return raw;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isRequired() {
        return required;
    }
}
