package Threxion.threxioncore.api.event;

import Threxion.threxioncore.api.data.Definition;
import com.google.gson.JsonObject;

/**
 * 在一个 Definition 从 JSON 加载完成后、注册进 DefinitionRegistry 前触发。
 * 权限：read / modify —— 监听器可直接修改 Definition 的 PropertyContainer
 * （增删属性），也可读取原始 JSON（getRaw）。
 */
public class DefinitionLoadEvent extends ThrexionEvent {

    private final Definition definition;
    private final JsonObject raw;

    public DefinitionLoadEvent(Definition definition, JsonObject raw) {
        super("definition_load");
        this.definition = definition;
        this.raw = raw;
    }

    public Definition getDefinition() {
        return definition;
    }

    /** 原始 JSON（可能为 null，若加载失败跳过时）。 */
    public JsonObject getRaw() {
        return raw;
    }
}
