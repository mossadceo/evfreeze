package dev.mossadceo.evfreeze;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class FreezeStorage {
    private final File file;
    private FileConfiguration config;

    public FreezeStorage(JavaPlugin plugin) {
        file = new File(plugin.getDataFolder(), "data.yml");
        reload();
    }

    public void reload() {
        config = YamlConfiguration.loadConfiguration(file);
    }

    public Map<UUID, FrozenPlayer> loadFrozenPlayers() {
        Map<UUID, FrozenPlayer> players = new LinkedHashMap<>();
        ConfigurationSection section = config.getConfigurationSection("frozen-players");
        if (section == null) {
            return players;
        }

        for (String key : section.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                String path = "frozen-players." + key;
                players.put(uuid, new FrozenPlayer(
                        uuid,
                        config.getString(path + ".name", "Unknown"),
                        config.getLong(path + ".frozen-at")
                ));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return players;
    }

    public void saveFrozenPlayers(Iterable<FrozenPlayer> players) {
        config.set("frozen-players", null);
        for (FrozenPlayer player : players) {
            String path = "frozen-players." + player.uuid();
            config.set(path + ".name", player.name());
            config.set(path + ".frozen-at", player.frozenAtMillis());
        }
        save();
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to save data.yml", exception);
        }
    }
}
