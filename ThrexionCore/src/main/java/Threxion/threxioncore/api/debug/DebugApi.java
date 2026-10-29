package Threxion.threxioncore.api.debug;

import Threxion.threxioncore.CoreRegistries;
import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.data.Definition;
import Threxion.threxioncore.api.modifier.Modifier;
import Threxion.threxioncore.api.modifier.ModifierPipeline;
import Threxion.threxioncore.api.property.PropertyValue;
import Threxion.threxioncore.api.transfer.Transfer;
import Threxion.threxioncore.config.ThrexionConfig;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

/**
 * Debug API（规格 debug_api）：展示完整属性链路
 * （Property → Base → Transfer → Modifier → Final）。
 *
 * <p>受 debug 配置控制：debug / trace_properties / trace_transfers 三项默认关闭。</p>
 */
public final class DebugApi {

    /** 逐线程的追踪链（供手动 trace 用）。 */
    private static final ThreadLocal<Deque<DebugTrace>> CHAIN = ThreadLocal.withInitial(ArrayDeque::new);

    private DebugApi() {
    }

    public static boolean isDebugEnabled() {
        ThrexionConfig config = ThrexionConfig.get();
        return config != null && config.isDebug();
    }

    public static boolean isTracePropertiesEnabled() {
        ThrexionConfig config = ThrexionConfig.get();
        return config != null && config.isTraceProperties();
    }

    public static boolean isTraceTransfersEnabled() {
        ThrexionConfig config = ThrexionConfig.get();
        return config != null && config.isTraceTransfers();
    }

    /** 清空当前线程追踪链。 */
    public static void begin() {
        CHAIN.get().clear();
    }

    /** 记录一步追踪（仅在 debug 开启时生效）。 */
    public static void trace(String source, String operation, double value) {
        if (isDebugEnabled()) {
            CHAIN.get().addLast(new DebugTrace(source, operation, value));
        }
    }

    /** 取出并清空当前线程的追踪链。 */
    public static List<DebugTrace> drain() {
        List<DebugTrace> out = new ArrayList<>(CHAIN.get());
        CHAIN.get().clear();
        return out;
    }

    /**
     * 计算某个 Definition 上单个属性的完整链路：
     * Base →（Transfer 影响）→（Modifier 影响）→ Final。
     * 始终记录全部步骤（供 /threxion chain 命令展示）；
     * 被动 trace(...) API 的开关由 trace_properties / trace_transfers 配置控制。
     */
    public static DebugChain computeChain(Definition definition, PropertyId property) {
        double base = 0.0;
        Optional<PropertyValue> current = definition != null ? definition.getProperties().get(property) : Optional.empty();
        if (current.isPresent() && current.get().isNumeric()) {
            base = current.get().asDouble();
        }
        double value = base;
        List<DebugTrace> transferSteps = new ArrayList<>();
        for (Transfer t : CoreRegistries.TRANSFERS.values()) {
            if (!t.isEnabled() || !property.equals(t.getTarget()) || t.getOperation() == Threxion.threxioncore.api.transfer.TransferOperation.IGNORE) {
                continue;
            }
            Optional<PropertyValue> src = definition != null ? definition.getProperties().get(t.getSource()) : Optional.empty();
            if (src.isEmpty() || !src.get().isNumeric()) {
                continue;
            }
            value = t.compute(src.get().asDouble(), value);
            String op = t.getOperation().name() + (t.getFactor() != 1.0 ? "*" + t.getFactor() : "");
            transferSteps.add(new DebugTrace(t.getId(), op + " (" + src.get().asDouble() + ")", value));
        }
        List<DebugTrace> modifierSteps = new ArrayList<>();
        for (Modifier m : ModifierPipeline.sort(CoreRegistries.MODIFIERS.values())) {
            if (!m.isEnabled() || !property.equals(m.getTarget())) {
                continue;
            }
            value = m.compute(value);
            modifierSteps.add(new DebugTrace(m.getId() + "[" + m.getSource() + "]", m.getOperation().name() + " " + m.describeAmount(), value));
        }
        return new DebugChain(property, base, transferSteps, modifierSteps, value);
    }
}
