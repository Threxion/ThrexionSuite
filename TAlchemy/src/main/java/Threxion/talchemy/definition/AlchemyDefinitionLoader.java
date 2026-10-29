package Threxion.talchemy.definition;

import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.data.DefinitionLoader;
import Threxion.threxioncore.api.modifier.ModifierOperation;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.LinkedHashSet;

public final class AlchemyDefinitionLoader implements DefinitionLoader<AlchemyDefinition> {
    @Override public PropertyId getType(){return PropertyId.parse("talchemy:recipe");}
    @Override public Set<String> getDirectories(){return Set.of("talchemy/recipes");}
    @Override public AlchemyDefinition load(Path file, JsonObject obj){
        List<AlchemyDefinition.PropertyModifier> modifiers = new ArrayList<>();
        if (obj.has("property_modifiers")) {
            JsonArray arr = GsonHelper.getAsJsonArray(obj, "property_modifiers");
            for (int i=0;i<arr.size();i++) {
                JsonObject m=arr.get(i).getAsJsonObject();
                PropertyId property=PropertyId.parse(GsonHelper.getAsString(m,"property"));
                ModifierOperation op=ModifierOperation.parse(GsonHelper.getAsString(m,"operation","add"));
                double amount=GsonHelper.getAsDouble(m,"amount",0.0);
                double min=GsonHelper.getAsDouble(m,"min",amount);
                double max=GsonHelper.getAsDouble(m,"max",amount);
                modifiers.add(new AlchemyDefinition.PropertyModifier(property,op,amount,min,max));
            }
        }
        Set<PropertyId> transferProperties = new LinkedHashSet<>();
        if (obj.has("transfer_properties")) {
            JsonArray a = GsonHelper.getAsJsonArray(obj, "transfer_properties");
            for (int i = 0; i < a.size(); i++) transferProperties.add(PropertyId.parse(a.get(i).getAsString()));
        }
        List<PropertyId> effects = new ArrayList<>();
        if (obj.has("effects")) {
            JsonArray a = GsonHelper.getAsJsonArray(obj, "effects");
            for (int i = 0; i < a.size(); i++) effects.add(PropertyId.parse(a.get(i).getAsString()));
        }
        Set<PropertyId> removeProperties = new LinkedHashSet<>();
        if (obj.has("remove_properties")) {
            JsonArray a = GsonHelper.getAsJsonArray(obj, "remove_properties");
            for (int i = 0; i < a.size(); i++) removeProperties.add(PropertyId.parse(a.get(i).getAsString()));
        }
        int expirationDays = GsonHelper.getAsInt(obj, "expiration_days", -2);
        String expiredBehavior = GsonHelper.getAsString(obj, "expired_behavior", "none");
        PropertyId expiredPotencyProperty = null;
        if (obj.has("expired_potency_property")) {
            String value = GsonHelper.getAsString(obj, "expired_potency_property", "");
            if (!value.isBlank()) expiredPotencyProperty = PropertyId.parse(value);
        }
        double expiredPotencyAmount = GsonHelper.getAsDouble(obj, "expired_potency_amount", 0.0);
        boolean showExpirationInLore = GsonHelper.getAsBoolean(obj, "show_expiration_in_lore", true);
        boolean medicine = GsonHelper.getAsBoolean(obj, "is_medicine", false);

        boolean qualityEffectsEnabled = false;
        List<String> qualityGradeOrder = new ArrayList<>();
        List<AlchemyDefinition.QualityEffect> qualityEffects = new ArrayList<>();
        if (obj.has("quality_effects")) {
            JsonObject q = GsonHelper.getAsJsonObject(obj, "quality_effects");
            qualityEffectsEnabled = GsonHelper.getAsBoolean(q, "enabled", true);
            JsonArray order = GsonHelper.getAsJsonArray(q, "grade_order", new JsonArray());
            for (int i = 0; i < order.size(); i++) qualityGradeOrder.add(order.get(i).getAsString());
            JsonArray rules = GsonHelper.getAsJsonArray(q, "rules", new JsonArray());
            for (int i = 0; i < rules.size(); i++) {
                JsonObject r = rules.get(i).getAsJsonObject();
                // Quality is resolved upstream into concrete properties. Do not add
                // a second hidden potency modifier layer here.
                qualityEffects.add(new AlchemyDefinition.QualityEffect(
                        GsonHelper.getAsString(r, "input_grade"),
                        GsonHelper.getAsInt(r, "output_count_delta", 0),
                        GsonHelper.getAsInt(r, "grade_delta", 0)));
            }
        }

        return new AlchemyDefinition(
                PropertyId.parse(GsonHelper.getAsString(obj,"id")),
                GsonHelper.getAsString(obj,"process",""),
                GsonHelper.getAsString(obj,"input",""), GsonHelper.getAsInt(obj,"input_count",1),
                GsonHelper.getAsString(obj,"output",""), GsonHelper.getAsInt(obj,"output_count",1),
                GsonHelper.getAsString(obj,"catalyst",""), GsonHelper.getAsBoolean(obj,"catalyst_consumed",false),
                modifiers,
                GsonHelper.getAsString(obj, "transfer_mode", "all"),
                transferProperties, removeProperties, effects, expirationDays, expiredBehavior,
                expiredPotencyProperty, expiredPotencyAmount, showExpirationInLore, medicine,
                GsonHelper.getAsString(obj, "container_input", ""),
                GsonHelper.getAsInt(obj, "container_input_count", 0),
                GsonHelper.getAsString(obj, "byproduct", ""),
                GsonHelper.getAsInt(obj, "byproduct_count", 0),
                GsonHelper.getAsDouble(obj, "time_multiplier", 1.0),
                GsonHelper.getAsInt(obj, "processing_time_ticks", -1),
                GsonHelper.getAsString(obj, "byproduct_potion", ""), qualityEffectsEnabled, qualityGradeOrder, qualityEffects);
    }
    @Override public void validate(AlchemyDefinition d){
        if(d.process().isBlank()) throw new IllegalArgumentException("Recipe process is required: "+d.getId());
                if(d.input().isBlank() || d.output().isBlank()) throw new IllegalArgumentException("Recipe input/output are required: "+d.getId());
        if(d.inputCount()<1 || d.outputCount()<1) throw new IllegalArgumentException("Recipe counts must be >= 1: "+d.getId());
        if (!d.containerInput().isBlank() && d.containerInputCount() < 1) throw new IllegalArgumentException("container_input_count must be >= 1 when container_input is set: " + d.getId());
        if (!d.byproduct().isBlank() && d.byproductCount() < 1) throw new IllegalArgumentException("byproduct_count must be >= 1 when byproduct is set: " + d.getId());
        if (d.timeMultiplier() <= 0) throw new IllegalArgumentException("time_multiplier must be > 0: " + d.getId());
        if (d.processingTimeTicks() < -1) throw new IllegalArgumentException("processing_time_ticks must be -1 or >= 0: " + d.getId());
        if(!d.catalyst().isBlank() && !d.catalyst().contains(":")) throw new IllegalArgumentException("Catalyst must be namespace:id: "+d.getId());
        if (!d.transferMode().equals("all") && !d.transferMode().equals("specific") && !d.transferMode().equals("none"))
            throw new IllegalArgumentException("transfer_mode must be all, specific or none: " + d.getId());
        if (d.transferMode().equals("specific") && d.transferProperties().isEmpty())
            throw new IllegalArgumentException("specific transfer_mode requires transfer_properties: " + d.getId());
        if (d.expirationDays() < -2)
            throw new IllegalArgumentException("expiration_days must be omitted, -1 (never), or >= 0: " + d.getId());
        if (!Set.of("none", "invalidate", "add_potency").contains(d.expiredBehavior()))
            throw new IllegalArgumentException("expired_behavior must be none, invalidate or add_potency: " + d.getId());
        if (d.expiredBehavior().equals("add_potency") && d.expiredPotencyProperty() == null)
            throw new IllegalArgumentException("add_potency requires expired_potency_property: " + d.getId());
        if (d.qualityEffectsEnabled()) {
            if (!d.medicine()) throw new IllegalArgumentException("quality_effects is only valid for medicine recipes: " + d.getId());
            if (d.qualityGradeOrder().isEmpty()) throw new IllegalArgumentException("quality_effects requires grade_order: " + d.getId());
            Set<String> grades = new java.util.HashSet<>();
            for (String grade : d.qualityGradeOrder()) if (grade.isBlank() || !grades.add(grade))
                throw new IllegalArgumentException("quality_effects grade_order contains duplicate/blank grade: " + d.getId());
            Set<String> ruleGrades = new java.util.HashSet<>();
            for (var rule : d.qualityEffects()) {
                if (!grades.contains(rule.inputGrade()) || !ruleGrades.add(rule.inputGrade()))
                    throw new IllegalArgumentException("quality_effects has invalid/duplicate input_grade: " + rule.inputGrade() + " in " + d.getId());
            }
        }
    }
}
