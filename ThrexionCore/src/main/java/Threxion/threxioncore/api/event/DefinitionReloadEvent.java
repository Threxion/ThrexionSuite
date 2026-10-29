package Threxion.threxioncore.api.event;

import Threxion.threxioncore.api.data.Definition;

import java.util.Collection;
import java.util.List;

/**
 * 数据热重载完成后触发（/reload）。权限：read —— 可读取重载后的全部 Definition。
 */
public class DefinitionReloadEvent extends ThrexionEvent {

    private final List<Definition> definitions;

    public DefinitionReloadEvent(Collection<Definition> definitions) {
        super("definition_reload");
        this.definitions = List.copyOf(definitions);
    }

    public List<Definition> getDefinitions() {
        return definitions;
    }
}
