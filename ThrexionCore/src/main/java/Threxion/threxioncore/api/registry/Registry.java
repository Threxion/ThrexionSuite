package Threxion.threxioncore.api.registry;

import Threxion.threxioncore.api.PropertyId;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * 通用注册表：以 PropertyId(ns:id) 为键的线程安全注册容器。
 * 提供 register / get / contains / remove / entries 五种操作（规格 registry_system.operations）。
 *
 * <p>注册重复键返回 false（不覆盖）。entries() 返回不可变视图。</p>
 *
 * @param <T> 注册条目类型
 */
public class Registry<T> {

    private final Map<PropertyId, T> entries = new LinkedHashMap<>();

    public synchronized boolean register(PropertyId id, T value) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(value, "value");
        if (entries.containsKey(id)) {
            return false;
        }
        entries.put(id, value);
        return true;
    }

    public synchronized boolean register(String id, T value) {
        if (!PropertyId.isValid(id)) {
            return false;
        }
        return register(PropertyId.parse(id), value);
    }

    public synchronized Optional<T> get(PropertyId id) {
        return Optional.ofNullable(entries.get(id));
    }

    public synchronized T getOrNull(PropertyId id) {
        return entries.get(id);
    }

    public synchronized T getOrThrow(PropertyId id) {
        T value = entries.get(id);
        if (value == null) {
            throw new NoSuchElementException("Registry has no entry for " + id);
        }
        return value;
    }

    public synchronized Optional<T> get(String id) {
        if (!PropertyId.isValid(id)) {
            return Optional.empty();
        }
        return get(PropertyId.parse(id));
    }

    public synchronized boolean contains(PropertyId id) {
        return id != null && entries.containsKey(id);
    }

    public synchronized boolean contains(String id) {
        return PropertyId.isValid(id) && contains(PropertyId.parse(id));
    }

    public synchronized boolean remove(PropertyId id) {
        return id != null && entries.remove(id) != null;
    }

    public synchronized boolean remove(String id) {
        return PropertyId.isValid(id) && remove(PropertyId.parse(id));
    }

    public synchronized Map<PropertyId, T> entries() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(entries));
    }

    public synchronized Collection<T> values() {
        return Collections.unmodifiableCollection(entries.values());
    }

    public synchronized Set<PropertyId> keys() {
        return Collections.unmodifiableSet(entries.keySet());
    }

    public synchronized int size() {
        return entries.size();
    }

    public synchronized boolean isEmpty() {
        return entries.isEmpty();
    }

    public synchronized void clear() {
        entries.clear();
    }
}
