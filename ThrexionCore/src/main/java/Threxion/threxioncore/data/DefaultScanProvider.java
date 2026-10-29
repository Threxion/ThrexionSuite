package Threxion.threxioncore.data;

import Threxion.threxioncore.ThrexionConstants;
import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.data.Definition;
import Threxion.threxioncore.api.property.PropertyValue;
import Threxion.threxioncore.api.scan.ScanContext;
import Threxion.threxioncore.api.scan.ScanProvider;
import Threxion.threxioncore.api.scan.ScanResult;

/**
 * Core 自带默认 ScanProvider：扫描一个 Definition 时列出其全部属性。
 * 演示 Scan 框架；各 Mod 可实现自己的 ScanProvider 并注册。
 */
public class DefaultScanProvider implements ScanProvider {

    private final PropertyId id = PropertyId.of(ThrexionConstants.CORE_NAMESPACE, "definitions");

    @Override
    public PropertyId getId() {
        return id;
    }

    @Override
    public void provide(ScanContext context, ScanResult result) {
        var subject = context.getSubject();
        if (subject.isPresent() && subject.get() instanceof Definition def) {
            for (var entry : def.getProperties().asMap().entrySet()) {
                PropertyValue v = entry.getValue();
                if (v.isNumeric()) {
                    result.add(entry.getKey().getPath(), v.asDouble(), id.toString());
                } else {
                    result.add(entry.getKey().getPath(), v, id.toString());
                }
            }
        }
    }
}
