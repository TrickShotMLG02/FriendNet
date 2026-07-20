package com.trickshotmlg.friendnet.adapter_spigot.Commands;

import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
import com.trickshotmlg.friendnet.adapter_spigot.GUIs.FriendsGUI;
import com.trickshotmlg.friendnet.adapter_spigot.GUIs.RequestsGUI;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.SpigotCommandResultRenderer;
import com.trickshotmlg.friendnet.core.application.KnownPlayerLookup;
import com.trickshotmlg.friendnet.core.application.command.CommandFeedbackUseCases;
import com.trickshotmlg.friendnet.core.application.command.CommandRegistry;
import com.trickshotmlg.friendnet.core.application.command.CommandUseCaseResult;
import com.trickshotmlg.friendnet.core.application.command.FriendCommandDefinitions;
import com.trickshotmlg.friendnet.core.application.command.FriendCommandUseCases;
import com.trickshotmlg.friendnet.core_api.models.BlocklistData;
import com.trickshotmlg.friendnet.core_api.models.FriendshipData;
import com.trickshotmlg.friendnet.core_api.models.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class FriendCommand extends AbstractCommand {

    public FriendCommand(FriendNetPlugin plugin) {
        super(plugin, FriendCommandDefinitions.FRIEND, createRegistry(plugin));

        registerTabCompletion(FriendCommandDefinitions.ADD.path(), this::completeAdd);
        registerTabCompletion(FriendCommandDefinitions.REMOVE.path(), this::completeFriends);
        registerTabCompletion(FriendCommandDefinitions.BLOCK.path(), this::completeBlock);
        registerTabCompletion(FriendCommandDefinitions.UNBLOCK.path(), this::completeBlocked);
        registerTabCompletion(FriendCommandDefinitions.ACCEPT.path(), this::completePendingRequests);
        registerTabCompletion(FriendCommandDefinitions.DENY.path(), this::completePendingRequests);
        registerTabCompletion(FriendCommandDefinitions.CANCEL.path(), this::completeSentRequests);

        register(plugin, "friend");
        register(plugin, "friends");
    }

    private static CommandRegistry createRegistry(FriendNetPlugin plugin) {
        CommandRegistry registry = FriendCommandDefinitions.registryWithUsageHandlers();
        if (plugin.isProxyBackendMode()) {
            registerProxyBackendHandlers(plugin, registry);
            return registry;
        }

        FriendCommandUseCases useCases = plugin.getApplicationServices().friendCommandUseCases();

        registry.register(FriendCommandDefinitions.ADD, context -> {
            if (context.args().isEmpty()) {
                return CommandFeedbackUseCases.usage(FriendCommandDefinitions.ADD.usage());
            }

            return resolveTarget(plugin, context.args().get(0))
                    .map(target -> useCases.sendFriendRequest(
                            context.senderId(),
                            context.senderName(),
                            target
                    ))
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
                    .map(target -> useCases.acceptRequest(
                            context.senderId(),
                            context.senderName(),
                            target.playerId(),
                            target.displayName()
                    ))
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

        registry.override(FriendCommandDefinitions.LIST.path(), (context, next) -> openFriendsGui(plugin, context.senderId(), context.args()));
        registry.override(FriendCommandDefinitions.FRIENDS_ALIAS.path(), (context, next) -> openFriendsGui(plugin, context.senderId(), context.args()));
        registry.override(FriendCommandDefinitions.REQUESTS.path(), (context, next) -> openRequestsGui(plugin, context.senderId(), context.args()));
        registry.override(FriendCommandDefinitions.RELOAD.path(), (context, next) ->
                CommandFeedbackUseCases.reload(plugin.reloadPluginConfigs())
        );

        return registry;
    }

    private static void registerProxyBackendHandlers(FriendNetPlugin plugin, CommandRegistry registry) {
        FriendCommandDefinitions.all().stream()
                .filter(definition -> !definition.path().equals(FriendCommandDefinitions.RELOAD.path()))
                .filter(definition -> !definition.path().equals(FriendCommandDefinitions.LIST.path()))
                .filter(definition -> !definition.path().equals(FriendCommandDefinitions.FRIENDS_ALIAS.path()))
                .filter(definition -> !definition.path().equals(FriendCommandDefinitions.REQUESTS.path()))
                .forEach(definition -> registry.override(definition.path(), (context, next) ->
                        definition.platformSpecific()
                                ? CommandFeedbackUseCases.proxyBackendGuiUnavailable()
                                : CommandFeedbackUseCases.proxyBackendCommandDisabled()
                ));
        registry.override(FriendCommandDefinitions.LIST.path(), (context, next) -> openFriendsGui(plugin, context.senderId(), context.args()));
        registry.override(FriendCommandDefinitions.FRIENDS_ALIAS.path(), (context, next) -> openFriendsGui(plugin, context.senderId(), context.args()));
        registry.override(FriendCommandDefinitions.REQUESTS.path(), (context, next) -> openRequestsGui(plugin, context.senderId(), context.args()));
        registry.override(FriendCommandDefinitions.RELOAD.path(), (context, next) ->
                CommandFeedbackUseCases.reload(plugin.reloadPluginConfigs())
        );
    }

    private static Optional<KnownPlayerLookup.KnownPlayer> resolveTarget(FriendNetPlugin plugin, String name) {
        return plugin.getApplicationServices().knownPlayerLookup().resolve(name);
    }

    private static CommandUseCaseResult openFriendsGui(FriendNetPlugin plugin, UUID senderId, List<String> args) {
        if (!args.isEmpty()) {
            return CommandFeedbackUseCases.usage(FriendCommandDefinitions.LIST.usage());
        }

        Player player = Bukkit.getPlayer(senderId);
        if (player == null) {
            return CommandFeedbackUseCases.playersOnly();
        }

        plugin.getFriendGuiService().friendListView(player).whenComplete((viewData, throwable) ->
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (throwable != null) {
                        SpigotCommandResultRenderer.render(player, CommandFeedbackUseCases.proxyBackendGuiUnavailable());
                        return;
                    }

                    new FriendsGUI(plugin, player, viewData).open();
                })
        );
        return CommandUseCaseResult.builder(true).build();
    }

    private static CommandUseCaseResult openRequestsGui(FriendNetPlugin plugin, UUID senderId, List<String> args) {
        if (!args.isEmpty()) {
            return CommandFeedbackUseCases.usage(FriendCommandDefinitions.REQUESTS.usage());
        }

        Player player = Bukkit.getPlayer(senderId);
        if (player == null) {
            return CommandFeedbackUseCases.playersOnly();
        }

        plugin.getFriendGuiService().friendListView(player).whenComplete((viewData, throwable) ->
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (throwable != null) {
                        SpigotCommandResultRenderer.render(player, CommandFeedbackUseCases.proxyBackendGuiUnavailable());
                        return;
                    }

                    new RequestsGUI(plugin, player, viewData).openWithParent(new FriendsGUI(plugin, player, viewData));
                })
        );
        return CommandUseCaseResult.builder(true).build();
    }

    private void register(FriendNetPlugin plugin, String commandName) {
        PluginCommand command = plugin.getCommand(commandName);
        if (command != null) {
            command.setExecutor(this);
            command.setTabCompleter(this);
        }
    }

    private List<String> completeAdd(org.bukkit.command.CommandSender sender, List<String> args) {
        if (args.size() != 1 || !(sender instanceof Player player)) {
            return List.of();
        }

        FriendNetPlugin plugin = getPlugin();
        String prefix = args.get(0).toLowerCase();
        return Bukkit.getOnlinePlayers().stream()
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
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(prefix))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    private List<String> completeFriends(org.bukkit.command.CommandSender sender, List<String> args) {
        if (args.size() != 1 || !(sender instanceof Player player)) {
            return List.of();
        }

        Set<FriendshipData> friends = getPlugin().getFriendService().getFriendships(player.getUniqueId());
        return getPlugin().getApplicationServices().knownPlayerLookup()
                .suggestFriendshipPlayers(friends, player.getUniqueId(), args.get(0));
    }

    private List<String> completeBlock(org.bukkit.command.CommandSender sender, List<String> args) {
        if (args.size() != 1 || !(sender instanceof Player player)) {
            return List.of();
        }

        FriendNetPlugin plugin = getPlugin();
        String prefix = args.get(0).toLowerCase();
        return Bukkit.getOnlinePlayers().stream()
                .filter(candidate -> !candidate.getUniqueId().equals(player.getUniqueId()))
                .filter(candidate -> !plugin.getApplicationServices().blocklistService()
                        .isBlocked(player.getUniqueId(), candidate.getUniqueId()))
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(prefix))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    private List<String> completeBlocked(org.bukkit.command.CommandSender sender, List<String> args) {
        if (args.size() != 1 || !(sender instanceof Player player)) {
            return List.of();
        }

        return getPlugin().getApplicationServices().blocklistService().getBlockedPlayers(player.getUniqueId()).stream()
                .map(BlocklistData::getBlockedId)
                .map(playerId -> getPlugin().getApplicationServices().knownPlayerLookup().displayName(playerId))
                .filter(name -> name.toLowerCase().startsWith(args.get(0).toLowerCase()))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    private List<String> completePendingRequests(org.bukkit.command.CommandSender sender, List<String> args) {
        if (args.size() != 1 || !(sender instanceof Player player)) {
            return List.of();
        }

        return getPlugin().getFriendService().getPendingRequests(player.getUniqueId()).stream()
                .map(FriendshipData::getRequesterId)
                .map(playerId -> getPlugin().getApplicationServices().knownPlayerLookup().displayName(playerId))
                .filter(name -> name.toLowerCase().startsWith(args.get(0).toLowerCase()))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    private List<String> completeSentRequests(org.bukkit.command.CommandSender sender, List<String> args) {
        if (args.size() != 1 || !(sender instanceof Player player)) {
            return List.of();
        }

        List<String> targets = getPlugin().getFriendService().getSentRequests(player.getUniqueId()).stream()
                .map(friendship -> friendship.getOtherPlayerId(player.getUniqueId()))
                .map(playerId -> getPlugin().getApplicationServices().knownPlayerLookup().displayName(playerId))
                .toList();
        return completeKnownNames(
                "all".startsWith(args.get(0).toLowerCase()) ? concat(targets, "all") : targets,
                args.get(0)
        );
    }

    private List<String> concat(List<String> values, String value) {
        return java.util.stream.Stream.concat(values.stream(), java.util.stream.Stream.of(value)).toList();
    }
}
