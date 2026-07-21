package com.trickshotmlg.friendnet.adapter_velocity.services;

import com.trickshotmlg.friendnet.adapter_velocity.FriendNetVelocityPlugin;
import com.trickshotmlg.friendnet.adapter_velocity.utils.VelocityFriendStatusNotifier;
import com.trickshotmlg.friendnet.core.Logger;
import com.trickshotmlg.friendnet.core.application.command.CommandDefinition;
import com.trickshotmlg.friendnet.core.application.command.FriendCommandDefinitions;
import com.trickshotmlg.friendnet.core_api.models.NetworkPlayerPresence;
import com.trickshotmlg.friendnet.core_api.models.PlayerData;
import com.trickshotmlg.friendnet.core_api.proxy.FriendNetProxyProtocol;
import com.trickshotmlg.friendnet.core_api.proxy.ProxyErrorCode;
import com.trickshotmlg.friendnet.core_api.proxy.ProxyMessageKind;
import com.trickshotmlg.friendnet.core_api.proxy.ProxyProtocolCodec;
import com.trickshotmlg.friendnet.core_api.proxy.ProxyProtocolException;
import com.trickshotmlg.friendnet.core_api.proxy.ProxyProtocolMessage;
import com.trickshotmlg.friendnet.core_api.proxy.ProxyRequestType;
import com.trickshotmlg.friendnet.core_api.proxy.ProxyResponseStatus;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyActionRequestPayload;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyActionRequestPayloadCodec;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyActionResponsePayload;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyActionResponsePayloadCodec;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyActionType;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyBackendCommandPermissionsPayload;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyBackendCommandPermissionsPayloadCodec;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyBackendGuiType;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyDisplayNameUpdatePayload;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyDisplayNameUpdatePayloadCodec;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyFriendListViewPayloadCodec;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyMessagePayload;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyMessageRecipient;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyOpenBackendGuiPayloadCodec;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.scheduler.ScheduledTask;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class VelocityProxyMessagingService {

    public static final MinecraftChannelIdentifier CHANNEL = MinecraftChannelIdentifier.from(FriendNetProxyProtocol.CHANNEL);
    private static final long BACKEND_GUI_TIMEOUT_MILLIS = 1500L;
    private static final long DISPLAY_NAME_UPDATE_WAIT_MILLIS = 2500L;
    private static final long BACKEND_REGISTRATION_TTL_MILLIS = 45_000L;

    private final FriendNetVelocityPlugin plugin;
    private final Map<UUID, PendingBackendGuiRequest> pendingBackendGuiRequests = new ConcurrentHashMap<>();
    private final Map<UUID, ScheduledTask> pendingOnlineNotifications = new ConcurrentHashMap<>();
    private final Map<String, Long> registeredBackends = new ConcurrentHashMap<>();
    private final Map<UUID, Set<String>> backendCommandPermissions = new ConcurrentHashMap<>();

    public VelocityProxyMessagingService(FriendNetVelocityPlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        plugin.getServer().getChannelRegistrar().register(CHANNEL);
        plugin.getServer().getEventManager().register(plugin, this);
    }

    public void unregister() {
        plugin.getServer().getChannelRegistrar().unregister(CHANNEL);
        pendingBackendGuiRequests.values().forEach(request -> request.timeoutTask().cancel());
        pendingBackendGuiRequests.clear();
        pendingOnlineNotifications.values().forEach(ScheduledTask::cancel);
        pendingOnlineNotifications.clear();
        registeredBackends.clear();
        backendCommandPermissions.clear();
    }

    public void notifyOnlineWhenDisplayNameReady(UUID playerId) {
        if (playerId == null) {
            return;
        }

        ScheduledTask oldTask = pendingOnlineNotifications.remove(playerId);
        if (oldTask != null) {
            oldTask.cancel();
        }

        ScheduledTask task = plugin.getServer().getScheduler().buildTask(plugin, () -> {
            if (pendingOnlineNotifications.remove(playerId) != null) {
                VelocityFriendStatusNotifier.notifyOnline(plugin, playerId);
            }
        }).delay(DISPLAY_NAME_UPDATE_WAIT_MILLIS, TimeUnit.MILLISECONDS).schedule();
        pendingOnlineNotifications.put(playerId, task);
    }

    public boolean openBackendGui(Player player, ProxyBackendGuiType guiType, Runnable fallback) {
        ServerConnection connection = player.getCurrentServer().orElse(null);
        if (connection == null) {
            return false;
        }
        if (!isBackendRegistered(connection.getServerInfo().getName())) {
            Logger.debug("Skipping backend GUI request because FriendNet backend is not registered: " + connection.getServerInfo().getName());
            return false;
        }

        ProxyProtocolMessage request = ProxyProtocolCodec.request(
                ProxyRequestType.OPEN_BACKEND_GUI,
                player.getUniqueId(),
                "velocity",
                ProxyOpenBackendGuiPayloadCodec.encode(guiType)
        );
        ScheduledTask timeoutTask = plugin.getServer().getScheduler().buildTask(plugin, () -> {
            PendingBackendGuiRequest removed = pendingBackendGuiRequests.remove(request.correlationId());
            if (removed != null) {
                Logger.debug("Backend GUI request timed out; rendering Velocity text fallback: " + request.correlationId());
                removed.fallback().run();
            }
        }).delay(BACKEND_GUI_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS).schedule();

        pendingBackendGuiRequests.put(request.correlationId(), new PendingBackendGuiRequest(player.getUniqueId(), fallback, timeoutTask));
        boolean sent = connection.sendPluginMessage(CHANNEL, ProxyProtocolCodec.encodeSigned(request, plugin.getConnectionToken()));
        if (!sent) {
            PendingBackendGuiRequest removed = pendingBackendGuiRequests.remove(request.correlationId());
            if (removed != null) {
                removed.timeoutTask().cancel();
            }
            return false;
        }

        return true;
    }

    public boolean hasBackendCommandPermission(Player player, String commandPath) {
        Set<String> allowedCommandPaths = backendCommandPermissions.get(player.getUniqueId());
        return allowedCommandPaths != null && allowedCommandPaths.contains(commandPath);
    }

    @Subscribe
    public void onPluginMessage(PluginMessageEvent event) {
        if (!CHANNEL.equals(event.getIdentifier())) {
            return;
        }

        event.setResult(PluginMessageEvent.ForwardResult.handled());
        if (!(event.getSource() instanceof ServerConnection connection)) {
            Logger.warn("Rejected FriendNet proxy message from non-server source.");
            return;
        }

        ProxyProtocolMessage message;
        try {
            message = ProxyProtocolCodec.decode(event.getData());
            ProxyProtocolCodec.verify(message, plugin.getConnectionToken(), System.currentTimeMillis());
        } catch (ProxyProtocolException e) {
            Logger.warn("Rejected malformed FriendNet proxy message from " + connection.getServerInfo().getName() + ": " + e.getMessage());
            return;
        }

        try {
            if (message.kind() == ProxyMessageKind.RESPONSE) {
                handleBackendGuiResponse(message);
                return;
            }

            Player player = validateRequest(message, event, connection);
            send(connection, handleRequest(message, player));
        } catch (ProxyProtocolException e) {
            Logger.warn("Rejected FriendNet proxy request " + message.correlationId() + ": " + e.getMessage());
            send(connection, ProxyProtocolCodec.response(message, ProxyResponseStatus.ERROR, e.getErrorCode(), new byte[0]));
        } catch (RuntimeException e) {
            Logger.error("Could not handle FriendNet proxy request " + message.correlationId(), e);
            send(connection, ProxyProtocolCodec.response(message, ProxyResponseStatus.ERROR, ProxyErrorCode.INTERNAL_ERROR, new byte[0]));
        }
    }

    private Player validateRequest(ProxyProtocolMessage request, PluginMessageEvent event, ServerConnection connection) {
        if (request.kind() != ProxyMessageKind.REQUEST) {
            throw new ProxyProtocolException(ProxyErrorCode.BAD_REQUEST, "Expected request message.");
        }

        if (!(event.getTarget() instanceof Player player)) {
            throw new ProxyProtocolException(ProxyErrorCode.PLAYER_REQUIRED, "Proxy request requires a player target.");
        }

        if (!player.getUniqueId().equals(request.playerId())) {
            throw new ProxyProtocolException(ProxyErrorCode.AUTHENTICATION_FAILED, "Player identity does not match plugin message target.");
        }

        if (!request.sourceServer().isBlank() && !connection.getServerInfo().getName().equals(request.sourceServer())) {
            Logger.debug("FriendNet request source name differs from Velocity connection: declared="
                    + request.sourceServer() + ", actual=" + connection.getServerInfo().getName());
        }

        return player;
    }

    private void handleBackendGuiResponse(ProxyProtocolMessage response) {
        PendingBackendGuiRequest pendingRequest = pendingBackendGuiRequests.remove(response.correlationId());
        if (pendingRequest == null) {
            Logger.debug("Received backend GUI response without pending request: " + response.correlationId());
            return;
        }

        pendingRequest.timeoutTask().cancel();
        if (!pendingRequest.playerId().equals(response.playerId())) {
            Logger.warn("Rejected backend GUI response for mismatched player: " + response.correlationId());
            pendingRequest.fallback().run();
            return;
        }

        if (response.responseStatus() == ProxyResponseStatus.ERROR) {
            Logger.debug("Backend GUI request failed; rendering Velocity text fallback: "
                    + response.correlationId() + " (" + response.errorCode() + ")");
            pendingRequest.fallback().run();
        }
    }

    private ProxyProtocolMessage handleRequest(ProxyProtocolMessage request, Player player) {
        return switch (request.requestType()) {
            case HANDSHAKE -> handleHandshake(request, player);
            case FRIEND_LIST_VIEW -> handleFriendListView(request, player);
            case FRIEND_ACTION_EXECUTE -> handleFriendAction(request, player);
            case DISPLAY_NAME_UPDATE -> handleDisplayNameUpdate(request, player);
            case BACKEND_COMMAND_PERMISSIONS -> handleBackendCommandPermissions(request, player);
            case OPEN_BACKEND_GUI -> throw new ProxyProtocolException(ProxyErrorCode.UNKNOWN_REQUEST, "Backend GUI requests are proxy-to-backend only.");
        };
    }

    private ProxyProtocolMessage handleHandshake(ProxyProtocolMessage request, Player player) {
        player.getCurrentServer().ifPresent(connection ->
                registeredBackends.put(connection.getServerInfo().getName(), System.currentTimeMillis())
        );
        return ProxyProtocolCodec.response(request, ProxyResponseStatus.SUCCESS, ProxyErrorCode.NONE, new byte[0]);
    }

    private boolean isBackendRegistered(String serverName) {
        Long lastSeen = registeredBackends.get(serverName);
        if (lastSeen == null) {
            return false;
        }

        if (System.currentTimeMillis() - lastSeen > BACKEND_REGISTRATION_TTL_MILLIS) {
            registeredBackends.remove(serverName, lastSeen);
            return false;
        }

        return true;
    }

    private ProxyProtocolMessage handleFriendListView(ProxyProtocolMessage request, Player player) {
        ensurePermission(player, FriendCommandDefinitions.LIST);
        return ProxyProtocolCodec.response(
                request,
                ProxyResponseStatus.SUCCESS,
                ProxyErrorCode.NONE,
                ProxyFriendListViewPayloadCodec.encode(plugin.getApplicationServices().friendListViewPayload(request.playerId()))
        );
    }

    private ProxyProtocolMessage handleDisplayNameUpdate(ProxyProtocolMessage request, Player player) {
        ProxyDisplayNameUpdatePayload payload = ProxyDisplayNameUpdatePayloadCodec.decode(request.payload());
        updateDisplayName(player, payload.displayName());
        flushPendingOnlineNotification(player.getUniqueId());
        return ProxyProtocolCodec.response(request, ProxyResponseStatus.SUCCESS, ProxyErrorCode.NONE, new byte[0]);
    }

    private ProxyProtocolMessage handleBackendCommandPermissions(ProxyProtocolMessage request, Player player) {
        ProxyBackendCommandPermissionsPayload payload = ProxyBackendCommandPermissionsPayloadCodec.decode(request.payload());
        backendCommandPermissions.put(player.getUniqueId(), Set.copyOf(payload.allowedCommandPaths()));
        return ProxyProtocolCodec.response(request, ProxyResponseStatus.SUCCESS, ProxyErrorCode.NONE, new byte[0]);
    }

    private void updateDisplayName(Player player, String displayName) {
        if (displayName == null || displayName.isBlank()) {
            return;
        }

        UUID playerId = player.getUniqueId();
        PlayerData playerData = plugin.getPlayerService().getPlayerData(playerId);
        if (playerData == null) {
            playerData = plugin.getDatabaseService()
                    .find(playerId, PlayerData.class)
                    .orElseGet(() -> plugin.getPlayerService().initPlayer(playerId));
            plugin.getPlayerService().putPlayerData(playerData);
        }

        playerData.setLastDisplayName(displayName);
        plugin.getPlayerDataSaveQueue().markDirty(playerData);

        String serverName = player.getCurrentServer()
                .map(connection -> connection.getServerInfo().getName())
                .orElse(playerData.getLastServerName());
        plugin.getNetworkAuthorityService().setPresence(new NetworkPlayerPresence(
                playerId,
                player.getUsername(),
                displayName,
                serverName,
                true,
                playerData.isShowOnlineStatus(),
                playerData.getLastSeen()
        ));
    }

    private void flushPendingOnlineNotification(UUID playerId) {
        ScheduledTask task = pendingOnlineNotifications.remove(playerId);
        if (task == null) {
            return;
        }

        task.cancel();
        VelocityFriendStatusNotifier.notifyOnline(plugin, playerId);
    }

    private ProxyProtocolMessage handleFriendAction(ProxyProtocolMessage request, Player player) {
        ProxyActionRequestPayload actionRequest = ProxyActionRequestPayloadCodec.decode(request.payload());
        ensurePermission(player, definitionFor(actionRequest.actionType()));
        ProxyActionResponsePayload payload = plugin.getApplicationServices()
                .proxyActionDispatcher()
                .dispatch(player.getUniqueId(), player.getUsername(), actionRequest);
        payload = deliverPlayerMessages(payload);
        return ProxyProtocolCodec.response(
                request,
                ProxyResponseStatus.SUCCESS,
                ProxyErrorCode.NONE,
                ProxyActionResponsePayloadCodec.encode(payload)
        );
    }

    private void ensurePermission(Player player, CommandDefinition definition) {
        if (!definition.permission().anyParentGranted(player::hasPermission)) {
            throw new ProxyProtocolException(ProxyErrorCode.PERMISSION_DENIED, "Player lacks permission: " + definition.permission().getPermissionPrefixed());
        }
    }

    private CommandDefinition definitionFor(ProxyActionType actionType) {
        return switch (actionType) {
            case ADD_FRIEND -> FriendCommandDefinitions.ADD;
            case REMOVE_FRIEND -> FriendCommandDefinitions.REMOVE;
            case ACCEPT_REQUEST -> FriendCommandDefinitions.ACCEPT;
            case ACCEPT_ALL_REQUESTS -> FriendCommandDefinitions.ACCEPT_ALL;
            case DENY_REQUEST -> FriendCommandDefinitions.DENY;
            case DENY_ALL_REQUESTS -> FriendCommandDefinitions.DENY_ALL;
            case CANCEL_REQUEST, CANCEL_ALL_REQUESTS -> FriendCommandDefinitions.CANCEL;
            case BLOCK_PLAYER -> FriendCommandDefinitions.BLOCK;
            case UNBLOCK_PLAYER -> FriendCommandDefinitions.UNBLOCK;
            case CLEAR_BLOCKLIST -> FriendCommandDefinitions.BLOCK;
            case SET_FAVOURITE,
                 SET_ALLOW_FRIEND_REQUESTS,
                 SET_SHOW_ONLINE_STATUS,
                 SET_AUTO_ACCEPT_FRIENDS,
                 SET_FRIEND_REQUEST_NOTIFICATIONS,
                 SET_FRIEND_LIST_PUBLIC,
                 SET_LOCALE -> FriendCommandDefinitions.ROOT;
        };
    }

    private ProxyActionResponsePayload deliverPlayerMessages(ProxyActionResponsePayload payload) {
        List<ProxyMessagePayload> backendMessages = payload.messages().stream()
                .filter(message -> {
                    if (message.recipient() != ProxyMessageRecipient.PLAYER) {
                        return true;
                    }

                    plugin.getServer().getPlayer(message.recipientId()).ifPresent(target -> {
                        Map<String, Object> placeholders = message.placeholders().entrySet().stream()
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                        plugin.getMessageManager().send(target, message.key(), placeholders);
                    });
                    return false;
                })
                .toList();

        return new ProxyActionResponsePayload(payload.success(), backendMessages, payload.friendListView());
    }

    private void send(ServerConnection connection, ProxyProtocolMessage response) {
        connection.sendPluginMessage(CHANNEL, ProxyProtocolCodec.encodeSigned(response, plugin.getConnectionToken()));
    }

    private record PendingBackendGuiRequest(
            UUID playerId,
            Runnable fallback,
            ScheduledTask timeoutTask
    ) {
    }
}
