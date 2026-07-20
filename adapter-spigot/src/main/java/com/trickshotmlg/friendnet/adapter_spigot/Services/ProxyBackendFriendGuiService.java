package com.trickshotmlg.friendnet.adapter_spigot.Services;

import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
import com.trickshotmlg.friendnet.core_api.models.PlayerData;
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
                .thenApply(payload -> {
                    FriendGuiViewData viewData = FriendGuiViewData.fromProxyPayload(player.getUniqueId(), payload);
                    applyViewDataToLocalPlayer(player, viewData);
                    return viewData;
                });
    }

    @Override
    public CompletableFuture<ProxyActionResponsePayload> executeAction(Player player, ProxyActionRequestPayload actionRequest) {
        return plugin.getProxyMessagingClient()
                .executeFriendAction(player, actionRequest)
                .thenApply(response -> {
                    if (response.friendListView() != null) {
                        applyViewDataToLocalPlayer(
                                player,
                                FriendGuiViewData.fromProxyPayload(player.getUniqueId(), response.friendListView())
                        );
                    }
                    return response;
                });
    }

    private void applyViewDataToLocalPlayer(Player player, FriendGuiViewData viewData) {
        PlayerData playerData = plugin.getPlayerService().getPlayerData(player.getUniqueId());
        if (playerData == null) {
            playerData = plugin.getPlayerService().initPlayer(player.getUniqueId());
        }
        viewData.applySettingsTo(playerData);
    }

}
