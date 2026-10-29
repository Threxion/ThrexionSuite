package Threxion.threxioncore.api.scan;

import Threxion.threxioncore.CoreRegistries;
import Threxion.threxioncore.ThrexionCore;
import Threxion.threxioncore.api.event.ScanEvent;
import Threxion.threxioncore.config.ThrexionConfig;
import net.minecraftforge.common.MinecraftForge;

/**
 * ScanManager：统一收集所有 ScanProvider 并组合成 Scan 结果。
 * 流程：遍历 ScanProviderRegistry 中的可用 Provider → 各自 provide →
 * 派发 ScanEvent（允许追加内容）→ 返回结果。
 */
public final class ScanManager {

    private ScanManager() {
    }

    public static ScanResult scan(ScanContext context) {
        ScanResult result = new ScanResult(context);
        if (context == null) {
            return result;
        }
        ThrexionConfig config = ThrexionConfig.get();
        if (config != null && !config.isScanEnabled()) {
            return result;
        }
        for (ScanProvider provider : CoreRegistries.SCAN_PROVIDERS.values()) {
            if (!provider.isAvailable()) {
                continue;
            }
            try {
                provider.provide(context, result);
            } catch (Exception e) {
                ThrexionCore.LOGGER.error("[threxioncore] ScanProvider {} failed: {}", provider.getId(), e.getMessage());
            }
        }
        MinecraftForge.EVENT_BUS.post(new ScanEvent(result));
        return result;
    }
}
