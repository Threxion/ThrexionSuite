package Threxion.threxioncore.api.transfer;

import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.event.TransferEvent;
import Threxion.threxioncore.api.property.PropertyContainer;
import Threxion.threxioncore.api.property.PropertyValue;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * TransferPolicy：一组 Transfer 规则构成的跨对象属性传递策略。
 * apply(source, target) 把源容器的属性按各规则传递给目标容器。
 * 每条规则执行前触发 TransferEvent（可取消 / 可修改结果值）。
 */
public final class TransferPolicy {

    private final String id;
    private final List<Transfer> transfers;

    private TransferPolicy(String id, List<Transfer> transfers) {
        this.id = id;
        this.transfers = List.copyOf(transfers);
    }

    public static Builder builder(String id) {
        return new Builder(id);
    }

    /** 应用整个策略：把 source 的属性按规则传递到 target。 */
    public void apply(PropertyContainer source, PropertyContainer target) {
        if (source == null || target == null) {
            return;
        }
        for (Transfer t : transfers) {
            applyOne(t, source, target);
        }
    }

    /** 应用单条规则（触发 TransferEvent，可取消 / 可修改结果值）。 */
    public void applyOne(Transfer transfer, PropertyContainer source, PropertyContainer target) {
        if (transfer == null || !transfer.isEnabled()
                || transfer.getOperation() == TransferOperation.IGNORE) {
            return;
        }
        if (!Transfer.canTransfer(transfer.getSource())) {
            return;
        }
        Optional<PropertyValue> src = source.get(transfer.getSource());
        if (src.isEmpty() || !src.get().isNumeric()) {
            return;
        }
        double sourceValue = src.get().asDouble();
        Optional<PropertyValue> cur = target.get(transfer.getTarget());
        double targetBase = cur.isPresent() && cur.get().isNumeric() ? cur.get().asDouble() : 0.0;
        double result = transfer.compute(sourceValue, targetBase);

        TransferContext context = new TransferContext(source, target);
        TransferEvent event = new TransferEvent(this, transfer, context, sourceValue, result);
        if (MinecraftForge.EVENT_BUS.post(event)) {
            return;
        }
        result = event.getComputedValue();
        PropertyValue written = cur.isPresent() ? cur.get().withNumber(result) : PropertyValue.ofNumber(result);
        target.set(transfer.getTarget(), written);
    }

    public String getId() {
        return id;
    }

    public List<Transfer> getTransfers() {
        return transfers;
    }

    public static final class Builder {
        private final String id;
        private final List<Transfer> rules = new ArrayList<>();

        private Builder(String id) {
            this.id = id;
        }

        public Builder add(Transfer transfer) {
            if (transfer != null) {
                rules.add(transfer);
            }
            return this;
        }

        public Builder add(PropertyId source, PropertyId target, TransferOperation operation) {
            return add(Transfer.builder(source.toString() + "->" + target, source, target, operation).build());
        }

        public TransferPolicy build() {
            return new TransferPolicy(id, rules);
        }
    }
}
