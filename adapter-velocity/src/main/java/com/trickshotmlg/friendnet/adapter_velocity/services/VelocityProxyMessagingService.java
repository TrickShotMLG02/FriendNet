package com.trickshotmlg.friendnet.adapter_velocity.services;

import com.trickshotmlg.friendnet.adapter_velocity.FriendNetVelocityPlugin;
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
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyActionType;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyFriendListViewPayloadCodec;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;

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
            Player player = validate(request, event, connection);
            send(connection, handleRequest(request, player));
        } catch (ProxyProtocolException e) {
            Logger.warn("Rejected FriendNet proxy request " + request.correlationId() + ": " + e.getMessage());
            send(connection, ProxyProtocolCodec.response(request, ProxyResponseStatus.ERROR, e.getErrorCode(), new byte[0]));
        } catch (RuntimeException e) {
            Logger.error("Could not handle FriendNet proxy request " + request.correlationId(), e);
            send(connection, ProxyProtocolCodec.response(request, ProxyResponseStatus.ERROR, ProxyErrorCode.INTERNAL_ERROR, new byte[0]));
        }
    }

    private Player validate(ProxyProtocolMessage request, PluginMessageEvent event, ServerConnection connection) {
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

        return player;
    }

    private ProxyProtocolMessage handleRequest(ProxyProtocolMessage request, Player player) {
        return switch (request.requestType()) {
            case FRIEND_LIST_VIEW -> handleFriendListView(request, player);
            case FRIEND_ACTION_EXECUTE -> handleFriendAction(request, player);
            case OPEN_BACKEND_GUI -> throw new ProxyProtocolException(ProxyErrorCode.UNKNOWN_REQUEST, "Backend GUI requests are proxy-to-backend only.");
        };
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

    private ProxyProtocolMessage handleFriendAction(ProxyProtocolMessage request, Player player) {
        ProxyActionRequestPayload actionRequest = ProxyActionRequestPayloadCodec.decode(request.payload());
        ensurePermission(player, definitionFor(actionRequest.actionType()));
        ProxyActionResponsePayload payload = plugin.getApplicationServices()
                .proxyActionDispatcher()
                .dispatch(player.getUniqueId(), player.getUsername(), actionRequest);
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
        };
    }

    private void send(ServerConnection connection, ProxyProtocolMessage response) {
        connection.sendPluginMessage(CHANNEL, ProxyProtocolCodec.encodeSigned(response, plugin.getConnectionToken()));
    }
}
