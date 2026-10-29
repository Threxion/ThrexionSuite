package Threxion.tallergen.state;

/**
 * Reaction 状态机阶段：
 *  NONE      无反应
 *  LATENT    潜伏期（随机时长，期间再摄入只加 Exposure + 可能缩短剩余潜伏期）
 *  ACTIVE    发作中（severity >= 1，施加效果与属性）
 *  RECOVERY  恢复期（效果与 Modifier 已移除，时长结束后 Reaction 归零）
 *
 * Exposure 独立于该状态机，始终按 interval/amount 衰减。
 */
public enum ReactionPhase {
    NONE,
    LATENT,
    ACTIVE,
    RECOVERY;

    public static ReactionPhase fromName(String name) {
        for (ReactionPhase p : values()) {
            if (p.name().equalsIgnoreCase(name)) {
                return p;
            }
        }
        return NONE;
    }
}
