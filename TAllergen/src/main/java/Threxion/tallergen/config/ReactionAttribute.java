package Threxion.tallergen.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Reaction 配置里的属性修改条目。
 * { "attribute": "minecraft:generic.movement_speed", "amount": -0.05, "operation": "multiply_base" }
 */
public class ReactionAttribute {
    public final ResourceLocation attributeId;
    public final double amount;
    public final AttributeModifier.Operation operation;

    public ReactionAttribute(ResourceLocation attributeId, double amount, AttributeModifier.Operation operation) {
        this.attributeId = attributeId;
        this.amount = amount;
        this.operation = operation;
    }

    public static ReactionAttribute fromJson(JsonObject obj) {
        ResourceLocation attributeId = ResourceLocation.tryParse(
                GsonHelper.getAsString(obj, "attribute", "minecraft:generic.movement_speed"));
        if (attributeId == null) {
            attributeId = ResourceLocation.tryParse("minecraft:generic.movement_speed");
        }
        String opStr = GsonHelper.getAsString(obj, "operation", "addition").toLowerCase();
        AttributeModifier.Operation op = switch (opStr) {
            case "multiply_base", "multiply_base1" -> AttributeModifier.Operation.MULTIPLY_BASE;
            case "multiply_total", "multiply" -> AttributeModifier.Operation.MULTIPLY_TOTAL;
            default -> AttributeModifier.Operation.ADDITION;
        };
        return new ReactionAttribute(attributeId, GsonHelper.getAsDouble(obj, "amount", 0.0), op);
    }

    public static List<ReactionAttribute> parseList(JsonArray arr) {
        List<ReactionAttribute> list = new ArrayList<>();
        if (arr == null) return list;
        for (int i = 0; i < arr.size(); i++) {
            list.add(fromJson(arr.get(i).getAsJsonObject()));
        }
        return list;
    }
}
