package Threxion.tdiscovery.definition;

import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.data.DefinitionLoader;
import net.minecraft.util.GsonHelper;
import com.google.gson.JsonObject;
import java.nio.file.Path;
import java.util.Set;

public final class DiscoveryEntryDefinitionLoader implements DefinitionLoader<DiscoveryEntryDefinition> {
    private static final PropertyId TYPE = PropertyId.parse("tdiscovery:entry");
    @Override public PropertyId getType() { return TYPE; }
    @Override public Set<String> getDirectories() { return Set.of("tdiscovery/entries"); }
    @Override public DiscoveryEntryDefinition load(Path file, JsonObject obj) {
        return new DiscoveryEntryDefinition(
                PropertyId.parse(GsonHelper.getAsString(obj, "id")),
                GsonHelper.getAsString(obj, "display_name", ""),
                GsonHelper.getAsString(obj, "category", "未分类"),
                GsonHelper.getAsString(obj, "description", ""));
    }
    @Override public void validate(DiscoveryEntryDefinition d) {
        if (d.displayName().isBlank()) throw new IllegalArgumentException("TDiscovery entry has blank display_name: " + d.getId());
    }
}
