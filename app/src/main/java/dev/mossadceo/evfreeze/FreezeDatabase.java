package dev.mossadceo.evfreeze;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class FreezeDatabase {
    private final Connection connection;

    public FreezeDatabase(JavaPlugin plugin) throws SQLException {
        File databaseFile = new File(plugin.getDataFolder(), "freeze.db");
        connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());
        createTables();
    }

    private void createTables() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS frozen_players (
                        uuid TEXT PRIMARY KEY,
                        name TEXT NOT NULL,
                        frozen_at INTEGER NOT NULL
                    )
                    """);
        }
    }

    public Map<UUID, FrozenPlayer> loadFrozenPlayers() throws SQLException {
        Map<UUID, FrozenPlayer> players = new LinkedHashMap<>();
        try (PreparedStatement statement = connection.prepareStatement("SELECT uuid, name, frozen_at FROM frozen_players");
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                players.put(uuid, new FrozenPlayer(
                        uuid,
                        resultSet.getString("name"),
                        resultSet.getLong("frozen_at")
                ));
            }
        }
        return players;
    }

    public void freeze(FrozenPlayer player) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO frozen_players (uuid, name, frozen_at)
                VALUES (?, ?, ?)
                ON CONFLICT(uuid) DO UPDATE SET name = excluded.name, frozen_at = excluded.frozen_at
                """)) {
            statement.setString(1, player.uuid().toString());
            statement.setString(2, player.name());
            statement.setLong(3, player.frozenAtMillis());
            statement.executeUpdate();
        }
    }

    public void unfreeze(UUID uuid) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM frozen_players WHERE uuid = ?")) {
            statement.setString(1, uuid.toString());
            statement.executeUpdate();
        }
    }

    public void close() {
        try {
            connection.close();
        } catch (SQLException ignored) {
        }
    }
}
