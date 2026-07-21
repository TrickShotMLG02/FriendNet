package com.trickshotmlg.friendnet.adapter_spigot.Services;

import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
import com.trickshotmlg.friendnet.adapter_spigot.GUIs.FriendsGUI;
import com.trickshotmlg.friendnet.adapter_spigot.GUIs.RequestsGUI;
import com.trickshotmlg.friendnet.adapter_spigot.SpigotPlayer;
import com.trickshotmlg.friendnet.core.Logger;
import com.trickshotmlg.friendnet.core.application.command.CommandDefinition;
import com.trickshotmlg.friendnet.core.application.command.FriendCommandDefinitions;
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
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyBackendGuiType;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyBackendCommandPermissionsPayload;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyBackendCommandPermissionsPayloadCodec;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyDisplayNameUpdatePayload;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyDisplayNameUpdatePayloadCodec;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyFriendListViewPayload;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyFriendListViewPayloadCodec;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyOpenBackendGuiPayloadCodec;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

public class SpigotProxyMessagingClient implements PluginMessageListener {

    private static final long REQUEST_TIMEOUT_TICKS = 100L;
    private static final long HANDSHAKE_INTERVAL_TICKS = 20L * 30L;

    private final FriendNetPlugin plugin;
    private final Map<UUID, PendingRequest> pendingRequests = new ConcurrentHashMap<>();
    private volatile boolean handshakeComplete;
    private BukkitTask handshakeTask;

