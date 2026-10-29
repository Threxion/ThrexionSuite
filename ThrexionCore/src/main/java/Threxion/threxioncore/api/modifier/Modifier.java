package Threxion.threxioncore.api.modifier;

import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.random.RandomApi;

import java.util.Objects;
import java.util.Random;

/**
 * Modifier：针对某个属性的数值变化规则。
 * 操作：set / add / multiply / random_add / random_multiply。
 * priority 数值越大优先级越高，同阶段（operation）内按优先级从高到低执行。
 */
public final class Modifier {

    private final String id;
    private final PropertyId target;
    private final ModifierOperation operation;
    private final double amount;
    private final double minAmount;
    private final double maxAmount;
    private final int priority;
    private final ModifierSource source;
    private final String condition;
    private final boolean enabled;

    private Modifier(String id, PropertyId target, ModifierOperation operation, double amount,
                     double minAmount, double maxAmount, int priority,
                     ModifierSource source, String condition, boolean enabled) {
        this.id = id;
        this.target = target;
        this.operation = operation;
        this.amount = amount;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.priority = priority;
        this.source = source == null ? ModifierSource.UNKNOWN : source;
        this.condition = condition == null ? "" : condition;
        this.enabled = enabled;
    }

    public static Builder builder(String id, PropertyId target, ModifierOperation operation) {
        return new Builder(id, target, operation);
    }

    /** 计算 base 经过该 Modifier 后的结果（RANDOM_* 用全局 {@link RandomApi}）。 */
    public double compute(double base) {
        return compute(base, null);
    }

    /**
     * 计算 base 经过该 Modifier 后的结果；RANDOM_ADD / RANDOM_MULTIPLY 使用
     * 给定的随机源，random 为 null 时回退全局 {@link RandomApi}。
     */
    public double compute(double base, Random random) {
        return switch (operation) {
            case SET -> amount;
            case ADD -> base + amount;
            case MULTIPLY -> base * amount;
            case RANDOM_ADD -> base + randomRoll(random);
            case RANDOM_MULTIPLY -> base * randomRoll(random);
        };
    }

    /** [minAmount, maxAmount] 区间内的随机量；random 为 null 时走 RandomApi。 */
    private double randomRoll(Random random) {
        if (random == null) {
            return RandomApi.random(minAmount, maxAmount);
        }
        double min = Math.min(minAmount, maxAmount);
        double max = Math.max(minAmount, maxAmount);
        if (min == max) {
            return min;
        }
        return min + random.nextDouble() * (max - min);
    }

    public String getId() {
        return id;
    }

    public PropertyId getTarget() {
        return target;
    }

    public ModifierOperation getOperation() {
        return operation;
    }

    public double getAmount() {
        return amount;
    }

    public double getMinAmount() {
        return minAmount;
    }

    public double getMaxAmount() {
        return maxAmount;
    }

    public int getPriority() {
        return priority;
    }

    public ModifierSource getSource() {
        return source;
    }

    public String getCondition() {
        return condition;
    }

    public boolean isEnabled() {
        return enabled;
    }

    /** 用于 Debug/日志：描述数值参数的字符串。 */
    public String describeAmount() {
        return switch (operation) {
            case RANDOM_ADD, RANDOM_MULTIPLY -> "[" + minAmount + ", " + maxAmount + "]";
            default -> Double.toString(amount);
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Modifier other)) {
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
        return "Modifier[" + id + " " + operation + " on " + target + " pri=" + priority + "]";
    }

    public static final class Builder {
        private final String id;
        private final PropertyId target;
        private final ModifierOperation operation;
        private double amount = 0.0;
        private double minAmount = 0.0;
        private double maxAmount = 0.0;
        private int priority = 0;
        private ModifierSource source = ModifierSource.UNKNOWN;
        private String condition = "";
        private boolean enabled = true;

        private Builder(String id, PropertyId target, ModifierOperation operation) {
            this.id = Objects.requireNonNull(id, "id");
            this.target = Objects.requireNonNull(target, "target");
            this.operation = Objects.requireNonNull(operation, "operation");
        }

        public Builder amount(double amount) {
            this.amount = amount;
            return this;
        }

        public Builder range(double min, double max) {
            this.minAmount = min;
            this.maxAmount = max;
            return this;
        }

        public Builder priority(int priority) {
            this.priority = priority;
            return this;
        }

        public Builder source(ModifierSource source) {
            this.source = source;
            return this;
        }

        public Builder condition(String condition) {
            this.condition = condition == null ? "" : condition;
            return this;
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Modifier build() {
            return new Modifier(id, target, operation, amount, minAmount, maxAmount,
                    priority, source, condition, enabled);
        }
    }
}
