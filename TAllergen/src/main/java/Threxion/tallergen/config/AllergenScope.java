package Threxion.tallergen.config;

import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.Set;

/**
 * 黑白名单：控制 Recipe Scanner 的处理范围。
 * - Blacklist 优先级最高：黑名单里的物品一律不扫描、不传播、不算携带过敏原。
 * - Whitelist 启用且非空时：只扫描白名单里的物品（收紧范围）。
 * - Whitelist 为空（默认）表示不限制，扫描所有不在黑名单的物品。
 */
public class AllergenScope {
    private final Set<ResourceLocation> blacklist = new HashSet<>();
    private final Set<ResourceLocation> whitelist = new HashSet<>();
    private boolean blacklistEnabled;
    private boolean whitelistEnabled;

    public void setBlacklistEnabled(boolean enabled) {
        this.blacklistEnabled = enabled;
    }

    public void setWhitelistEnabled(boolean enabled) {
        this.whitelistEnabled = enabled;
    }

    public void addBlacklistItem(ResourceLocation item) {
        this.blacklist.add(item);
    }

    public void addWhitelistItem(ResourceLocation item) {
        this.whitelist.add(item);
    }

    public void clear() {
        this.blacklist.clear();
        this.whitelist.clear();
        this.blacklistEnabled = false;
        this.whitelistEnabled = false;
    }

    public Set<ResourceLocation> getBlacklist() {
        return this.blacklist;
    }

    public Set<ResourceLocation> getWhitelist() {
        return this.whitelist;
    }

    public boolean isBlacklistEnabled() {
        return this.blacklistEnabled;
    }

    public boolean isWhitelistEnabled() {
        return this.whitelistEnabled;
    }

    /** Blacklist 优先于 Whitelist。 */
    public boolean isEligible(ResourceLocation item) {
        if (this.blacklistEnabled && this.blacklist.contains(item)) {
            return false;
        }
        if (this.whitelistEnabled && !this.whitelist.isEmpty() && !this.whitelist.contains(item)) {
            return false;
        }
        return true;
    }
}
