package Threxion.threxioncore.api.scan;

import Threxion.threxioncore.api.PropertyId;

/**
 * ScanProvider 接口：由各 Mod 实现并注册到 ScanProviderRegistry，
 * Core 统一收集并组合成 Scan 结果（规格 scan.ScanProvider）。
 */
public interface ScanProvider {

    /** 唯一标识（namespace:id）。 */
    PropertyId getId();

    /** 该 Provider 当前是否可用（默认可用）。 */
    default boolean isAvailable() {
        return true;
    }

    /** 向扫描结果中追加内容。 */
    void provide(ScanContext context, ScanResult result);
}
