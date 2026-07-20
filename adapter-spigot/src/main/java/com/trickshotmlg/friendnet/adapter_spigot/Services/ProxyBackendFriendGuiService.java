package com.trickshotmlg.friendnet.adapter_spigot.Services;

import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyActionRequestPayload;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyActionResponsePayload;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

public class ProxyBackendFriendGuiService implements FriendGuiService {

    private final FriendNetPlugin plugin;

    public ProxyBackendFriendGuiService(FriendNetPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public CompletableFuture<FriendGuiViewData> friendListView(Player player) {
        return plugin.getProxyMessagingClient()
                .requestFriendListView(player)
                .thenApply(payload -> FriendGuiViewData.fromProxyPayload(player.getUniqueId(), payload));
    }

    @Override
    public CompletableFuture<ProxyActionResponsePayload> executeAction(Player player, ProxyActionRequestPayload actionRequest) {
        return plugin.getProxyMessagingClient().executeFriendAction(player, actionRequest);
    }

}
