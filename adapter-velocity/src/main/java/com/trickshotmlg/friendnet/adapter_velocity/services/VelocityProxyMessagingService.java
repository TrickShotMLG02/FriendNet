package com.trickshotmlg.friendnet.adapter_velocity.services;

import com.trickshotmlg.friendnet.adapter_velocity.FriendNetVelocityPlugin;
import com.trickshotmlg.friendnet.core.Logger;
import com.trickshotmlg.friendnet.core.application.command.FriendCommandDefinitions;
import com.trickshotmlg.friendnet.core.application.command.FriendListViewData;
import com.trickshotmlg.friendnet.core_api.models.FriendshipData;
import com.trickshotmlg.friendnet.core_api.models.NetworkPlayerPresence;
import com.trickshotmlg.friendnet.core_api.proxy.FriendNetProxyProtocol;
import com.trickshotmlg.friendnet.core_api.proxy.ProxyErrorCode;
import com.trickshotmlg.friendnet.core_api.proxy.ProxyMessageKind;
import com.trickshotmlg.friendnet.core_api.proxy.ProxyProtocolCodec;
import com.trickshotmlg.friendnet.core_api.proxy.ProxyProtocolException;
import com.trickshotmlg.friendnet.core_api.proxy.ProxyProtocolMessage;
import com.trickshotmlg.friendnet.core_api.proxy.ProxyRequestType;
import com.trickshotmlg.friendnet.core_api.proxy.ProxyResponseStatus;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyFriendEntry;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyFriendListViewPayload;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyFriendListViewPayloadCodec;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class VelocityProxyMessagingService {

    public static final MinecraftChannelIdentifier CHANNEL = MinecraftChannelIdentifier.from(FriendNetProxyProtocol.CHANNEL);

    private final FriendNetVelocityPlugin plugin;

    public VelocityProxyMessagingService(FriendNetVelocityPlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        plugin.getServer().getChannelRegistrar().register(CHANNEL);
        plugin.getServer().getEventManager().register(plugin, this);
    }

    public void unregister() {
        plugin.getServer().getChannelRegistrar().unregister(CHANNEL);
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

        ProxyProtocolMessage request;
        try {
            request = ProxyProtocolCodec.decode(event.getData());
        } catch (ProxyProtocolException e) {
            Logger.warn("Rejected malformed FriendNet proxy message from " + connection.getServerInfo().getName() + ": " + e.getMessage());
            return;
        }

        try {
            validate(request, event, connection);
            send(connection, handleRequest(request));
        } catch (ProxyProtocolException e) {
            Logger.warn("Rejected FriendNet proxy request " + request.correlationId() + ": " + e.getMessage());
            send(connection, ProxyProtocolCodec.response(request, ProxyResponseStatus.ERROR, e.getErrorCode(), new byte[0]));
        } catch (RuntimeException e) {
            Logger.error("Could not handle FriendNet proxy request " + request.correlationId(), e);
            send(connection, ProxyProtocolCodec.response(request, ProxyResponseStatus.ERROR, ProxyErrorCode.INTERNAL_ERROR, new byte[0]));
        }
    }

    private void validate(ProxyProtocolMessage request, PluginMessageEvent event, ServerConnection connection) {
        ProxyProtocolCodec.verify(request, plugin.getConnectionToken(), System.currentTimeMillis());
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
    }

    private ProxyProtocolMessage handleRequest(ProxyProtocolMessage request) {
        if (request.requestType() != ProxyRequestType.FRIEND_LIST_VIEW) {
            throw new ProxyProtocolException(ProxyErrorCode.UNKNOWN_REQUEST, "Unknown request type: " + request.requestType());
        }

        Player player = plugin.getServer().getPlayer(request.playerId())
                .orElseThrow(() -> new ProxyProtocolException(ProxyErrorCode.PLAYER_REQUIRED, "Player is not connected to proxy."));

        if (!player.hasPermission(FriendCommandDefinitions.LIST.permission().getPermissionPrefixed())) {
            throw new ProxyProtocolException(ProxyErrorCode.PERMISSION_DENIED, "Player lacks permission for friend list view.");
        }

        FriendListViewData viewData = plugin.getApplicationServices()
                .friendCommandUseCases()
                .listViewData(request.playerId());
        ProxyFriendListViewPayload payload = new ProxyFriendListViewPayload(
                toEntries(request.playerId(), viewData.friends()),
                toEntries(request.playerId(), viewData.pendingRequests())
        );

        return ProxyProtocolCodec.response(
                request,
                ProxyResponseStatus.SUCCESS,
                ProxyErrorCode.NONE,
                ProxyFriendListViewPayloadCodec.encode(payload)
        );
    }

    private List<ProxyFriendEntry> toEntries(UUID viewerId, List<FriendshipData> friendships) {
        return friendships.stream()
                .map(friendship -> toEntry(friendship.getOtherPlayerId(viewerId)))
                .toList();
    }

    private ProxyFriendEntry toEntry(UUID playerId) {
        Optional<NetworkPlayerPresence> presence = plugin.getNetworkAuthorityService().getPresence(playerId);
        String displayName = presence
                .map(NetworkPlayerPresence::displayName)
                .filter(name -> !name.isBlank())
                .orElseGet(() -> plugin.getApplicationServices().knownPlayerLookup().displayName(playerId));
        boolean online = presence
                .map(status -> status.online() && status.visibleOnline())
                .orElse(false);
        String serverName = presence
                .map(NetworkPlayerPresence::serverName)
                .orElse("");

        return new ProxyFriendEntry(playerId, displayName, online, serverName);
    }

    private void send(ServerConnection connection, ProxyProtocolMessage response) {
        connection.sendPluginMessage(CHANNEL, ProxyProtocolCodec.encodeSigned(response, plugin.getConnectionToken()));
    }
}
