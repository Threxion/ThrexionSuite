package Threxion.tdiscovery.client;
import java.util.HashSet;
import java.util.Set;
public final class DiscoveryClientState {
    private static final Set<String> discovered = new HashSet<>();
    private DiscoveryClientState() {}
    public static void set(Set<String> value) { synchronized (discovered) { discovered.clear(); discovered.addAll(value); } }
    public static void add(String id) { synchronized (discovered) { discovered.add(id); } }
    public static boolean has(String id) { synchronized (discovered) { return discovered.contains(id); } }
}
