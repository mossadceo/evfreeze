package dev.mossadceo.evfreeze;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.title.Title;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.time.Duration;
import java.util.Locale;

public final class MessageConfig {
    private final JavaPlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private FileConfiguration config;
    private String locale = "ru";

    public MessageConfig(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);
        locale = config.getString("localization", "ru").toLowerCase(Locale.ROOT);
        if (!locale.equals("ru") && !locale.equals("en")) {
            plugin.getLogger().warning("Unknown localization in messages.yml: " + locale + ". Falling back to ru.");
            locale = "ru";
        }
    }

    public void send(CommandSender sender, String path, TagResolver... resolvers) {
        sender.sendMessage(component("messages." + path, resolvers));
    }

    public Component component(String path, TagResolver... resolvers) {
        String localizedPath = "locales." + locale + "." + path;
        String message = config.getString(localizedPath);
        if (message == null && !locale.equals("ru")) {
            message = config.getString("locales.ru." + path);
        }
        if (message == null) {
            message = config.getString(path, "<red>Missing message: " + localizedPath);
        }
        return miniMessage.deserialize(message, resolvers);
    }

    public void showTitle(Player player, String id) {
        String titlePath = "titles." + id;
        Title.Times times = Title.Times.times(
                Duration.ofMillis(number(titlePath + ".fade-in", 10L) * 50L),
                Duration.ofMillis(number(titlePath + ".stay", 60L) * 50L),
                Duration.ofMillis(number(titlePath + ".fade-out", 20L) * 50L)
        );
        player.showTitle(Title.title(
                component(titlePath + ".title"),
                component(titlePath + ".subtitle"),
                times
        ));
        playSound(player, string(titlePath + ".sound", ""));
    }

    public TagResolver player(String name) {
        return Placeholder.unparsed("player", name);
    }

    public TagResolver time(String time) {
        return Placeholder.unparsed("time", time);
    }

    public String plain(String path, String defaultValue) {
        return string(path, defaultValue);
    }

    private void playSound(Player player, String soundName) {
        if (soundName == null || soundName.isBlank()) {
            return;
        }
        Sound sound = Registry.SOUNDS.get(NamespacedKey.minecraft(soundName.toLowerCase(Locale.ROOT)));
        if (sound == null) {
            plugin.getLogger().warning("Unknown sound in messages.yml: " + soundName);
            return;
        }
        player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
    }

    private long number(String path, long defaultValue) {
        String localizedPath = "locales." + locale + "." + path;
        if (config.contains(localizedPath)) {
            return config.getLong(localizedPath);
        }
        if (!locale.equals("ru") && config.contains("locales.ru." + path)) {
            return config.getLong("locales.ru." + path);
        }
        return config.getLong(path, defaultValue);
    }

    private String string(String path, String defaultValue) {
        String localizedPath = "locales." + locale + "." + path;
        String value = config.getString(localizedPath);
        if (value == null && !locale.equals("ru")) {
            value = config.getString("locales.ru." + path);
        }
        if (value == null) {
            value = config.getString(path, defaultValue);
        }
        return value;
    }
}
