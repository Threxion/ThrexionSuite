package Threxion.threxioncore.api.event;

import Threxion.threxioncore.api.property.PropertyContainer;
import Threxion.threxioncore.api.transfer.Transfer;
import Threxion.threxioncore.api.transfer.TransferContext;
import Threxion.threxioncore.api.transfer.TransferPolicy;
import net.minecraftforge.eventbus.api.Cancelable;

/**
 * 执行一条 Transfer 时触发（TransferPolicy.applyOne）。
 * 权限：read / modify / cancel —— 可读取源/目标容器（见 {@link #getContext()}）
 * 与源值，用 setComputedValue 修改传递后的结果值，或用 setCanceled(true) 跳过该条 Transfer。
 *
 * <p>注意：Forge 的 Event 基类已占用 getResult()/setResult(Event.Result)，
 * 故本事件使用 getComputedValue()/setComputedValue(double)。</p>
 */
@Cancelable
public class TransferEvent extends ThrexionEvent {

    private final TransferPolicy policy;
    private final Transfer transfer;
    private final TransferContext context;
    private final double sourceValue;
    private double result;

    public TransferEvent(TransferPolicy policy, Transfer transfer, TransferContext context,
                         double sourceValue, double result) {
        super("transfer");
        this.policy = policy;
        this.transfer = transfer;
        this.context = context;
        this.sourceValue = sourceValue;
        this.result = result;
    }

    public TransferPolicy getPolicy() {
        return policy;
    }

    public Transfer getTransfer() {
        return transfer;
    }

    /** 本次传递的源/目标对象。 */
    public TransferContext getContext() {
        return context;
    }

    public PropertyContainer getSource() {
        return context.getSource();
    }

    public PropertyContainer getTarget() {
        return context.getTarget();
    }

    public double getSourceValue() {
        return sourceValue;
    }

    public double getComputedValue() {
        return result;
    }

    public void setComputedValue(double result) {
        this.result = result;
    }
}
