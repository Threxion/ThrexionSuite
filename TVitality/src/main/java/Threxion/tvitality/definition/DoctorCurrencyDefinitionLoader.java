package Threxion.tvitality.definition;

import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.data.DefinitionLoader;
import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;
import java.nio.file.Path;
import java.util.Set;

public final class DoctorCurrencyDefinitionLoader implements DefinitionLoader<DoctorCurrencyDefinition> {
    @Override public PropertyId getType() { return PropertyId.parse("tvitality:doctor_currency"); }
    @Override public Set<String> getDirectories() { return Set.of("tvitality/doctor"); }
    @Override public DoctorCurrencyDefinition load(Path file, JsonObject obj) {
        return new DoctorCurrencyDefinition(GsonHelper.getAsString(obj, "id", "tvitality:doctor_currency"), GsonHelper.getAsString(obj, "item", "minecraft:gold_ingot"));
    }
    @Override public void validate(DoctorCurrencyDefinition d) {
        if (d.item().isBlank()) throw new IllegalArgumentException("Doctor currency item cannot be blank");
    }
}
