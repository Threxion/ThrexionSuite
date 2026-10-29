package Threxion.threxioncore.api.data;

import Threxion.threxioncore.CoreRegistries;
import Threxion.threxioncore.ThrexionConstants;
import Threxion.threxioncore.ThrexionCore;
import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.property.PropertySchema;
import Threxion.threxioncore.api.property.PropertyValue;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.nio.file.Path;

/**
 * Core 自带的 Definition 加载器（{@code definitions/*.json} 格式）：
 * {"id": "example:wheat", "type": "threxioncore:definition", "properties": {"example:property": 1.0}}。
 * 由 {@link DefinitionLoaderRegistry} 按 "type" 字段分发到本加载器。
 *
 * <p>load() 负责格式解析（id_exists / id_valid / filename_matches_id /
 * required_fields_exist / numeric_type_correct）；validate() 负责需要注册表
 * 参与的一致性校验（property_registered / property_type_correct）。
 * strict=true 时校验失败抛异常（该文件被跳过）；strict=false 时仅记警告。</p>
 */
public final class CoreDefinitionLoader implements DefinitionLoader<CoreDefinition> {

    private final boolean strict;

    public CoreDefinitionLoader(boolean strict) {
        this.strict = strict;
    }

    @Override
    public PropertyId getType() {
        return PropertyId.parse(ThrexionConstants.CORE_NAMESPACE + ":definition");
    }

    @Override
    public CoreDefinition load(Path file, JsonObject obj) {
        if (obj == null) {
            throw new IllegalArgumentException("Invalid JSON (after comment stripping)");
        }

        // required_fields_exist + id_valid
        if (!obj.has("id")) {
            throw new IllegalArgumentException("Missing field \"id\"");
        }
        JsonElement idEl = obj.get("id");
        if (!idEl.isJsonPrimitive() || !idEl.getAsJsonPrimitive().isString()) {
            throw new IllegalArgumentException("Field \"id\" must be a string");
        }
        String idStr = idEl.getAsString();
        if (!PropertyId.isValid(idStr)) {
            throw new IllegalArgumentException("Invalid id \"" + idStr + "\" (must be namespace:id, a-z0-9_. and a-z0-9._-)");
        }
        PropertyId id = PropertyId.parse(idStr);

        // filename_matches_id
        String stem = filenameStem(file);
        if (!stem.equals(id.getPath())) {
            String msg = "Filename \"" + file.getFileName() + "\" does not match id path \"" + id.getPath() + "\"";
            if (strict) {
                throw new IllegalArgumentException(msg);
            } else {
                ThrexionCore.LOGGER.warn("[threxioncore] {}", msg);
            }
        }

        CoreDefinition.Builder builder = CoreDefinition.builder(id);
        if (obj.has("properties")) {
            JsonElement propsEl = obj.get("properties");
            if (!propsEl.isJsonObject()) {
                throw new IllegalArgumentException("Field \"properties\" must be an object");
            }
            JsonObject props = propsEl.getAsJsonObject();
            for (var entry : props.entrySet()) {
                String pid = entry.getKey();
                if (!PropertyId.isValid(pid)) {
                    throw new IllegalArgumentException("Invalid property id \"" + pid + "\" in properties");
                }
                JsonElement v = entry.getValue();
                if (!v.isJsonPrimitive()) {
                    throw new IllegalArgumentException("Property \"" + pid + "\" value must be a primitive (number/boolean/string)");
                }
                PropertyValue pv = valueFromJson(v.getAsJsonPrimitive(), pid);
                builder.property(PropertyId.parse(pid), pv);
            }
        }
        return builder.build();
    }

    @Override
    public void validate(CoreDefinition definition) {
        if (definition == null) {
            return;
        }
        for (PropertyId pid : definition.getProperties().getPropertyIds()) {
            PropertyValue pv = definition.getProperties().get(pid).orElse(null);
            if (pv == null) {
                continue;
            }
            // property_registered
            var schemaOpt = CoreRegistries.PROPERTIES.get(pid);
            if (schemaOpt.isEmpty()) {
                String msg = "Property \"" + pid + "\" is not registered in PropertyRegistry";
                if (strict) {
                    throw new IllegalArgumentException(msg);
                } else {
                    ThrexionCore.LOGGER.warn("[threxioncore] {}", msg);
                }
                continue;
            }
            // property_type_correct
            PropertySchema schema = schemaOpt.get();
            if (!schema.accepts(pv)) {
                String msg = "Property \"" + pid + "\" type mismatch: schema expects "
                        + schema.getType() + ", got " + pv.getType();
                if (strict) {
                    throw new IllegalArgumentException(msg);
                } else {
                    ThrexionCore.LOGGER.warn("[threxioncore] {}", msg);
                }
            }
        }
    }

    private static PropertyValue valueFromJson(JsonPrimitive p, String propertyId) {
        if (p.isNumber()) {
            double d = p.getAsDouble();
            if (!Double.isInfinite(d) && d == Math.floor(d) && Math.abs(d) <= 9_007_199_254_740_991L) {
                return PropertyValue.ofInt((long) d);
            }
            return PropertyValue.ofNumber(d);
        }
        if (p.isBoolean()) {
            return PropertyValue.ofBoolean(p.getAsBoolean());
        }
        if (p.isString()) {
            return PropertyValue.ofString(p.getAsString());
        }
        throw new IllegalArgumentException("Unsupported value for property \"" + propertyId + "\"");
    }

    /** 文件名去除 .json / .jsonc 后缀。 */
    static String filenameStem(Path file) {
        String name = file.getFileName().toString();
        if (name.endsWith(".json")) {
            return name.substring(0, name.length() - 5);
        }
        if (name.endsWith(".jsonc")) {
            return name.substring(0, name.length() - 6);
        }
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(0, dot) : name;
    }
}
