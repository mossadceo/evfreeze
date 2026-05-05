package dev.mossadceo.evfreeze;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class FreezeCommand implements CommandExecutor, TabCompleter {
    private final EVFreeze plugin;

    public FreezeCommand(EVFreeze plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("freeze")) {
            return handleFreeze(sender, args);
        }
        if (command.getName().equalsIgnoreCase("unfreeze")) {
            return handleUnfreeze(sender, args);
        }
        if (command.getName().equalsIgnoreCase("freezelist")) {
            return handleFreezeList(sender, args);
        }
        return false;
    }

    private boolean handleFreeze(CommandSender sender, String[] args) {
        if (!sender.hasPermission("evfreeze.freeze")) {
            plugin.messages().send(sender, "no-permission");
            return true;
        }

        if (args.length != 1) {
            plugin.messages().send(sender, "usage-freeze");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            plugin.messages().send(sender, "player-not-found", plugin.messages().player(args[0]));
            return true;
        }

        if (plugin.isFrozen(target.getUniqueId())) {
            plugin.messages().send(sender, "player-already-frozen", plugin.messages().player(target.getName()));
            return true;
        }

        plugin.freeze(target);
        plugin.messages().send(sender, "freeze-success", plugin.messages().player(target.getName()));
        return true;
    }

    private boolean handleFreezeList(CommandSender sender, String[] args) {
        if (!sender.hasPermission("evfreeze.list")) {
            plugin.messages().send(sender, "no-permission");
            return true;
        }

        if (args.length != 0) {
            plugin.messages().send(sender, "usage-freezelist");
            return true;
        }

        sendFrozenList(sender);
        return true;
    }

    private boolean handleUnfreeze(CommandSender sender, String[] args) {
        if (!sender.hasPermission("evfreeze.unfreeze")) {
            plugin.messages().send(sender, "no-permission");
            return true;
        }

        if (args.length != 1) {
            plugin.messages().send(sender, "usage-unfreeze");
            return true;
        }

        FrozenPlayer frozenPlayer = plugin.findFrozenByName(args[0]);
        if (frozenPlayer == null) {
            plugin.messages().send(sender, "player-not-frozen", plugin.messages().player(args[0]));
            return true;
        }

        plugin.unfreeze(frozenPlayer);
        plugin.messages().send(sender, "unfreeze-success", plugin.messages().player(frozenPlayer.name()));
        return true;
    }

    private void sendFrozenList(CommandSender sender) {
        if (plugin.frozenPlayers().isEmpty()) {
            plugin.messages().send(sender, "list-empty");
            return;
        }

        plugin.messages().send(sender, "list-header");
        for (FrozenPlayer frozenPlayer : plugin.frozenPlayers()) {
            plugin.messages().send(
                    sender,
                    "list-entry",
                    plugin.messages().player(frozenPlayer.name()),
                    plugin.messages().time(plugin.formatFrozenTime(frozenPlayer))
            );
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length != 1) {
            return Collections.emptyList();
        }

        List<String> suggestions = new ArrayList<>();
        if (command.getName().equalsIgnoreCase("freeze")) {
            if (sender.hasPermission("evfreeze.freeze")) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!plugin.isFrozen(player.getUniqueId())) {
                        suggestions.add(player.getName());
                    }
                }
            }
        } else if (command.getName().equalsIgnoreCase("unfreeze") && sender.hasPermission("evfreeze.unfreeze")) {
            for (FrozenPlayer frozenPlayer : plugin.frozenPlayers()) {
                suggestions.add(frozenPlayer.name());
            }
        }

        String input = args[0].toLowerCase();
        suggestions.removeIf(suggestion -> !suggestion.toLowerCase().startsWith(input));
        return suggestions;
    }
}