    public SpigotProxyMessagingClient(FriendNetPlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, FriendNetProxyProtocol.CHANNEL);
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, FriendNetProxyProtocol.CHANNEL, this);
        handshakeTask = plugin.getServer().getScheduler().runTaskTimer(plugin, this::sendHandshakeWithAnyOnlinePlayer, HANDSHAKE_INTERVAL_TICKS, HANDSHAKE_INTERVAL_TICKS);
    }

    public void unregister() {
        if (handshakeTask != null) {
            handshakeTask.cancel();
            handshakeTask = null;
        }
        plugin.getServer().getMessenger().unregisterOutgoingPluginChannel(plugin, FriendNetProxyProtocol.CHANNEL);
        plugin.getServer().getMessenger().unregisterIncomingPluginChannel(plugin, FriendNetProxyProtocol.CHANNEL, this);
        pendingRequests.values().forEach(request -> {
            request.timeoutTask().cancel();
            request.future().cancel(false);
        });
        pendingRequests.clear();
    }

    public boolean sendHandshakeWithAnyOnlinePlayer() {
        Player player = plugin.getServer().getOnlinePlayers().stream().findFirst().orElse(null);
        if (player == null) {
            return false;
        }

        sendHandshake(player);
        return true;
    }

    public CompletableFuture<ProxyFriendListViewPayload> requestFriendListView(Player player) {
        ProxyProtocolMessage request = ProxyProtocolCodec.request(
                ProxyRequestType.FRIEND_LIST_VIEW,
                player.getUniqueId(),
                "",
                new byte[0]
        );

        CompletableFuture<ProxyProtocolMessage> responseFuture = send(player, request);
        return responseFuture.thenApply(response -> ProxyFriendListViewPayloadCodec.decode(response.payload()));
    }

    public CompletableFuture<Boolean> sendHandshake(Player player) {
        ProxyProtocolMessage request = ProxyProtocolCodec.request(
                ProxyRequestType.HANDSHAKE,
                player.getUniqueId(),
                "",
                new byte[0]
        );

        return send(player, request)
                .thenApply(response -> {
                    handshakeComplete = true;
                    return true;
                })
                .whenComplete((ignored, throwable) -> {
                    if (throwable != null && plugin.isProxyBackendMode()) {
                        plugin.disableForProxyAuthenticationFailure("Could not complete FriendNet proxy handshake: " + throwable.getMessage());
                    }
                });
    }

    public CompletableFuture<ProxyActionResponsePayload> executeFriendAction(Player player, ProxyActionRequestPayload actionRequest) {
        ProxyProtocolMessage request = ProxyProtocolCodec.request(
                ProxyRequestType.FRIEND_ACTION_EXECUTE,
                player.getUniqueId(),
                "",
                ProxyActionRequestPayloadCodec.encode(actionRequest)
        );

        CompletableFuture<ProxyProtocolMessage> responseFuture = send(player, request);
        return responseFuture.thenApply(response -> ProxyActionResponsePayloadCodec.decode(response.payload()));
    }

    public void sendDisplayNameUpdate(Player player) {
        ProxyProtocolMessage request = ProxyProtocolCodec.request(
                ProxyRequestType.DISPLAY_NAME_UPDATE,
                player.getUniqueId(),
                "",
                ProxyDisplayNameUpdatePayloadCodec.encode(new ProxyDisplayNameUpdatePayload(player.getDisplayName()))
        );

        send(player, request).exceptionally(throwable -> {
            Logger.debug("Could not send display name update to proxy: " + throwable.getMessage());
            return null;
        });
    }

    public void sendBackendCommandPermissions(Player player) {
        SpigotPlayer platformPlayer = new SpigotPlayer(player);
        List<String> allowedCommandPaths = List.of(
                        FriendCommandDefinitions.RELOAD,
                        FriendCommandDefinitions.PROXY,
                        FriendCommandDefinitions.PROXY_SYNC,
                        FriendCommandDefinitions.PROXY_HANDSHAKE,
                        FriendCommandDefinitions.PROXY_RELOAD
                ).stream()
                .filter(definition -> definition.permission().has(platformPlayer))
                .map(CommandDefinition::path)
                .map(Object::toString)
                .toList();

        ProxyProtocolMessage request = ProxyProtocolCodec.request(
                ProxyRequestType.BACKEND_COMMAND_PERMISSIONS,
                player.getUniqueId(),
                "",
                ProxyBackendCommandPermissionsPayloadCodec.encode(new ProxyBackendCommandPermissionsPayload(allowedCommandPaths))
        );

        send(player, request).exceptionally(throwable -> {
            Logger.debug("Could not send backend command permissions to proxy: " + throwable.getMessage());
            return null;
        });
    }

    public int syncOnlineDisplayNames() {
        int count = 0;
        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            sendDisplayNameUpdate(onlinePlayer);
            count++;
        }
        return count;
    }

    public boolean isHandshakeComplete() {
        return handshakeComplete;
    }

    private CompletableFuture<ProxyProtocolMessage> send(Player player, ProxyProtocolMessage request) {
        CompletableFuture<ProxyProtocolMessage> future = new CompletableFuture<>();
        BukkitTask timeoutTask = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            PendingRequest removed = pendingRequests.remove(request.correlationId());
            if (removed != null) {
                removed.future().completeExceptionally(new TimeoutException("Proxy request timed out: " + request.requestType()));
            }
        }, REQUEST_TIMEOUT_TICKS);

        pendingRequests.put(request.correlationId(), new PendingRequest(future, timeoutTask));
        player.sendPluginMessage(plugin, FriendNetProxyProtocol.CHANNEL, ProxyProtocolCodec.encodeSigned(request, plugin.getConnectionToken()));
        return future;
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!FriendNetProxyProtocol.CHANNEL.equals(channel)) {
            return;
        }

        try {
            ProxyProtocolMessage response = ProxyProtocolCodec.decode(message);
            ProxyProtocolCodec.verify(response, plugin.getConnectionToken(), System.currentTimeMillis());
            if (response.kind() == ProxyMessageKind.REQUEST) {
                handleProxyRequest(player, response);
                return;
            }

            if (response.kind() != ProxyMessageKind.RESPONSE) {
                return;
            }

            PendingRequest pendingRequest = pendingRequests.remove(response.correlationId());
            if (pendingRequest == null) {
                Logger.warn("Received proxy response without pending request: " + response.correlationId());
                return;
            }

            pendingRequest.timeoutTask().cancel();
            if (response.responseStatus() == ProxyResponseStatus.ERROR) {
                pendingRequest.future().completeExceptionally(new ProxyProtocolException(response.errorCode(), "Proxy request failed: " + response.errorCode()));
                return;
            }

            pendingRequest.future().complete(response);
        } catch (ProxyProtocolException e) {
            Logger.warn("Rejected proxy response: " + e.getMessage());
            if (e.getErrorCode() == ProxyErrorCode.AUTHENTICATION_FAILED) {
                plugin.disableForProxyAuthenticationFailure("Rejected FriendNet proxy response because authentication failed.");
            }
        } catch (RuntimeException e) {
            Logger.error("Could not handle proxy response", e);
        }
    }

    private void handleProxyRequest(Player player, ProxyProtocolMessage request) {
        if (!player.getUniqueId().equals(request.playerId())) {
            Logger.warn("Rejected backend GUI request for mismatched player: " + request.correlationId());
            return;
        }

        switch (request.requestType()) {
            case OPEN_BACKEND_GUI -> handleOpenBackendGui(player, request);
            case BACKEND_RELOAD -> handleBackendReload(player, request);
            default -> Logger.warn("Rejected unsupported proxy-to-backend request: " + request.requestType());
        }
    }

    private void handleOpenBackendGui(Player player, ProxyProtocolMessage request) {
        ProxyBackendGuiType guiType = ProxyOpenBackendGuiPayloadCodec.decode(request.payload());
        plugin.getFriendGuiService().friendListView(player).whenComplete((viewData, throwable) ->
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    if (throwable != null) {
                        Logger.warn("Could not open backend GUI from proxy request: " + throwable.getMessage());
                        sendProxyResponse(player, request, ProxyResponseStatus.ERROR, ProxyErrorCode.INTERNAL_ERROR);
                        return;
                    }

                    switch (guiType) {
                        case FRIENDS -> new FriendsGUI(plugin, player, viewData).open();
                        case REQUESTS -> new RequestsGUI(plugin, player, viewData).openWithParent(new FriendsGUI(plugin, player, viewData));
                    }
                    sendProxyResponse(player, request, ProxyResponseStatus.SUCCESS, ProxyErrorCode.NONE);
                })
        );
    }

    private void handleBackendReload(Player player, ProxyProtocolMessage request) {
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            boolean success = plugin.reloadPluginConfigs();
            sendProxyResponse(
                    player,
                    request,
                    success ? ProxyResponseStatus.SUCCESS : ProxyResponseStatus.ERROR,
                    success ? ProxyErrorCode.NONE : ProxyErrorCode.INTERNAL_ERROR
            );
        });
    }

    private void sendProxyResponse(Player player, ProxyProtocolMessage request, ProxyResponseStatus status, ProxyErrorCode errorCode) {
        ProxyProtocolMessage response = ProxyProtocolCodec.response(request, status, errorCode, new byte[0]);
        player.sendPluginMessage(plugin, FriendNetProxyProtocol.CHANNEL, ProxyProtocolCodec.encodeSigned(response, plugin.getConnectionToken()));
    }

    private record PendingRequest(
            CompletableFuture<ProxyProtocolMessage> future,
            BukkitTask timeoutTask
    ) {
    }
}
