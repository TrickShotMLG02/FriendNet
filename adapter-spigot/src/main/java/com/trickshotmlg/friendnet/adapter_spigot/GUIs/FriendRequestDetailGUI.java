package com.trickshotmlg.friendnet.adapter_spigot.GUIs;

import com.trickshotmlg.friendnet.adapter_spigot.Actions.BlocklistActions;
import com.trickshotmlg.friendnet.adapter_spigot.Actions.FriendRequestActions;
import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
import com.trickshotmlg.friendnet.adapter_spigot.GUIs.Items.ActionItemStack;
import com.trickshotmlg.friendnet.adapter_spigot.Services.FriendGuiViewData;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.GUIUtils;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.MessageManager;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.ProxyActionResponseRenderer;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.SpigotUtils;
import com.trickshotmlg.friendnet.core_api.enums.FriendshipStatus;
import com.trickshotmlg.friendnet.core_api.models.FriendshipData;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyActionRequestPayload;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyActionType;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyFriendEntry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class FriendRequestDetailGUI extends AbstractGUI {
    private static final String DEFAULT_TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm";

    private final UUID requesterId;
    private FriendGuiViewData viewData;

    public FriendRequestDetailGUI(JavaPlugin plugin, Player player, UUID requesterId) {
        this(plugin, player, requesterId, null);
    }

    public FriendRequestDetailGUI(JavaPlugin plugin, Player player, UUID requesterId, FriendGuiViewData viewData) {
        super(plugin, player, 9 * 5, "titles.friendRequestDetailGUI");
        this.requesterId = requesterId;
        this.viewData = viewData;
    }

    @Override
    protected String getInventoryTitle() {
        return locale("titles.friendRequestDetailGUI", Map.of("target", getRequesterDisplayName()));
    }

    @Override
    protected void buildInventory() {
        interactableSlots.clear();
        inventory.clear();

        Optional<FriendshipData> request = getPendingRequest();
        if (request.isEmpty()) {
            MessageManager.send(player, "friendRequest.accept.sender.notFound", Map.of("target", getRequesterDisplayName()));
            setInteractableItem(40, new ActionItemStack(
                    GUIUtils.CreateBackItem(player),
                    player,
                    this::goBack,
                    ActionItemStack.SoundProfile.NAVIGATION
            ));
            fillEmptySlots();
            return;
        }

        inventory.setItem(13, createRequesterItem(request.get()));

        setInteractableItem(28, new ActionItemStack(
                GUIUtils.CreateLocalizedHead(
                        player,
                        GUIUtils.CHECK_TEXTURE,
                        "gui",
                        "friendRequestDetailGUI.buttons.accept.displayName",
                        "friendRequestDetailGUI.buttons.accept.lore"
                ),
                player,
                this::acceptRequest
        ));

        setInteractableItem(30, new ActionItemStack(
                GUIUtils.CreateLocalizedHead(
                        player,
                        GUIUtils.RED_X_TEXTURE,
                        "gui",
                        "friendRequestDetailGUI.buttons.deny.displayName",
                        "friendRequestDetailGUI.buttons.deny.lore"
                ),
                player,
                () -> openConfirmation(
                        "titles.confirmationGUI",
                        "confirmations.denyRequest.displayName",
                        "confirmations.denyRequest.lore",
                        Map.of("target", getRequesterDisplayName()),
                        confirmed -> {
                            if (confirmed) {
                                denyRequest();
                            }
                        }
                )
        ));

        setInteractableItem(32, new ActionItemStack(
                SpigotUtils.createItem(
                        Material.BARRIER,
                        player,
                        "gui",
                        "friendRequestDetailGUI.buttons.block.displayName",
                        "friendRequestDetailGUI.buttons.block.lore"
                ),
                player,
                () -> openConfirmation(
                        "titles.confirmationGUI",
                        "confirmations.blockPlayer.displayName",
                        "confirmations.blockPlayer.lore",
                        Map.of("target", getRequesterDisplayName()),
                        confirmed -> {
                            if (confirmed) {
                                blockRequester();
                            }
                        }
                )
        ));

        setInteractableItem(34, new ActionItemStack(
                GUIUtils.CreateBackItem(player),
                player,
                this::goBack,
                ActionItemStack.SoundProfile.NAVIGATION
        ));

        fillEmptySlots();
    }

    private ItemStack createRequesterItem(FriendshipData request) {
        return SpigotUtils.createPlayerHead(requesterId, getRequesterDisplayName(), createRequesterLore(request));
    }

    private List<String> createRequesterLore(FriendshipData request) {
        List<String> lore = new ArrayList<>();
        lore.add(locale("friendRequestsGUI.requestEntry.lore.status", Map.of("status", ChatColor.YELLOW + locale("friendRequestsGUI.requestEntry.status.pending"))));
        lore.add(locale("friendRequestsGUI.requestEntry.lore.sentAt", Map.of("date", ChatColor.YELLOW + formatTimestamp(request.getRequestSentTime()))));
        return lore;
    }

    private void acceptRequest() {
        if (((FriendNetPlugin) plugin).isProxyBackendMode()) {
            executeProxyAction(ProxyActionType.ACCEPT_REQUEST);
            return;
        }

        new FriendRequestActions((FriendNetPlugin) plugin).acceptRequest(player, getRequester());
        goBack();
    }

    private void denyRequest() {
        if (((FriendNetPlugin) plugin).isProxyBackendMode()) {
            executeProxyAction(ProxyActionType.DENY_REQUEST);
            return;
        }

        new FriendRequestActions((FriendNetPlugin) plugin).denyRequest(player, getRequester());
        goBack();
    }

    private void blockRequester() {
        if (((FriendNetPlugin) plugin).isProxyBackendMode()) {
            executeProxyAction(ProxyActionType.BLOCK_PLAYER);
            return;
        }

        new BlocklistActions((FriendNetPlugin) plugin).block(player, requesterId);
        goBack();
    }

    private Optional<FriendshipData> getPendingRequest() {
        if (((FriendNetPlugin) plugin).isProxyBackendMode() && viewData != null) {
            return viewData.pendingRequests().stream()
                    .filter(request -> requesterId.equals(request.getRequesterId()))
                    .findFirst();
        }

        return ((FriendNetPlugin) plugin)
                .getFriendService()
                .getFriendshipData(player.getUniqueId(), requesterId)
                .filter(request -> request.getFriendshipStatus() == FriendshipStatus.Pending)
                .filter(request -> requesterId.equals(request.getRequesterId()));
    }

    private OfflinePlayer getRequester() {
        return Bukkit.getOfflinePlayer(requesterId);
    }

    private String getRequesterDisplayName() {
        if (viewData != null) {
            ProxyFriendEntry proxyEntry = viewData.proxyEntry(requesterId);
            if (proxyEntry != null && !proxyEntry.displayName().isBlank()) {
                return proxyEntry.displayName();
            }
        }

        String displayName = SpigotUtils.getPlayerDisplayName((FriendNetPlugin) plugin, requesterId);
        return displayName.isBlank() ? requesterId.toString() : displayName;
    }

    private void executeProxyAction(ProxyActionType actionType) {
        ProxyActionRequestPayload request = new ProxyActionRequestPayload(
                actionType,
                requesterId,
                getRequesterDisplayName(),
                true
        );
        FriendNetPlugin friendNetPlugin = (FriendNetPlugin) plugin;
        friendNetPlugin.getFriendGuiService().executeAction(player, request).whenComplete((response, throwable) ->
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (throwable != null) {
                        MessageManager.send(player, "commandFeedback.proxyBackendGuiUnavailable");
                        return;
                    }

                    ProxyActionResponseRenderer.render(player, response);
                    if (response.friendListView() != null) {
                        viewData = FriendGuiViewData.fromProxyPayload(player.getUniqueId(), response.friendListView());
                    }
                    if (response.success()) {
                        goBack();
                    } else {
                        buildInventory();
                    }
                })
        );
    }

    private String formatTimestamp(Timestamp timestamp) {
        if (timestamp == null) {
            return locale("friendEntries.lastSeen.unknown");
        }

        return getTimestampFormatter().format(timestamp.toInstant().atZone(ZoneId.systemDefault()));
    }

    private DateTimeFormatter getTimestampFormatter() {
        String pattern = locale("format.timestamp");
        if (pattern == null || pattern.isBlank() || pattern.equals("format.timestamp")) {
            pattern = DEFAULT_TIMESTAMP_FORMAT;
        }

        try {
            return DateTimeFormatter.ofPattern(pattern);
        } catch (IllegalArgumentException e) {
            return DateTimeFormatter.ofPattern(DEFAULT_TIMESTAMP_FORMAT);
        }
    }

    private String locale(String key) {
        return FriendNetPlugin.LocaleManager.getMessage(player.getUniqueId(), "gui", key);
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
