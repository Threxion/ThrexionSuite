package Threxion.tallergen.scan;

import Threxion.tallergen.property.AllergenPropertyService;
import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.property.PropertyValue;
import Threxion.threxioncore.api.property.ItemStackPropertyContainer;
import Threxion.threxioncore.api.scan.ScanContext;
import Threxion.threxioncore.api.scan.ScanProvider;
import Threxion.threxioncore.api.scan.ScanResult;
import net.minecraft.world.item.ItemStack;

/** 将 ItemStack 上持久化的 tallergen:* 属性暴露给统一 /threxion scan。 */
public final class AllergenScanProvider implements ScanProvider {
    private static final PropertyId ID = PropertyId.parse("tallergen:scan");

    @Override public PropertyId getId() { return ID; }

    @Override public void provide(ScanContext context, ScanResult result) {
        Object subject = context.getSubject().orElse(null);
        if (!(subject instanceof ItemStack stack) || stack.isEmpty()) return;
        AllergenPropertyService.annotate(stack);
        ItemStackPropertyContainer properties = new ItemStackPropertyContainer(stack);
        for (PropertyId id : properties.getPropertyIds()) {
            if (!"tallergen".equals(id.getNamespace())) continue;
            if ("scan".equals(id.getPath())) continue;
            properties.get(id).filter(PropertyValue::isNumeric).ifPresent(v ->
                    result.add("过敏原/" + id.getPath(), v.asDouble(), "TAllergen"));
        }
    }
}
