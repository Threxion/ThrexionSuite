package Threxion.tallergen.property;

import Threxion.tallergen.database.FoodAllergenDatabase;
import Threxion.tallergen.config.ConfigManager;
import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.property.ItemStackPropertyContainer;
import Threxion.threxioncore.api.property.PropertyValue;
import net.minecraft.world.item.ItemStack;
import java.util.LinkedHashMap;
import java.util.Map;

/** 将扫描结果物化为 Core Property；TAlchemy 等下游 Mod 不需要理解 TAllergen 数据库。 */
public final class AllergenPropertyService {
    private AllergenPropertyService() {}

    public static void annotate(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return;
        var db = FoodAllergenDatabase.get();
        for (String allergenId : db.getAllergens(stack.getItem())) {
            var config = ConfigManager.get().getAllergen(allergenId);
            if (config == null) continue;
            PropertyId id = PropertyId.parse("tallergen:" + allergenId);
            ItemStackPropertyContainer properties = new ItemStackPropertyContainer(stack);
            if (!properties.has(id)) {
                properties.set(id, PropertyValue.ofNumber(1.0));
            }
        }
    }

    /** 返回物品携带的过敏原暴露值（1.0 = 100% = 默认一单位暴露）。 */
    public static Map<String, Double> getExposures(ItemStack stack) {
        Map<String, Double> result = new LinkedHashMap<>();
        if (stack == null || stack.isEmpty()) return result;
        ItemStackPropertyContainer properties = new ItemStackPropertyContainer(stack);
        for (var id : properties.getPropertyIds()) {
            if (!"tallergen".equals(id.getNamespace())) continue;
            properties.get(id).filter(PropertyValue::isNumeric).ifPresent(v -> result.put(id.getPath(), v.asDouble()));
        }
        return result;
    }
}
