package com.trickshotmlg.friendnet.adapter_spigot.GUIs;

import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
import com.trickshotmlg.friendnet.adapter_spigot.GUIs.Items.ActionItemStack;
import com.trickshotmlg.friendnet.adapter_spigot.Services.FriendGuiViewData;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.GUIUtils;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.MessageManager;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.ProxyActionResponseRenderer;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.SpigotCommandResultRenderer;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.SpigotUtils;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyActionRequestPayload;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyActionType;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyFriendEntry;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PublicFriendActionGUI extends AbstractGUI {

    private final UUID targetId;
    private FriendGuiViewData viewData;

    public PublicFriendActionGUI(JavaPlugin plugin, Player player, UUID targetId, FriendGuiViewData viewData) {
        super(plugin, player, 9 * 5, "titles.publicFriendActionGUI");
        this.targetId = targetId;
        this.viewData = viewData;
    }

    @Override
    protected String getInventoryTitle() {
        return locale("titles.publicFriendActionGUI", Map.of("target", getTargetDisplayName()));
    }

    @Override
    protected void buildInventory() {
        interactableSlots.clear();
        inventory.clear();

        inventory.setItem(13, SpigotUtils.createPlayerHead((FriendNetPlugin) plugin, viewData, targetId, getTargetDisplayName(), List.of()));

        if (isRequestSent()) {
            setInteractableItem(29, new ActionItemStack(
                    GUIUtils.CreateLocalizedHead(
                            player,
                            GUIUtils.RED_X_TEXTURE,
                            "gui",
                            "sentRequestDetailGUI.buttons.cancel.displayName",
                            "sentRequestDetailGUI.buttons.cancel.lore"
                    ),
                    player,
                    () -> executeAction(ProxyActionType.CANCEL_REQUEST),
                    ActionItemStack.SoundProfile.NAVIGATION
            ));
        } else {
            setInteractableItem(29, new ActionItemStack(
                    SpigotUtils.createItem(
                            Material.WRITABLE_BOOK,
                            player,
                            "gui",
                            "publicFriendActionGUI.buttons.add.displayName",
                            "publicFriendActionGUI.buttons.add.lore"
                    ),
                    player,
                    () -> executeAction(ProxyActionType.ADD_FRIEND),
                    ActionItemStack.SoundProfile.NAVIGATION
            ));
        }

        setInteractableItem(33, new ActionItemStack(
                GUIUtils.CreateBackItem(player),
                player,
                this::goBack,
                ActionItemStack.SoundProfile.NAVIGATION
        ));

        fillEmptySlots();
    }

    @Override
    protected void updateViewData(FriendGuiViewData viewData) {
        this.viewData = viewData;
    }

    private boolean isRequestSent() {
        if (viewData != null) {
            ProxyFriendEntry proxyEntry = viewData.proxyEntry(targetId);
            if (proxyEntry != null) {
                return proxyEntry.requestSentByViewer();
            }

            return viewData.sentRequests().stream()
                    .anyMatch(request -> targetId.equals(request.getOtherPlayerId(player.getUniqueId())));
        }

        return ((FriendNetPlugin) plugin).getFriendService().requestPending(player.getUniqueId(), targetId);
    }

    private void executeAction(ProxyActionType actionType) {
        FriendNetPlugin friendNetPlugin = (FriendNetPlugin) plugin;
        if (friendNetPlugin.isProxyBackendMode()) {
            ProxyActionRequestPayload request = new ProxyActionRequestPayload(
                    actionType,
                    targetId,
                    getTargetDisplayName(),
                    true
            );
            friendNetPlugin.getFriendGuiService().executeAction(player, request).whenComplete((response, throwable) ->
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        if (throwable != null) {
                            MessageManager.send(player, "commandFeedback.proxyBackendGuiUnavailable");
                            return;
                        }

                        ProxyActionResponseRenderer.render(player, response);
                        if (response.success()) {
                            goBack();
                        } else {
                            buildInventory();
                        }
                    })
            );
            return;
        }

        var useCases = friendNetPlugin.getApplicationServices().friendCommandUseCases();
        if (actionType == ProxyActionType.ADD_FRIEND) {
            SpigotCommandResultRenderer.render(player, useCases.sendFriendRequest(
                    player.getUniqueId(),
                    player.getName(),
                    new com.trickshotmlg.friendnet.core.application.KnownPlayerLookup.KnownPlayer(
                            targetId,
                            getTargetDisplayName(),
                            SpigotUtils.getPlayerData(friendNetPlugin, targetId),
                            Bukkit.getPlayer(targetId) != null
                    )
            ));
        } else {
            SpigotCommandResultRenderer.render(player, useCases.cancelRequest(player.getUniqueId(), targetId, getTargetDisplayName()));
        }
        goBack();
    }

    private String getTargetDisplayName() {
        if (viewData != null) {
            ProxyFriendEntry proxyEntry = viewData.proxyEntry(targetId);
            if (proxyEntry != null && !proxyEntry.displayName().isBlank()) {
                return proxyEntry.displayName();
            }
        }

        String displayName = SpigotUtils.getPlayerDisplayName((FriendNetPlugin) plugin, targetId);
        return displayName.isBlank() ? targetId.toString() : displayName;
    }

    private String locale(String key, Map<String, Object> placeholders) {
        return FriendNetPlugin.LocaleManager.getMessage(player.getUniqueId(), "gui", key, placeholders);
    }

    private void fillEmptySlots() {
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, SpigotUtils.createFillerGlass());
            }
        }
    }
}
