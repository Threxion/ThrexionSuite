package Threxion.threxioncore.api.transfer;

import Threxion.threxioncore.api.property.PropertyContainer;

import java.util.Objects;

/**
 * Transfer 执行上下文：绑定一次传递的源对象与目标对象。
 * Transfer 描述"源 Property → 目标 Property"的规则（见 {@link Transfer}），
 * TransferContext 提供本次执行涉及的具体对象——炼金场景即原料 ItemStack
 * 的 PropertyContainer 与产物 ItemStack 的 PropertyContainer：
 *
 * <pre>
 * transfer.apply(new TransferContext(herbProperties, potionProperties));
 * </pre>
 */
public final class TransferContext {

    private final PropertyContainer source;
    private final PropertyContainer target;

    public TransferContext(PropertyContainer source, PropertyContainer target) {
        this.source = Objects.requireNonNull(source, "source");
        this.target = Objects.requireNonNull(target, "target");
    }

    public PropertyContainer getSource() {
        return source;
    }

    public PropertyContainer getTarget() {
        return target;
    }
}
