package Threxion.threxioncore.api.event;

import Threxion.threxioncore.api.property.PropertyValue;
import Threxion.threxioncore.api.scan.ScanResult;

/**
 * 扫描完成后触发（ScanManager.scan 的收尾阶段）。
 * 权限：read / add_content —— 监听器可用 add(...) 向扫描结果追加内容。
 *
 * <p>注意：Forge 的 Event 基类已占用 getResult()，故使用 getScanResult()。</p>
 */
public class ScanEvent extends ThrexionEvent {

    private final ScanResult result;

    public ScanEvent(ScanResult result) {
        super("scan");
        this.result = result;
    }

    public ScanResult getScanResult() {
        return result;
    }

    public ScanEvent add(String name, PropertyValue value, String source) {
        result.add(name, value, source);
        return this;
    }

    public ScanEvent add(String name, double value, String source) {
        result.add(name, value, source);
        return this;
    }
}
