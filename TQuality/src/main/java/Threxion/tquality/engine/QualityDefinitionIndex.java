package Threxion.tquality.engine;

import Threxion.tquality.definition.QualityAssignmentDefinition;
import Threxion.tquality.definition.QualityDefinition;
import Threxion.threxioncore.CoreRegistries;
import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.data.Definition;
import Threxion.threxioncore.api.event.DefinitionReloadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.*;

/** Item-to-assignment index rebuilt atomically after Core Definition reload. */
@Mod.EventBusSubscriber(modid = "tquality", bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class QualityDefinitionIndex {
    private static volatile boolean initialized;
    public record Assignment(QualityAssignmentDefinition definition, QualityDefinition quality) {}
    private static volatile Map<String, Assignment> byItem = Map.of();
    private QualityDefinitionIndex() {}
    @SubscribeEvent public static void onDefinitionsReloaded(DefinitionReloadEvent event) { rebuild(event.getDefinitions()); }
    public static synchronized void ensureInitialized() { if (!initialized) rebuild(CoreRegistries.DEFINITIONS.values()); }
    private static synchronized void rebuild(Collection<Definition> defs) {
        Map<String, Definition> definitionsById = new HashMap<>();
        for (Definition d : defs) definitionsById.put(d.getId().getString(), d);
        Map<String, Assignment> map = new HashMap<>();
        for (Definition d : defs) if (d instanceof QualityAssignmentDefinition a) {
            Definition q = definitionsById.get(a.qualityDefinition().getString());
            if (!(q instanceof QualityDefinition quality)) continue;
            Assignment indexed = new Assignment(a, quality);
            for (String item : a.whitelist()) map.putIfAbsent(item, indexed);
        }
        byItem = Map.copyOf(map); initialized = true;
    }
    public static Assignment assignment(String itemId) { ensureInitialized(); return byItem.get(itemId); }
}
