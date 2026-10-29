package Threxion.threxioncore.api;

import java.util.Objects;

/**
 * 统一 ID 规范：namespace:id。
 * namespace 规则：小写，允许字符 a-z0-9_；
 * id(path) 规则：小写，允许字符 a-z0-9._-。
 *
 * <p>所有 API（Property / Definition / Modifier / Transfer / ScanProvider / Integration）
 * 都使用该类型作为标识，不可变的 value object。</p>
 */
public final class PropertyId {

    private final String namespace;
    private final String path;

    private PropertyId(String namespace, String path) {
        this.namespace = namespace;
        this.path = path;
    }

    public static PropertyId of(String namespace, String path) {
        if (!isValidNamespace(namespace)) {
            throw new IllegalArgumentException("Invalid namespace '" + namespace
                    + "': must match " + Threxion.threxioncore.ThrexionConstants.NAMESPACE_PATTERN);
        }
        if (!isValidPath(path)) {
            throw new IllegalArgumentException("Invalid id path '" + path
                    + "': must match " + Threxion.threxioncore.ThrexionConstants.PATH_PATTERN);
        }
        return new PropertyId(namespace, path);
    }

    public static PropertyId parse(String id) {
        if (id == null) {
            throw new IllegalArgumentException("id cannot be null");
        }
        int idx = id.indexOf(':');
        if (idx <= 0 || idx == id.length() - 1) {
            throw new IllegalArgumentException("Invalid id '" + id + "': must be in 'namespace:id' format");
        }
        return of(id.substring(0, idx), id.substring(idx + 1));
    }

    public static boolean isValid(String id) {
        if (id == null) {
            return false;
        }
        int idx = id.indexOf(':');
        if (idx <= 0 || idx == id.length() - 1) {
            return false;
        }
        return isValidNamespace(id.substring(0, idx)) && isValidPath(id.substring(idx + 1));
    }

    public static boolean isValidNamespace(String namespace) {
        return namespace != null
                && namespace.matches(Threxion.threxioncore.ThrexionConstants.NAMESPACE_PATTERN);
    }

    public static boolean isValidPath(String path) {
        return path != null && path.matches(Threxion.threxioncore.ThrexionConstants.PATH_PATTERN);
    }

    public String getNamespace() {
        return namespace;
    }

    public String getPath() {
        return path;
    }

    public String getString() {
        return namespace + ":" + path;
    }

    @Override
    public String toString() {
        return getString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PropertyId other)) {
            return false;
        }
        return namespace.equals(other.namespace) && path.equals(other.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespace, path);
    }
}
