package com.trickshotmlg.friendnet.adapter_velocity.commands;

import com.trickshotmlg.friendnet.adapter_velocity.FriendNetVelocityPlugin;
import com.trickshotmlg.friendnet.adapter_velocity.services.VelocityProxyMessagingService;
import com.trickshotmlg.friendnet.adapter_velocity.utils.VelocityCommandResultRenderer;
import com.trickshotmlg.friendnet.core.application.KnownPlayerLookup;
import com.trickshotmlg.friendnet.core.application.command.CommandDefinition;
import com.trickshotmlg.friendnet.core.application.command.CommandExecutionContext;
import com.trickshotmlg.friendnet.core.application.command.CommandFeedbackUseCases;
import com.trickshotmlg.friendnet.core.application.command.CommandPath;
import com.trickshotmlg.friendnet.core.application.command.CommandRegistry;
import com.trickshotmlg.friendnet.core.application.command.CommandUseCaseResult;
import com.trickshotmlg.friendnet.core.application.command.FriendCommandDefinitions;
import com.trickshotmlg.friendnet.core.application.command.FriendCommandUseCases;
import com.trickshotmlg.friendnet.core.application.command.FriendListViewData;
import com.trickshotmlg.friendnet.core_api.interfaces.PermissionNode;
import com.trickshotmlg.friendnet.core_api.models.BlocklistData;
import com.trickshotmlg.friendnet.core_api.models.FriendshipData;
import com.trickshotmlg.friendnet.core_api.models.PlayerData;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyBackendGuiType;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class VelocityFriendCommand implements SimpleCommand {

    private final FriendNetVelocityPlugin plugin;
    private final CommandRegistry registry;

    public VelocityFriendCommand(FriendNetVelocityPlugin plugin) {
        this.plugin = plugin;
        this.registry = createRegistry(plugin);
    }

    @Override
    public void execute(Invocation invocation) {
        ResolvedCommand resolvedCommand = resolve(invocation.alias(), invocation.arguments());
        Optional<CommandDefinition> definition = registry.definition(resolvedCommand.path());

        if (definition.isEmpty()) {
            VelocityCommandResultRenderer.render(plugin, invocation.source(), CommandFeedbackUseCases.usage(primaryUsage(invocation.source())));
            return;
        }

        CommandDefinition commandDefinition = definition.get();
        if (!hasPermission(invocation.source(), commandDefinition.permission())) {
            VelocityCommandResultRenderer.noPermission(plugin, invocation.source());
            return;
        }

        if (commandDefinition.playerOnly() && !(invocation.source() instanceof Player)) {
            VelocityCommandResultRenderer.playersOnly(plugin, invocation.source());
            return;
        }

        if (resolvedCommand.path().equals(FriendCommandDefinitions.FRIEND) && resolvedCommand.args().isEmpty()) {
            VelocityCommandResultRenderer.render(plugin, invocation.source(), CommandFeedbackUseCases.usage(primaryUsage(invocation.source())));
            return;
        }

        VelocityCommandResultRenderer.render(plugin, invocation.source(), registry.execute(context(invocation.source(), resolvedCommand)));
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        if ("friends".equalsIgnoreCase(invocation.alias())) {
            return List.of();
        }

        String[] arguments = invocation.arguments();
        if (arguments.length <= 1) {
            String prefix = arguments.length == 0 ? "" : arguments[0].toLowerCase(Locale.ROOT);
            return completeSubcommands(invocation.source(), prefix);
        }

        ResolvedCommand resolvedCommand = resolve(invocation.alias(), arguments);
        CommandPath path = resolvedCommand.path();
        if (path.equals(FriendCommandDefinitions.ADD.path())) {
            return completeAdd(invocation.source(), resolvedCommand.args());
        }
        if (path.equals(FriendCommandDefinitions.REMOVE.path())) {
            return completeFriends(invocation.source(), resolvedCommand.args());
        }
        if (path.equals(FriendCommandDefinitions.BLOCK.path())) {
            return completeBlock(invocation.source(), resolvedCommand.args());
        }
        if (path.equals(FriendCommandDefinitions.UNBLOCK.path())) {
            return completeBlocked(invocation.source(), resolvedCommand.args());
        }
        if (path.equals(FriendCommandDefinitions.ACCEPT.path()) || path.equals(FriendCommandDefinitions.DENY.path())) {
            return completePendingRequests(invocation.source(), resolvedCommand.args());
        }
        if (path.equals(FriendCommandDefinitions.CANCEL.path())) {
            return completeSentRequests(invocation.source(), resolvedCommand.args());
        }
        return List.of();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return registry.definition(resolve(invocation.alias(), invocation.arguments()).path())
                .map(definition -> hasPermission(invocation.source(), definition.permission()))
                .orElse(true);
    }

    private static CommandRegistry createRegistry(FriendNetVelocityPlugin plugin) {
        CommandRegistry registry = FriendCommandDefinitions.registryWithUsageHandlers();
        FriendCommandUseCases useCases = plugin.getApplicationServices().friendCommandUseCases();

        registry.register(FriendCommandDefinitions.ADD, context -> {
            if (context.args().isEmpty()) {
                return CommandFeedbackUseCases.usage(FriendCommandDefinitions.ADD.usage());
            }

            return resolveTarget(plugin, context.args().get(0))
                    .map(target -> useCases.sendFriendRequest(context.senderId(), context.senderName(), target))
                    .orElseGet(CommandFeedbackUseCases::playerNotFound);
        });

        registry.register(FriendCommandDefinitions.REMOVE, context -> {
            if (context.args().isEmpty()) {
                return CommandFeedbackUseCases.usage(FriendCommandDefinitions.REMOVE.usage());
            }

            return resolveTarget(plugin, context.args().get(0))
                    .map(target -> useCases.removeFriend(context.senderId(), target.playerId(), target.displayName()))
                    .orElseGet(CommandFeedbackUseCases::playerNotFound);
        });

        registry.register(FriendCommandDefinitions.BLOCK, context -> {
            if (context.args().isEmpty()) {
                return CommandFeedbackUseCases.usage(FriendCommandDefinitions.BLOCK.usage());
            }

            return resolveTarget(plugin, context.args().get(0))
                    .map(target -> useCases.blockPlayer(context.senderId(), target.playerId(), target.displayName()))
                    .orElseGet(CommandFeedbackUseCases::playerNotFound);
        });

        registry.register(FriendCommandDefinitions.UNBLOCK, context -> {
            if (context.args().isEmpty()) {
                return CommandFeedbackUseCases.usage(FriendCommandDefinitions.UNBLOCK.usage());
            }

            return resolveTarget(plugin, context.args().get(0))
                    .map(target -> useCases.unblockPlayer(context.senderId(), target.playerId(), target.displayName()))
                    .orElseGet(CommandFeedbackUseCases::playerNotFound);
        });

        registry.register(FriendCommandDefinitions.ACCEPT, context -> {
            if (context.args().isEmpty()) {
                return CommandFeedbackUseCases.usage(FriendCommandDefinitions.ACCEPT.usage());
            }

            return resolveTarget(plugin, context.args().get(0))
                    .map(target -> useCases.acceptRequest(context.senderId(), context.senderName(), target.playerId(), target.displayName()))
                    .orElseGet(CommandFeedbackUseCases::playerNotFound);
        });

        registry.register(FriendCommandDefinitions.ACCEPT_ALL, context -> {
            if (!context.args().isEmpty()) {
                return CommandFeedbackUseCases.usage(FriendCommandDefinitions.ACCEPT_ALL.usage());
            }

            return useCases.acceptAllRequests(context.senderId(), context.senderName());
        });

        registry.register(FriendCommandDefinitions.DENY, context -> {
            if (context.args().isEmpty()) {
                return CommandFeedbackUseCases.usage(FriendCommandDefinitions.DENY.usage());
            }

            return resolveTarget(plugin, context.args().get(0))
                    .map(target -> useCases.denyRequest(context.senderId(), target.playerId(), target.displayName()))
                    .orElseGet(CommandFeedbackUseCases::playerNotFound);
        });

        registry.register(FriendCommandDefinitions.DENY_ALL, context -> {
            if (!context.args().isEmpty()) {
                return CommandFeedbackUseCases.usage(FriendCommandDefinitions.DENY_ALL.usage());
            }

            return useCases.denyAllRequests(context.senderId());
        });

        registry.register(FriendCommandDefinitions.CANCEL, context -> {
            if (context.args().isEmpty()) {
                return CommandFeedbackUseCases.usage(FriendCommandDefinitions.CANCEL.usage());
            }

            if ("all".equalsIgnoreCase(context.args().get(0))) {
                return useCases.cancelAllRequests(context.senderId());
            }

            return resolveTarget(plugin, context.args().get(0))
                    .map(target -> useCases.cancelRequest(context.senderId(), target.playerId(), target.displayName()))
                    .orElseGet(CommandFeedbackUseCases::playerNotFound);
        });

        registry.override(FriendCommandDefinitions.LIST.path(), (context, next) -> showFriends(plugin, context.senderId(), context.args()));
        registry.override(FriendCommandDefinitions.FRIENDS_ALIAS.path(), (context, next) -> showFriends(plugin, context.senderId(), context.args()));
        registry.override(FriendCommandDefinitions.REQUESTS.path(), (context, next) -> showRequests(plugin, context.senderId(), context.args()));
        registry.override(FriendCommandDefinitions.RELOAD.path(), (context, next) -> CommandFeedbackUseCases.reload(plugin.reloadPluginConfigs()));

        return registry;
    }

    private static Optional<KnownPlayerLookup.KnownPlayer> resolveTarget(FriendNetVelocityPlugin plugin, String name) {
        return plugin.getApplicationServices().knownPlayerLookup().resolve(name);
    }

    private static CommandUseCaseResult showFriends(FriendNetVelocityPlugin plugin, UUID senderId, List<String> args) {
        if (!args.isEmpty()) {
            return CommandFeedbackUseCases.usage(FriendCommandDefinitions.LIST.usage());
        }

        if (requestBackendGui(plugin, senderId, ProxyBackendGuiType.FRIENDS, () -> renderFriendsText(plugin, senderId))) {
            return CommandUseCaseResult.builder(true).build();
        }

        return friendsText(plugin, senderId);
    }

    private static CommandUseCaseResult friendsText(FriendNetVelocityPlugin plugin, UUID senderId) {
        FriendListViewData viewData = plugin.getApplicationServices().friendCommandUseCases().listViewData(senderId);
        if (viewData.friends().isEmpty()) {
            return CommandUseCaseResult.builder(false)
                    .message(com.trickshotmlg.friendnet.core.application.command.CommandMessage.sender("friendList.empty"))
                    .build();
        }

        CommandUseCaseResult.Builder result = CommandUseCaseResult.builder(true)
                .message(com.trickshotmlg.friendnet.core.application.command.CommandMessage.sender("friendList.header"));
        for (FriendshipData friendship : viewData.friends()) {
            UUID friendId = friendship.getOtherPlayerId(senderId);
            result.message(com.trickshotmlg.friendnet.core.application.command.CommandMessage.sender(
                    "friendList.entry",
                    java.util.Map.of("target", plugin.getApplicationServices().knownPlayerLookup().displayName(friendId))
            ));
        }
        return result.build();
    }

    private static CommandUseCaseResult showRequests(FriendNetVelocityPlugin plugin, UUID senderId, List<String> args) {
        if (!args.isEmpty()) {
            return CommandFeedbackUseCases.usage(FriendCommandDefinitions.REQUESTS.usage());
        }

        if (requestBackendGui(plugin, senderId, ProxyBackendGuiType.REQUESTS, () -> renderRequestsText(plugin, senderId))) {
            return CommandUseCaseResult.builder(true).build();
        }

        return requestsText(plugin, senderId);
    }

    private static CommandUseCaseResult requestsText(FriendNetVelocityPlugin plugin, UUID senderId) {
        FriendListViewData viewData = plugin.getApplicationServices().friendCommandUseCases().listViewData(senderId);
        if (viewData.pendingRequests().isEmpty()) {
            return CommandUseCaseResult.builder(false)
                    .message(com.trickshotmlg.friendnet.core.application.command.CommandMessage.sender("friendRequest.accept.sender.nonePending"))
                    .build();
        }

        CommandUseCaseResult.Builder result = CommandUseCaseResult.builder(true)
                .message(com.trickshotmlg.friendnet.core.application.command.CommandMessage.sender("requestList.header"));
        for (FriendshipData request : viewData.pendingRequests()) {
            UUID requesterId = request.getRequesterId();
            result.message(com.trickshotmlg.friendnet.core.application.command.CommandMessage.sender(
                    "requestList.entry",
                    java.util.Map.of("target", plugin.getApplicationServices().knownPlayerLookup().displayName(requesterId))
            ));
        }
        return result.build();
    }

    private static void renderFriendsText(FriendNetVelocityPlugin plugin, UUID senderId) {
        plugin.getServer().getPlayer(senderId).ifPresent(player ->
                VelocityCommandResultRenderer.render(plugin, player, friendsText(plugin, senderId))
        );
    }

    private static void renderRequestsText(FriendNetVelocityPlugin plugin, UUID senderId) {
        plugin.getServer().getPlayer(senderId).ifPresent(player ->
                VelocityCommandResultRenderer.render(plugin, player, requestsText(plugin, senderId))
        );
    }

    private static boolean requestBackendGui(FriendNetVelocityPlugin plugin, UUID senderId, ProxyBackendGuiType guiType, Runnable fallback) {
        if (senderId == null) {
            return false;
        }

        Player player = plugin.getServer().getPlayer(senderId).orElse(null);
        if (player == null) {
            return false;
        }

        return plugin.getProxyMessagingService().openBackendGui(player, guiType, fallback);
    }

    private ResolvedCommand resolve(String label, String[] args) {
        if ("friends".equalsIgnoreCase(label)) {
            return new ResolvedCommand(CommandPath.of("friends"), List.of(args));
        }

        if (args.length == 0) {
            return new ResolvedCommand(FriendCommandDefinitions.FRIEND, List.of());
        }

        CommandPath subcommandPath = FriendCommandDefinitions.FRIEND.append(args[0]);
        if (registry.definition(subcommandPath).isPresent()) {
            return new ResolvedCommand(subcommandPath, Arrays.asList(Arrays.copyOfRange(args, 1, args.length)));
        }

        return new ResolvedCommand(FriendCommandDefinitions.FRIEND, Arrays.asList(args));
    }

    private CommandExecutionContext context(CommandSource source, ResolvedCommand resolvedCommand) {
        UUID senderId = source instanceof Player player ? player.getUniqueId() : null;
        return new CommandExecutionContext(
                senderId,
                source instanceof Player player ? player.getUsername() : "Console",
                source instanceof Player,
                resolvedCommand.path(),
                resolvedCommand.args()
        );
    }

    private boolean hasPermission(CommandSource source, PermissionNode permission) {
        if (permission == null) {
            return true;
        }
        return permission.anyParentGranted(source::hasPermission);
    }

    private List<String> completeSubcommands(CommandSource source, String prefix) {
        return registry.definitions().stream()
                .filter(definition -> definition.path().startsWith(FriendCommandDefinitions.FRIEND))
                .filter(definition -> definition.path().segments().size() == FriendCommandDefinitions.FRIEND.segments().size() + 1)
                .filter(definition -> hasPermission(source, definition.permission()))
                .map(definition -> definition.path().commandName())
                .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(prefix))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    private String primaryUsage(CommandSource source) {
        return registry.definitions().stream()
                .filter(definition -> definition.path().startsWith(FriendCommandDefinitions.FRIEND))
                .filter(definition -> definition.path().segments().size() == FriendCommandDefinitions.FRIEND.segments().size() + 1)
                .filter(definition -> hasPermission(source, definition.permission()))
                .map(definition -> definition.path().commandName())
                .sorted(Comparator.naturalOrder())
                .reduce((left, right) -> left + " | " + right)
                .map(names -> "/friend <" + names + ">")
                .orElse("/friend");
    }

    private List<String> completeAdd(CommandSource source, List<String> args) {
        if (args.size() != 1 || !(source instanceof Player player)) {
            return List.of();
        }

        String prefix = args.get(0).toLowerCase();
        return plugin.getServer().getAllPlayers().stream()
                .filter(candidate -> !candidate.getUniqueId().equals(player.getUniqueId()))
                .filter(candidate -> !plugin.getFriendService().areFriends(player.getUniqueId(), candidate.getUniqueId()))
                .filter(candidate -> !plugin.getFriendService().requestPending(candidate.getUniqueId(), player.getUniqueId()))
                .filter(candidate -> !plugin.getFriendService().requestPending(player.getUniqueId(), candidate.getUniqueId()))
                .filter(candidate -> !plugin.getApplicationServices().blocklistService()
                        .hasEitherBlocked(player.getUniqueId(), candidate.getUniqueId()))
                .filter(candidate -> {
                    PlayerData playerData = plugin.getPlayerService().getPlayerData(candidate.getUniqueId());
                    return playerData == null || playerData.isAllowFriendRequests();
                })
                .map(Player::getUsername)
                .filter(name -> name.toLowerCase().startsWith(prefix))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    private List<String> completeFriends(CommandSource source, List<String> args) {
        if (args.size() != 1 || !(source instanceof Player player)) {
            return List.of();
        }

        Set<FriendshipData> friends = plugin.getFriendService().getFriendships(player.getUniqueId());
        return plugin.getApplicationServices().knownPlayerLookup()
                .suggestFriendshipPlayers(friends, player.getUniqueId(), args.get(0));
    }

    private List<String> completeBlock(CommandSource source, List<String> args) {
        if (args.size() != 1 || !(source instanceof Player player)) {
            return List.of();
        }

        String prefix = args.get(0).toLowerCase();
        return plugin.getServer().getAllPlayers().stream()
                .filter(candidate -> !candidate.getUniqueId().equals(player.getUniqueId()))
                .filter(candidate -> !plugin.getApplicationServices().blocklistService()
                        .isBlocked(player.getUniqueId(), candidate.getUniqueId()))
                .map(Player::getUsername)
                .filter(name -> name.toLowerCase().startsWith(prefix))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    private List<String> completeBlocked(CommandSource source, List<String> args) {
        if (args.size() != 1 || !(source instanceof Player player)) {
            return List.of();
        }

        return plugin.getApplicationServices().blocklistService().getBlockedPlayers(player.getUniqueId()).stream()
                .map(BlocklistData::getBlockedId)
                .map(playerId -> plugin.getApplicationServices().knownPlayerLookup().displayName(playerId))
                .filter(name -> name.toLowerCase().startsWith(args.get(0).toLowerCase()))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    private List<String> completePendingRequests(CommandSource source, List<String> args) {
        if (args.size() != 1 || !(source instanceof Player player)) {
            return List.of();
        }

        return plugin.getFriendService().getPendingRequests(player.getUniqueId()).stream()
                .map(FriendshipData::getRequesterId)
                .map(playerId -> plugin.getApplicationServices().knownPlayerLookup().displayName(playerId))
                .filter(name -> name.toLowerCase().startsWith(args.get(0).toLowerCase()))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    private List<String> completeSentRequests(CommandSource source, List<String> args) {
        if (args.size() != 1 || !(source instanceof Player player)) {
            return List.of();
        }

        List<String> targets = plugin.getFriendService().getSentRequests(player.getUniqueId()).stream()
                .map(friendship -> friendship.getOtherPlayerId(player.getUniqueId()))
                .map(playerId -> plugin.getApplicationServices().knownPlayerLookup().displayName(playerId))
                .toList();
        return completeKnownNames(
                "all".startsWith(args.get(0).toLowerCase()) ? concat(targets, "all") : targets,
                args.get(0)
        );
    }

    private List<String> completeKnownNames(Collection<String> names, String prefix) {
        String normalizedPrefix = prefix == null ? "" : prefix.toLowerCase(Locale.ROOT);
        return names.stream()
                .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(normalizedPrefix))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    private List<String> concat(List<String> values, String value) {
        return java.util.stream.Stream.concat(values.stream(), java.util.stream.Stream.of(value)).toList();
    }

    private record ResolvedCommand(CommandPath path, List<String> args) {
    }
}
