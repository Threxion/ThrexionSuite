package Threxion.threxioncore.api.scan;

import Threxion.threxioncore.api.PropertyId;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 扫描上下文：携带被扫描的目标（subject）与附加数据。
 * ScanProvider 读取 context 产出 ScanEntry。
 */
public class ScanContext {

    private final Object subject;
    private final Map<String, Object> attributes = new LinkedHashMap<>();

    public ScanContext(Object subject) {
        this.subject = subject;
    }

    public ScanContext(Object subject, Map<String, Object> attributes) {
        this.subject = subject;
        if (attributes != null) {
            this.attributes.putAll(attributes);
        }
    }

    public Optional<Object> getSubject() {
        return Optional.ofNullable(subject);
    }

    public ScanContext with(String key, Object value) {
        attributes.put(key, value);
        return this;
    }

    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    public Map<String, Object> getAttributes() {
        return Map.copyOf(attributes);
    }
}
