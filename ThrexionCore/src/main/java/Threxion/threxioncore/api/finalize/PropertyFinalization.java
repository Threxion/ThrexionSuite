package Threxion.threxioncore.api.finalize;

import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.property.MutablePropertyContainer;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Generic property finalization helpers.
 *
 * <p>Core deliberately does not know what "medicine", "metal", "weapon" or
 * "equipment" means. A domain mod performs its own final calculation, then
 * uses these helpers to consume properties that no longer belong to the result.</p>
 *
 * <p>Finalization is one-way: reading a property never creates another property.
 * The domain finalizer decides which calculated result properties are retained.</p>
 */
public final class PropertyFinalization {
    private PropertyFinalization() {}

    /** Remove the supplied properties after a domain-specific final calculation. */
    public static void consume(MutablePropertyContainer properties, Collection<PropertyId> ids) {
        if (properties == null || ids == null) return;
        for (PropertyId id : ids) {
            if (id != null) properties.remove(id);
        }
    }

    /**
     * Consume every current property except the explicitly retained result properties.
     * The caller owns the semantic meaning of the retained set.
     */
    public static void consumeAllExcept(MutablePropertyContainer properties, Collection<PropertyId> retained) {
        if (properties == null) return;
        Set<PropertyId> keep = retained == null ? Set.of() : new HashSet<>(retained);
        for (PropertyId id : Set.copyOf(properties.getPropertyIds())) {
            if (!keep.contains(id)) properties.remove(id);
        }
    }
}
