package dev.mossadceo.evfreeze;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public final class EVFreezePlaceholderExpansion extends PlaceholderExpansion {
    private final EVFreeze plugin;

    public EVFreezePlaceholderExpansion(EVFreeze plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "evfreeze";
    }

    @Override
    public @NotNull String getAuthor() {
        return "mossadceo";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (!params.equalsIgnoreCase("frozen") || player == null) {
            return "";
        }
        return plugin.isFrozen(player.getUniqueId())
                ? plugin.messages().plain("placeholders.frozen", "\u2744\uFE0E")
                : "";
    }
}
