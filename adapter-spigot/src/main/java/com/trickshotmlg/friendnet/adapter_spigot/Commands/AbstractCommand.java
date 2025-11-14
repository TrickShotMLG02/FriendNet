package com.trickshotmlg.friendnet.adapter_spigot.Commands;

import com.trickshotmlg.friendnet.adapter_spigot.SpigotPlayer;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.MessageManager;
import com.trickshotmlg.friendnet.core.Logger;
import com.trickshotmlg.friendnet.core_api.interfaces.PermissionNode;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public abstract class AbstractCommand implements CommandExecutor, TabCompleter {

    private final JavaPlugin plugin;
    private final String name;
    private final PermissionNode permission;
    private final String description;
    private final String usage;
    private final Map<String, AbstractCommand> subCommands = new HashMap<>();
    private AbstractCommand parent; // for permission inheritance

    protected AbstractCommand(JavaPlugin plugin, String name, String description, String usage, PermissionNode permission) {
        this.plugin = plugin;
        this.name = name;
        this.description = description;
        this.usage = usage;
        this.permission = permission;

        Logger.debug("Registered Command: " + this);
    }

    public JavaPlugin getPlugin() { return plugin; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getUsage() { return usage; }
    public PermissionNode getPermission() { return permission; }

    public AbstractCommand getParent() { return parent; }
    public void setParent(AbstractCommand parent) { this.parent = parent; }

    public void registerSubCommand(AbstractCommand sub) {
        sub.setParent(this);
        subCommands.put(sub.getName().toLowerCase(), sub);
    }

    public Collection<AbstractCommand> getSubCommands() { return subCommands.values(); }

    protected abstract boolean execute(CommandSender sender, String[] args);
    protected abstract List<String> tabComplete(CommandSender sender, String[] args);

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        return handleCommand(sender, args);
    }

    public boolean handleCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            // no subcommand — execute this command
            if (!hasPermission(sender)) {
                MessageManager.send(sender, "noPermission");
                return true;
            }
            return execute(sender, args);
        }

        AbstractCommand sub = subCommands.get(args[0].toLowerCase());
        if (sub != null) {
            return sub.handleCommand(sender, Arrays.copyOfRange(args, 1, args.length));
        }

        // No matching subcommand — maybe execute this one
        if (!hasPermission(sender)) {
            MessageManager.send(sender, "noPermission");
            return true;
        }

        return execute(sender, args);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        return handleTabComplete(sender, args);
    }

    public List<String> handleTabComplete(CommandSender sender, String[] args) {

        if (args.length == 1 && !subCommands.isEmpty()) {
            return subCommands.entrySet().stream()
                    .filter(entry -> {
                        String cmdName = entry.getKey();
                        AbstractCommand cmd = entry.getValue();

                        boolean cmdNamePredicate = cmdName.toLowerCase().startsWith(args[0].toLowerCase());
                        boolean cmdPredicate = cmd.hasPermission(sender);

                        return cmdNamePredicate && cmdPredicate;
                    })
                    .map(Map.Entry::getKey)
                    .toList();
        }

        AbstractCommand sub = subCommands.get(args[0].toLowerCase());
        if (sub != null) {
            return sub.handleTabComplete(sender, Arrays.copyOfRange(args, 1, args.length));
        }

        return tabComplete(sender, args);
    }

    private boolean hasPermission(CommandSender sender) {
        // console always has permission to do something
        if (sender instanceof ConsoleCommandSender) return true;

        Player player = sender instanceof Player p ? p : null;

        SpigotPlayer spigotPlayer = new SpigotPlayer(player);
        return permission.has(spigotPlayer);
    }

    private String getEffectivePermission() {
        if (permission != null && !permission.getPermission().isEmpty()) return permission.getPermissionPrefixed();
        if (parent != null) return parent.getEffectivePermission();
        return null;
    }

    protected String getUsageMessage(CommandSender sender) {
        if (getSubCommands().isEmpty()) {
            return getUsage() != null ? getUsage() : "/" + getName();
        }

        StringBuilder sb = new StringBuilder("/").append(getName()).append(" <");

        List<String> subNames = new ArrayList<>();
        for (AbstractCommand sub : getSubCommands()) {
            if (sender != null && sub.hasPermission(sender)) {
                subNames.add(sub.getName());
            }
        }

        subNames.sort(String.CASE_INSENSITIVE_ORDER);

        sb.append(String.join(" | ", subNames)).append(">");
        return sb.toString();
    }

    protected String getUsageMessage() {
        return getUsageMessage(null);
    }

    @Override
    public String toString() {
        return  getClass().getName() + "{" +
                "name='" + name + '\'' +
                ", permission='" + permission + '\'' +
                ", description='" + description + '\'' +
                ", usage='" + getUsageMessage() + '\'' +
                '}';
    }
}
