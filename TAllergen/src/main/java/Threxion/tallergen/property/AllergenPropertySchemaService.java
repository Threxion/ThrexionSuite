package Threxion.tallergen.property;

import Threxion.tallergen.config.AllergenConfig;
import Threxion.tallergen.config.ConfigManager;
import Threxion.threxioncore.CoreRegistries;
import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.property.PropertySchema;
import Threxion.threxioncore.api.property.PropertyType;

import java.util.HashSet;
import java.util.Set;

/** 保持 JSON 中的过敏原与 Core PropertySchema 同步。 */
public final class AllergenPropertySchemaService {
    private static final Set<PropertyId> OWN_IDS = new HashSet<>();

    private AllergenPropertySchemaService() {}

    public static synchronized void sync() {
        Set<PropertyId> current = new HashSet<>();
        // 过敏原参数可能在 /reload 后变化，先移除上一轮由 TAllergen 拥有的 schema。
        for (PropertyId old : Set.copyOf(OWN_IDS)) CoreRegistries.PROPERTIES.remove(old);
        for (AllergenConfig allergen : ConfigManager.get().getAllergens()) {
            PropertyId id = PropertyId.parse("tallergen:" + allergen.id);
            current.add(id);
            CoreRegistries.PROPERTIES.register(id, PropertySchema.builder(id)
                    .type(PropertyType.NUMBER)
                    .defaultValue(0.0)
                    .range(allergen.minExposure, allergen.exposureMax)
                    .modifiable(true)
                    .persistent(true)
                    .description("TAllergen exposure carrier: " + allergen.displayName)
                    .build());
        }
        OWN_IDS.clear();
        OWN_IDS.addAll(current);
    }
}
