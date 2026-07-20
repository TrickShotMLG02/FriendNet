package com.trickshotmlg.friendnet.adapter_spigot.Services;

import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
import com.trickshotmlg.friendnet.adapter_spigot.GUIs.FriendsGUI;
import com.trickshotmlg.friendnet.adapter_spigot.GUIs.RequestsGUI;
import com.trickshotmlg.friendnet.core.Logger;
import com.trickshotmlg.friendnet.core_api.proxy.FriendNetProxyProtocol;
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
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyFriendListViewPayload;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyFriendListViewPayloadCodec;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyOpenBackendGuiPayloadCodec;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

public class SpigotProxyMessagingClient implements PluginMessageListener {

    private static final long REQUEST_TIMEOUT_TICKS = 100L;

    private final FriendNetPlugin plugin;
    private final Map<UUID, PendingRequest> pendingRequests = new ConcurrentHashMap<>();

    public SpigotProxyMessagingClient(FriendNetPlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, FriendNetProxyProtocol.CHANNEL);
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, FriendNetProxyProtocol.CHANNEL, this);
    }

    public void unregister() {
        plugin.getServer().getMessenger().unregisterOutgoingPluginChannel(plugin, FriendNetProxyProtocol.CHANNEL);
        plugin.getServer().getMessenger().unregisterIncomingPluginChannel(plugin, FriendNetProxyProtocol.CHANNEL, this);
        pendingRequests.values().forEach(request -> request.future().cancel(false));
        pendingRequests.clear();
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
        } catch (RuntimeException e) {
            Logger.error("Could not handle proxy response", e);
        }
    }

    private void handleProxyRequest(Player player, ProxyProtocolMessage request) {
        if (!player.getUniqueId().equals(request.playerId())) {
            Logger.warn("Rejected backend GUI request for mismatched player: " + request.correlationId());
            return;
        }

        if (request.requestType() != ProxyRequestType.OPEN_BACKEND_GUI) {
            Logger.warn("Rejected unsupported proxy-to-backend request: " + request.requestType());
            return;
        }

        ProxyBackendGuiType guiType = ProxyOpenBackendGuiPayloadCodec.decode(request.payload());
        plugin.getFriendGuiService().friendListView(player).whenComplete((viewData, throwable) ->
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    if (throwable != null) {
                        Logger.warn("Could not open backend GUI from proxy request: " + throwable.getMessage());
                        return;
                    }

                    switch (guiType) {
                        case FRIENDS -> new FriendsGUI(plugin, player, viewData).open();
                        case REQUESTS -> new RequestsGUI(plugin, player, viewData).openWithParent(new FriendsGUI(plugin, player, viewData));
                    }
                })
        );
    }

    private record PendingRequest(
            CompletableFuture<ProxyProtocolMessage> future,
            BukkitTask timeoutTask
    ) {
    }
}
