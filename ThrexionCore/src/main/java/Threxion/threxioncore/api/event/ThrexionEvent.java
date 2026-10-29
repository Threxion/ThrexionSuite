package Threxion.threxioncore.api.event;

import net.minecraftforge.eventbus.api.Event;

/**
 * ThrexionCore 事件基类。所有自定义事件在 Forge 的
 * {@link net.minecraftforge.common.MinecraftForge#EVENT_BUS} 上派发。
 *
 * <p>规格：Event 允许读取/修改/取消，但具体权限因事件类型而异
 * （DefinitionLoadEvent: read/modify，PropertyModifyEvent: read/modify/cancel，
 * ScanEvent: read/add_content）。</p>
 */
public abstract class ThrexionEvent extends Event {

    private final String type;

    protected ThrexionEvent(String type) {
        this.type = type;
    }

    /** 事件类型标识，如 "property_modify"。 */
    public String getType() {
        return type;
    }
}
