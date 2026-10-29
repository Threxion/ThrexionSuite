package Threxion.talchemy.engine;

/** 只读的药物保质期检查结果；检查本身不会修改 ItemStack。 */
public record ExpirationStatus(boolean configured, boolean expired, long creationDay, long expirationDay,
                               int expirationDays, String behavior, boolean handled) {
    public boolean neverExpires() { return configured && expirationDays < 0; }
}
