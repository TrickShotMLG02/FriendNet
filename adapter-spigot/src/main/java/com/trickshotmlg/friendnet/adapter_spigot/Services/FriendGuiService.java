package com.trickshotmlg.friendnet.adapter_spigot.Services;

import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyActionRequestPayload;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyActionResponsePayload;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

public interface FriendGuiService {

    CompletableFuture<FriendGuiViewData> friendListView(Player player);

    CompletableFuture<ProxyActionResponsePayload> executeAction(Player player, ProxyActionRequestPayload actionRequest);
}
