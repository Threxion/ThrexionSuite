package Threxion.threxioncore.reload;

import Threxion.threxioncore.api.reload.ReloadManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.server.packs.resources.ResourceManager;

/**
 * 通过 AddReloadListenerEvent 注册的 /reload 监听器。
 * /reload 时触发 ReloadManager.reload()（重读 config + JSON 数据并同步客户端）。
 * apply 在游戏主线程执行。
 */
public class ThrexionReloadListener extends SimplePreparableReloadListener<Void> {

    @Override
    protected Void prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        return null;
    }

    @Override
    protected void apply(Void object, ResourceManager resourceManager, ProfilerFiller profiler) {
        ReloadManager.reload();
    }
}
