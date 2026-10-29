package Threxion.threxioncore.config;

/**
 * JSONC 加载器：支持 // 行注释与 /* ... *​/ 块注释（保留字符串字面量内的注释内容），
 * 剥离后交给 Gson 解析。所有配置文件采用 JSONC 格式（规格 jsonc_enabled: true）。
 */
public final class JsoncLoader {

    private JsoncLoader() {
    }

    /** 剥离 JSONC 注释，返回合法 JSON 文本。 */
    public static String stripComments(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        StringBuilder sb = new StringBuilder(input.length());
        boolean inString = false;
        boolean escaped = false;
        int i = 0;
        int n = input.length();
        while (i < n) {
            char c = input.charAt(i);
            if (inString) {
                sb.append(c);
                if (escaped) {
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '"') {
                    inString = false;
                }
                i++;
                continue;
            }
            if (c == '"') {
                inString = true;
                sb.append(c);
                i++;
                continue;
            }
            if (c == '/' && i + 1 < n) {
                char next = input.charAt(i + 1);
                if (next == '/') {
                    i += 2;
                    while (i < n && input.charAt(i) != '\n') {
                        i++;
                    }
                    sb.append('\n');
                    continue;
                }
                if (next == '*') {
                    i += 2;
                    while (i + 1 < n && !(input.charAt(i) == '*' && input.charAt(i + 1) == '/')) {
                        i++;
                    }
                    i = Math.min(i + 2, n);
                    sb.append('\n');
                    continue;
                }
            }
            sb.append(c);
            i++;
        }
        return sb.toString();
    }

    /** 解析 JSONC 字符串为 Gson JsonElement。 */
    public static com.google.gson.JsonElement parse(String jsonc) {
        return com.google.gson.JsonParser.parseString(stripComments(jsonc));
    }

    /** 解析 JSONC 字符串为 JsonObject；非对象或解析失败返回 null。 */
    public static com.google.gson.JsonObject parseObject(String jsonc) {
        try {
            com.google.gson.JsonElement element = parse(jsonc);
            return element != null && element.isJsonObject() ? element.getAsJsonObject() : null;
        } catch (RuntimeException e) {
            return null;
        }
    }
}
