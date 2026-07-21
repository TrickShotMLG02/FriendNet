package com.trickshotmlg.friendnet.adapter_spigot.Commands;

import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
import com.trickshotmlg.friendnet.adapter_spigot.SpigotPlayer;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.SpigotCommandResultRenderer;
import com.trickshotmlg.friendnet.core.application.command.CommandDefinition;
import com.trickshotmlg.friendnet.core.application.command.CommandExecutionContext;
import com.trickshotmlg.friendnet.core.application.command.CommandFeedbackUseCases;
import com.trickshotmlg.friendnet.core.application.command.CommandPath;
import com.trickshotmlg.friendnet.core.application.command.CommandRegistry;
import com.trickshotmlg.friendnet.core_api.interfaces.PermissionNode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public abstract class AbstractCommand implements CommandExecutor, TabCompleter {

    private final FriendNetPlugin plugin;
    private final CommandPath primaryPath;
    private final CommandRegistry registry;
    private final Map<CommandPath, TabCompletionHandler> tabCompletionHandlers = new HashMap<>();

    protected AbstractCommand(FriendNetPlugin plugin, CommandPath primaryPath, CommandRegistry registry) {
        this.plugin = plugin;
        this.primaryPath = primaryPath;
        this.registry = registry;
    }

    protected FriendNetPlugin getPlugin() {
        return plugin;
    }

    protected CommandRegistry getRegistry() {
        return registry;
    }

    protected void registerTabCompletion(CommandPath path, TabCompletionHandler handler) {
        tabCompletionHandlers.put(path, handler);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        ResolvedCommand resolvedCommand = resolve(label, args);
        Optional<CommandDefinition> definition = registry.definition(resolvedCommand.path());

        if (definition.isEmpty()) {
            SpigotCommandResultRenderer.render(sender, CommandFeedbackUseCases.usage(primaryUsage(sender)));
            return true;
        }

        CommandDefinition commandDefinition = definition.get();
        if (!hasPermission(sender, commandDefinition.permission())) {
            SpigotCommandResultRenderer.noPermission(sender);
            return true;
        }

        if (commandDefinition.playerOnly() && !(sender instanceof Player)) {
            SpigotCommandResultRenderer.playersOnly(sender);
            return true;
        }

        if (resolvedCommand.path().equals(primaryPath) && resolvedCommand.args().isEmpty()) {
            SpigotCommandResultRenderer.render(sender, CommandFeedbackUseCases.usage(primaryUsage(sender)));
            return true;
        }

        SpigotCommandResultRenderer.render(sender, registry.execute(context(sender, resolvedCommand)));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (isAlias(alias, "friends")) {
            return List.of();
        }

        if (args.length <= 1) {
            String prefix = args.length == 0 ? "" : args[0].toLowerCase(Locale.ROOT);
            return completeSubcommands(sender, prefix);
        }

        ResolvedCommand resolvedCommand = resolve(alias, args);
        TabCompletionHandler handler = tabCompletionHandlers.get(resolvedCommand.path());
        if (handler == null) {
            return List.of();
        }

        return handler.complete(sender, resolvedCommand.args());
    }

    protected List<String> completeKnownNames(Collection<String> names, String prefix) {
        String normalizedPrefix = prefix == null ? "" : prefix.toLowerCase(Locale.ROOT);
        return names.stream()
                .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(normalizedPrefix))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    private List<String> completeSubcommands(CommandSender sender, String prefix) {
        return registry.definitions().stream()
                .filter(definition -> definition.path().startsWith(primaryPath))
                .filter(definition -> definition.path().segments().size() == primaryPath.segments().size() + 1)
                .filter(definition -> hasPermission(sender, definition.permission()))
                .map(definition -> definition.path().commandName())
                .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(prefix))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    private ResolvedCommand resolve(String label, String[] args) {
        if (isAlias(label, "friends")) {
            return new ResolvedCommand(CommandPath.of("friends"), List.of(args));
        }

        if (args.length == 0) {
            return new ResolvedCommand(primaryPath, List.of());
        }

        CommandPath subcommandPath = primaryPath.append(args[0]);
        if (registry.definition(subcommandPath).isPresent()) {
            return new ResolvedCommand(subcommandPath, Arrays.asList(Arrays.copyOfRange(args, 1, args.length)));
        }

        return new ResolvedCommand(primaryPath, Arrays.asList(args));
    }

    private CommandExecutionContext context(CommandSender sender, ResolvedCommand resolvedCommand) {
        UUID senderId = sender instanceof Player player ? player.getUniqueId() : null;
        return new CommandExecutionContext(
                senderId,
                sender.getName(),
                sender instanceof Player,
                resolvedCommand.path(),
                resolvedCommand.args()
        );
    }

    private boolean hasPermission(CommandSender sender, PermissionNode permission) {
        if (permission == null) {
            return true;
        }
        if (sender instanceof ConsoleCommandSender) {
            return true;
        }
        if (!(sender instanceof Player player)) {
            return false;
        }

        return permission.has(new SpigotPlayer(player));
    }

    private String primaryUsage(CommandSender sender) {
        return registry.definitions().stream()
                .filter(definition -> definition.path().startsWith(primaryPath))
                .filter(definition -> definition.path().segments().size() == primaryPath.segments().size() + 1)
                .filter(definition -> hasPermission(sender, definition.permission()))
                .map(definition -> definition.path().commandName())
                .sorted(Comparator.naturalOrder())
                .reduce((left, right) -> left + " | " + right)
                .map(names -> "/" + primaryPath.commandName() + " <" + names + ">")
                .orElse("/" + primaryPath.commandName());
    }

    private boolean isAlias(String value, String expected) {
        return value != null && value.equalsIgnoreCase(expected);
    }

    protected record ResolvedCommand(CommandPath path, List<String> args) {
    }

    @FunctionalInterface
    protected interface TabCompletionHandler {
        List<String> complete(CommandSender sender, List<String> args);
    }
}
