package Threxion.threxioncore.api.transfer;

import Threxion.threxioncore.CoreRegistries;
import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.property.PropertyContainer;
import Threxion.threxioncore.api.property.PropertySchema;
import Threxion.threxioncore.api.property.PropertyValue;

import java.util.Objects;
import java.util.Optional;

/**
 * Transfer：一条跨对象属性传递规则。
 * 把 source 属性（源对象的属性）按 operation 传递到 target 属性（目标对象的属性）。
 *
 * <p>语义（规格 transfer_operations）：
 * <ul>
 *   <li>COPY：target = source × factor；</li>
 *   <li>ADD：target = target + source × factor；</li>
 *   <li>MULTIPLY：target = target × (source × factor)；</li>
 *   <li>IGNORE：不执行，target 保持不变。</li>
 * </ul>
 *
 * <p>source / target 是源对象与目标对象上的属性 id；实际执行时通过
 * {@link TransferContext} 提供源/目标 {@link PropertyContainer} 对象
 * （例如炼金的原料容器与产物容器），见 {@link #apply(TransferContext)}。</p>
 */
public final class Transfer {

    private final String id;
    private final PropertyId source;
    private final PropertyId target;
    private final TransferOperation operation;
    private final double factor;
    private final boolean enabled;

    private Transfer(String id, PropertyId source, PropertyId target,
                     TransferOperation operation, double factor, boolean enabled) {
        this.id = id;
        this.source = source;
        this.target = target;
        this.operation = operation;
        this.factor = factor;
        this.enabled = enabled;
    }

    public static Builder builder(String id, PropertyId source, PropertyId target, TransferOperation operation) {
        return new Builder(id, source, target, operation);
    }

    /**
     * 计算：给定源值对目标值的影响结果。
     * COPY=source×factor / ADD=target+source×factor / MULTIPLY=target×(source×factor)。
     */
    public double compute(double sourceValue, double targetValue) {
        return switch (operation) {
            case COPY -> sourceValue * factor;
            case ADD -> targetValue + sourceValue * factor;
            case MULTIPLY -> targetValue * (sourceValue * factor);
            case IGNORE -> targetValue;
        };
    }

    /**
     * 在给定的源/目标对象上执行这条 Transfer：读取源对象的 source 属性，
     * 按 operation/factor 计算后写入目标对象的 target 属性。
     * 源属性缺失或非数值、或该条被禁用 / 为 IGNORE、或源属性的 schema 声明
     * persistent=false（仅存在于当前计算过程，不得继承到输出）时不执行。
     *
     * <p>这是直接执行路径（不触发 TransferEvent）；需要事件拦截（读/改/取消）时
     * 请使用 {@link TransferPolicy}。</p>
     */
    public void apply(TransferContext context) {
        if (context == null || !enabled || operation == TransferOperation.IGNORE) {
            return;
        }
        if (!canTransfer(source)) {
            return;
        }
        PropertyContainer sourceContainer = context.getSource();
        PropertyContainer targetContainer = context.getTarget();
        if (sourceContainer == null || targetContainer == null) {
            return;
        }
        Optional<PropertyValue> src = sourceContainer.get(source);
        if (src.isEmpty() || !src.get().isNumeric()) {
            return;
        }
        double sourceValue = src.get().asDouble();
        Optional<PropertyValue> cur = targetContainer.get(target);
        double targetBase = cur.isPresent() && cur.get().isNumeric() ? cur.get().asDouble() : 0.0;
        double result = compute(sourceValue, targetBase);
        PropertyValue written = cur.isPresent() ? cur.get().withNumber(result) : PropertyValue.ofNumber(result);
        targetContainer.set(target, written);
    }

    public String getId() {
        return id;
    }

    /**
     * persistent 规则（规格 persistence_rule）：持久化与否由属性自身的 schema
     * 决定，而不是由 T Mod 各自判断。源属性在 {@link CoreRegistries#PROPERTIES}
     * 中的 schema 声明 persistent=false 时，该属性只存在于当前计算过程，
     * 不允许被 Transfer 继承到目标对象（输出物）。
     *
     * <p>schema 未注册时按默认 persistent=true 处理（放行），保证向后兼容。
     * 例：tallergen:wheat（persistent=true）可随药粉保存；
     * talchemy:processing_temperature（persistent=false）只存在于炼制过程。</p>
     */
    /**
     * 将源容器中所有 persistent=true 的属性复制到目标容器。
     * 这是 TAlchemy 等数据处理 Mod 的通用“属性随产物传递”入口；
     * 非持久化属性由 PropertySchema 过滤，不会泄漏到成品。
     */
    public static void transferPersistentProperties(PropertyContainer source, PropertyContainer target) {
        if (source == null || target == null) {
            return;
        }
        for (PropertyId id : source.getPropertyIds()) {
            if (!canTransfer(id)) {
                continue;
            }
            Optional<PropertyValue> value = source.get(id);
            if (value.isPresent()) {
                target.set(id, value.get());
            }
        }
    }

    public static boolean canTransfer(PropertyId sourceProperty) {
        if (sourceProperty == null) {
            return false;
        }
        Optional<PropertySchema> schema = CoreRegistries.PROPERTIES.schema(sourceProperty);
        return schema.isEmpty() || schema.get().isPersistent();
    }

    public PropertyId getSource() {
        return source;
    }

    public PropertyId getTarget() {
        return target;
    }

    public TransferOperation getOperation() {
        return operation;
    }

    public double getFactor() {
        return factor;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Transfer other)) {
            return false;
        }
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Transfer[" + id + " " + source + " -> " + target + " " + operation + " x" + factor + "]";
    }

    public static final class Builder {
        private final String id;
        private final PropertyId source;
        private final PropertyId target;
        private final TransferOperation operation;
        private double factor = 1.0;
        private boolean enabled = true;

        private Builder(String id, PropertyId source, PropertyId target, TransferOperation operation) {
            this.id = Objects.requireNonNull(id, "id");
            this.source = Objects.requireNonNull(source, "source");
            this.target = Objects.requireNonNull(target, "target");
            this.operation = Objects.requireNonNull(operation, "operation");
        }

        public Builder factor(double factor) {
            this.factor = factor;
            return this;
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Transfer build() {
            return new Transfer(id, source, target, operation, factor, enabled);
        }
    }
}
