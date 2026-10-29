package Threxion.threxioncore.api.validation;

import Threxion.threxioncore.CoreRegistries;
import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.modifier.ModifierOperation;
import Threxion.threxioncore.api.property.PropertyValue;
import Threxion.threxioncore.api.transfer.TransferOperation;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Validator：数据校验工具（规格 schema_validation）。
 * 覆盖：id_exists / id_valid / property_registered / property_type_correct /
 * operation_valid / required_fields_exist / numeric_type_correct /
 * filename_matches_id / reference_exists。
 */
public final class Validator {

    private Validator() {
    }

    // ---- id 格式 ----

    public static boolean isValidId(String id) {
        return PropertyId.isValid(id);
    }

    public static boolean isValidNamespace(String namespace) {
        return PropertyId.isValidNamespace(namespace);
    }

    public static boolean isValidPath(String path) {
        return PropertyId.isValidPath(path);
    }

    // ---- 注册表存在性 ----

    public static boolean idExists(PropertyId id) {
        return id != null && CoreRegistries.DEFINITIONS.contains(id);
    }

    public static boolean idExists(String id) {
        return PropertyId.isValid(id) && idExists(PropertyId.parse(id));
    }

    public static boolean propertyRegistered(PropertyId id) {
        return id != null && CoreRegistries.PROPERTIES.contains(id);
    }

    public static boolean referenceExists(PropertyId id) {
        return idExists(id) || propertyRegistered(id);
    }

    public static boolean propertyTypeCorrect(PropertyId id, PropertyValue value) {
        if (id == null || value == null) {
            return false;
        }
        var schemaOpt = CoreRegistries.PROPERTIES.get(id);
        return schemaOpt.isEmpty() || schemaOpt.get().accepts(value);
    }

    // ---- 操作有效性 ----

    public static boolean operationValid(ModifierOperation operation) {
        return operation != null;
    }

    public static boolean transferOperationValid(TransferOperation operation) {
        return operation != null;
    }

    // ---- JSON 字段 ----

    public static boolean requiredFieldsExist(JsonObject obj, String... fields) {
        if (obj == null) {
            return false;
        }
        for (String field : fields) {
            if (!obj.has(field)) {
                return false;
            }
        }
        return true;
    }

    public static boolean numericTypeCorrect(JsonElement element) {
        return element != null && element.isJsonPrimitive()
                && element.getAsJsonPrimitive().isNumber();
    }

    public static boolean booleanTypeCorrect(JsonElement element) {
        return element != null && element.isJsonPrimitive()
                && element.getAsJsonPrimitive().isBoolean();
    }

    public static boolean stringTypeCorrect(JsonElement element) {
        return element != null && element.isJsonPrimitive()
                && element.getAsJsonPrimitive().isString();
    }

    public static boolean arrayTypeCorrect(JsonElement element) {
        return element != null && element.isJsonArray();
    }

    public static boolean filenameMatchesId(JsonObject obj, String fileName) {
        if (!obj.has("id")) {
            return false;
        }
        try {
            PropertyId id = PropertyId.parse(obj.get("id").getAsString());
            String stem = fileName.endsWith(".json") ? fileName.substring(0, fileName.length() - 5)
                    : (fileName.endsWith(".jsonc") ? fileName.substring(0, fileName.length() - 6) : fileName);
            return stem.equals(id.getPath());
        } catch (RuntimeException e) {
            return false;
        }
    }

    /** 便捷：返回第一个缺失的字段名，全部存在返回 null。 */
    public static String firstMissingField(JsonObject obj, String... fields) {
        if (obj == null) {
            return "<null object>";
        }
        for (String field : fields) {
            if (!obj.has(field)) {
                return field;
            }
        }
        return null;
    }
}
