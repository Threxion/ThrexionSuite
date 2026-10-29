package Threxion.talchemy.definition;

import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.data.Definition;
import Threxion.threxioncore.api.modifier.ModifierOperation;

import java.util.List;
import java.util.Set;
import java.util.Objects;

public final class AlchemyDefinition implements Definition {
    public record PropertyModifier(PropertyId property, ModifierOperation operation,
                                    double amount, double min, double max) {}

    /**
     * Medicine-only quality structure rule. TAlchemy reads the input tquality:grade
     * property but does not depend on TQuality classes. Quality-derived potency is
     * already an ordinary property on the material; this rule only controls explicit
     * medicine output structure changes such as output count and output grade.
     */
    public record QualityEffect(String inputGrade, int outputCountDelta, int gradeDelta) {}

    private final PropertyId id;
    private final String process;
    private final boolean medicine;
    private final String containerInput;
    private final int containerInputCount;
    private final String byproduct;
    private final int byproductCount;
    private final double timeMultiplier;
    private final int processingTimeTicks;
    private final String byproductPotion;
    private final String input;
    private final int inputCount;
    private final String output;
    private final int outputCount;
    private final String catalyst;
    private final boolean catalystConsumed;
    private final List<PropertyModifier> modifiers;
    private final String transferMode;
    private final Set<PropertyId> transferProperties;
    private final Set<PropertyId> removeProperties;
    private final List<PropertyId> effects;
    private final int expirationDays;
    private final String expiredBehavior;
    private final PropertyId expiredPotencyProperty;
    private final double expiredPotencyAmount;
    private final boolean showExpirationInLore;
    private final boolean qualityEffectsEnabled;
    private final List<String> qualityGradeOrder;
    private final List<QualityEffect> qualityEffects;

    public AlchemyDefinition(PropertyId id, String process, String input, int inputCount,
                             String output, int outputCount, String catalyst, boolean catalystConsumed,
                             List<PropertyModifier> modifiers, String transferMode,
                             Set<PropertyId> transferProperties, Set<PropertyId> removeProperties,
                             List<PropertyId> effects, int expirationDays, String expiredBehavior,
                             PropertyId expiredPotencyProperty, double expiredPotencyAmount,
                             boolean showExpirationInLore, boolean medicine, String containerInput, int containerInputCount, String byproduct, int byproductCount, double timeMultiplier, int processingTimeTicks, String byproductPotion, boolean qualityEffectsEnabled, List<String> qualityGradeOrder, List<QualityEffect> qualityEffects) {
        this.id=Objects.requireNonNull(id); this.process=process;
        this.input=input; this.inputCount=inputCount; this.output=output; this.outputCount=outputCount;
        this.catalyst=catalyst == null ? "" : catalyst; this.catalystConsumed=catalystConsumed;
        this.modifiers=List.copyOf(modifiers);
        this.transferMode = transferMode == null || transferMode.isBlank() ? "all" : transferMode;
        this.transferProperties = Set.copyOf(transferProperties == null ? Set.of() : transferProperties);
        this.removeProperties = Set.copyOf(removeProperties == null ? Set.of() : removeProperties);
        this.effects = List.copyOf(effects == null ? List.of() : effects);
        this.expirationDays = expirationDays;
        this.expiredBehavior = expiredBehavior == null || expiredBehavior.isBlank() ? "none" : expiredBehavior;
        this.expiredPotencyProperty = expiredPotencyProperty;
        this.expiredPotencyAmount = expiredPotencyAmount;
        this.showExpirationInLore = showExpirationInLore;
        this.qualityEffectsEnabled = qualityEffectsEnabled;
        this.qualityGradeOrder = List.copyOf(qualityGradeOrder == null ? List.of() : qualityGradeOrder);
        this.qualityEffects = List.copyOf(qualityEffects == null ? List.of() : qualityEffects);
        this.medicine = medicine;
        this.containerInput = containerInput == null ? "" : containerInput;
        this.containerInputCount = Math.max(0, containerInputCount);
        this.byproduct = byproduct == null ? "" : byproduct;
        this.byproductCount = Math.max(0, byproductCount);
        this.timeMultiplier = timeMultiplier <= 0 ? 1.0 : timeMultiplier;
        this.processingTimeTicks = processingTimeTicks;
        this.byproductPotion = byproductPotion == null ? "" : byproductPotion;
    }
    public AlchemyDefinition(PropertyId id, String process, String input, int inputCount,
                             String output, int outputCount, String catalyst, boolean catalystConsumed,
                             List<PropertyModifier> modifiers) {
        this(id, process, input, inputCount, output, outputCount, catalyst, catalystConsumed,
                modifiers, "all", Set.of(), Set.of(), List.of(), -2, "none", null, 0.0, true, false, "", 0, "", 0, 1.0, -1, "", false, List.of(), List.of());
    }
    @Override public PropertyId getId(){return id;}
    public String process(){return process;}
    public String input(){return input;} public int inputCount(){return inputCount;}
    public String output(){return output;} public int outputCount(){return outputCount;}
    public String catalyst(){return catalyst;} public boolean catalystConsumed(){return catalystConsumed;}
    public List<PropertyModifier> modifiers(){return modifiers;}
    public String transferMode(){return transferMode;}
    public Set<PropertyId> transferProperties(){return transferProperties;}
    public Set<PropertyId> removeProperties(){return removeProperties;}
    public List<PropertyId> effects(){return effects;}
    public int expirationDays(){return expirationDays;}
    public String expiredBehavior(){return expiredBehavior;}
    public PropertyId expiredPotencyProperty(){return expiredPotencyProperty;}
    public double expiredPotencyAmount(){return expiredPotencyAmount;}
    public boolean showExpirationInLore(){return showExpirationInLore;}
    public boolean medicine(){return medicine;}
    public String containerInput(){return containerInput;}
    public int containerInputCount(){return containerInputCount;}
    public String byproduct(){return byproduct;}
    public int byproductCount(){return byproductCount;}
    public double timeMultiplier(){return timeMultiplier;}
    public int processingTimeTicks(){return processingTimeTicks;}
    public String byproductPotion(){return byproductPotion;}
}
