package Threxion.threxioncore.api.transfer;

import java.util.Locale;

/**
 * Transfer 操作类型（规格 transfer_operations）：copy / add / multiply / ignore。
 * 语义（source 为源属性值，target 为目标属性值，factor 为系数）：
 * <ul>
 *   <li>COPY：target = source × factor</li>
 *   <li>ADD：target = target + source × factor</li>
 *   <li>MULTIPLY：target = target × (source × factor)</li>
 *   <li>IGNORE：跳过该条 Transfer</li>
 * </ul>
 * Transfer 负责属性跨对象传递，Modifier 负责数值变化，职责分离。
 */
public enum TransferOperation {
    /** target = source × factor。 */
    COPY,
    /** target = target + source × factor。 */
    ADD,
    /** target = target × (source × factor)。 */
    MULTIPLY,
    /** 忽略该条 Transfer。 */
    IGNORE;

    public static TransferOperation parse(String s) {
        if (s == null) {
            throw new IllegalArgumentException("Missing field \"operation\"");
        }
        return switch (s.toLowerCase(Locale.ROOT)) {
            case "copy" -> COPY;
            case "add" -> ADD;
            case "multiply", "mul" -> MULTIPLY;
            case "ignore", "skip" -> IGNORE;
            default -> throw new IllegalArgumentException("Invalid transfer operation \"" + s + "\"");
        };
    }
}
