package dev.mossadceo.evfreeze;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class EVFreeze extends JavaPlugin {
    private final Map<UUID, FrozenPlayer> frozenPlayers = new ConcurrentHashMap<>();
    private final Map<UUID, ScheduledTask> effectTasks = new ConcurrentHashMap<>();
    private MessageConfig messages;
    private FreezeDatabase database;

    @Override
    public void onEnable() {
        getDataFolder().mkdirs();
        messages = new MessageConfig(this);
        messages.reload();

        try {
            database = new FreezeDatabase(this);
            frozenPlayers.putAll(database.loadFrozenPlayers());
        } catch (SQLException exception) {
            getLogger().severe("Failed to initialize SQLite database.");
            exception.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        FreezeCommand command = new FreezeCommand(this);
        Objects.requireNonNull(getCommand("freeze")).setExecutor(command);
        Objects.requireNonNull(getCommand("freeze")).setTabCompleter(command);
        Objects.requireNonNull(getCommand("unfreeze")).setExecutor(command);
        Objects.requireNonNull(getCommand("unfreeze")).setTabCompleter(command);
        Objects.requireNonNull(getCommand("freezelist")).setExecutor(command);
        Objects.requireNonNull(getCommand("freezelist")).setTabCompleter(command);

        getServer().getPluginManager().registerEvents(new FreezeListener(this), this);
        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new EVFreezePlaceholderExpansion(this).register();
        }
        Bukkit.getOnlinePlayers().forEach(player -> {
            if (isFrozen(player.getUniqueId())) {
                startFreezeEffect(player);
            }
        });
    }

    @Override
    public void onDisable() {
        effectTasks.values().forEach(ScheduledTask::cancel);
        effectTasks.clear();
        if (database != null) {
            database.close();
        }
    }

    public MessageConfig messages() {
        return messages;
    }

    public boolean isFrozen(UUID uuid) {
        return frozenPlayers.containsKey(uuid);
    }

    public Collection<FrozenPlayer> frozenPlayers() {
        ArrayList<FrozenPlayer> players = new ArrayList<>(frozenPlayers.values());
        players.sort(Comparator.comparing(FrozenPlayer::frozenAtMillis));
        return players;
    }

    public FrozenPlayer findFrozenByName(String name) {
        for (FrozenPlayer frozenPlayer : frozenPlayers.values()) {
            if (frozenPlayer.name().equalsIgnoreCase(name)) {
                return frozenPlayer;
            }
        }
        return null;
    }

    public boolean freeze(Player player) throws SQLException {
        if (isFrozen(player.getUniqueId())) {
            return false;
        }

        FrozenPlayer frozenPlayer = new FrozenPlayer(player.getUniqueId(), player.getName(), System.currentTimeMillis());
        database.freeze(frozenPlayer);
        frozenPlayers.put(player.getUniqueId(), frozenPlayer);
        runFor(player, () -> {
            applyFreezeEffect(player);
            messages.showTitle(player, "freeze");
            startFreezeEffect(player);
        });
        return true;
    }

    public boolean unfreeze(FrozenPlayer frozenPlayer) throws SQLException {
        if (frozenPlayer == null || !isFrozen(frozenPlayer.uuid())) {
            return false;
        }

        database.unfreeze(frozenPlayer.uuid());
        frozenPlayers.remove(frozenPlayer.uuid());
        stopFreezeEffect(frozenPlayer.uuid());

        Player player = Bukkit.getPlayer(frozenPlayer.uuid());
        if (player != null) {
            runFor(player, () -> {
                player.setFreezeTicks(0);
                messages.showTitle(player, "unfreeze");
            });
        }
        return true;
    }

    public void runFor(Player player, Runnable runnable) {
        player.getScheduler().execute(this, runnable, null, 1L);
    }

    public void startFreezeEffect(Player player) {
        stopFreezeEffect(player.getUniqueId());
        ScheduledTask task = player.getScheduler().runAtFixedRate(this, ignored -> {
            if (!isFrozen(player.getUniqueId()) || !player.isOnline()) {
                ignored.cancel();
                effectTasks.remove(player.getUniqueId());
                return;
            }
            applyFreezeEffect(player);
        }, null, 1L, 20L);
        if (task != null) {
            effectTasks.put(player.getUniqueId(), task);
        }
    }

    public void stopFreezeEffect(UUID uuid) {
        ScheduledTask task = effectTasks.remove(uuid);
        if (task != null) {
            task.cancel();
        }
    }

    public void applyFreezeEffect(Player player) {
        player.setFreezeTicks(player.getMaxFreezeTicks());
    }

    public String formatFrozenTime(FrozenPlayer frozenPlayer) {
        Duration duration = Duration.ofMillis(System.currentTimeMillis() - frozenPlayer.frozenAtMillis());
        long days = duration.toDays();
        long hours = duration.toHoursPart();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();

        if (days > 0) {
            return "%dд %dч %dм %dс".formatted(days, hours, minutes, seconds);
        }
        if (hours > 0) {
            return "%dч %dм %dс".formatted(hours, minutes, seconds);
        }
        if (minutes > 0) {
            return "%dм %dс".formatted(minutes, seconds);
        }
        return "%dс".formatted(seconds);
    }
}
