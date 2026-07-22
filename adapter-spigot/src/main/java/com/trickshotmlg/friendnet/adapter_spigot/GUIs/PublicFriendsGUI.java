package com.trickshotmlg.friendnet.adapter_spigot.GUIs;

import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
import com.trickshotmlg.friendnet.adapter_spigot.GUIs.Items.ActionItemStack;
import com.trickshotmlg.friendnet.adapter_spigot.Services.FriendGuiViewData;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.GUIUtils;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.MessageManager;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.SpigotUtils;
import com.trickshotmlg.friendnet.core_api.models.FriendEntry;
import com.trickshotmlg.friendnet.core_api.models.PlayerData;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyFriendEntry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PublicFriendsGUI extends AbstractGUI {

    private static final String DEFAULT_TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm";

    private final UUID viewedPlayerId;
    private final int friendRows = 4;
    private final int friendsPerPage = friendRows * 9;
    private int currentPage = 0;
    private FriendGuiViewData viewData;

    public PublicFriendsGUI(JavaPlugin plugin, Player player, UUID viewedPlayerId, FriendGuiViewData viewData) {
        super(plugin, player, 9 * 6, "titles.publicFriendsGUI");
        this.viewedPlayerId = viewedPlayerId;
        this.viewData = viewData;
    }

    @Override
    protected String getInventoryTitle() {
        return locale("titles.publicFriendsGUI", Map.of("target", getViewedDisplayName()));
    }

    @Override
    protected void buildInventory() {
        interactableSlots.clear();
        inventory.clear();

        List<FriendEntry> friends = viewData.friends().stream()
                .sorted(Comparator
                        .comparing((FriendEntry friend) -> !isOnline(friend.friendId()))
                        .thenComparing(friend -> getDisplayName(friend.friendId()).toLowerCase()))
                .toList();
        clampCurrentPage(friends.size());

        int startIndex = currentPage * friendsPerPage;
        int endIndex = Math.min(startIndex + friendsPerPage, friends.size());
        List<FriendEntry> visibleFriends = SpigotUtils.safeSubList(friends, startIndex, endIndex);

        for (int i = 0; i < visibleFriends.size(); i++) {
            FriendEntry friend = visibleFriends.get(i);
            setInteractableItem(i, new ActionItemStack(
                    createFriendItem(friend),
                    player,
                    () -> openPlayerAction(friend.friendId()),
                    ActionItemStack.SoundProfile.NAVIGATION
            ));
        }

        if (friends.isEmpty()) {
            inventory.setItem(22, GUIUtils.CreateEmptyStateItem(
                    player,
                    viewData.viewedFriendListPublic()
                            ? "publicFriendsGUI.emptyListMessage"
                            : "publicFriendsGUI.emptyPrivateListMessage"
            ));
        }

        int bottomRowStart = inventory.getSize() - 9;
        if (currentPage > 0) {
            setInteractableItem(bottomRowStart, new ActionItemStack(
                    GUIUtils.CreatePreviousPageItem(player),
                    player,
                    () -> {
                        currentPage--;
                        buildInventory();
                    },
                    ActionItemStack.SoundProfile.NAVIGATION
            ));
        }

        if (endIndex < friends.size()) {
            setInteractableItem(bottomRowStart + 8, new ActionItemStack(
                    GUIUtils.CreateNextPageItem(player),
                    player,
                    () -> {
                        currentPage++;
                        buildInventory();
                    },
                    ActionItemStack.SoundProfile.NAVIGATION
            ));
        }

        inventory.setItem(bottomRowStart + 4 - 9, SpigotUtils.createPlayerHead(
                (FriendNetPlugin) plugin,
                viewData,
                viewedPlayerId,
                getViewedDisplayName(),
                createViewedPlayerLore(friends.size())
        ));

        int maxPage = GUIUtils.CalculateMaxPage(friends.size(), friendsPerPage);
        inventory.setItem(bottomRowStart + 4, GUIUtils.CreatePageIndicatorItem(player, currentPage, maxPage));

        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, SpigotUtils.createFillerGlass());
            }
        }
    }

    @Override
    protected void updateViewData(FriendGuiViewData viewData) {
        this.viewData = viewData;
    }

    private void openPlayerAction(UUID targetId) {
        if (targetId.equals(player.getUniqueId())) {
            ((FriendNetPlugin) plugin).getFriendGuiService().friendListView(player).whenComplete((ownViewData, throwable) ->
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        if (throwable != null) {
                            MessageManager.send(player, "commandFeedback.proxyBackendGuiUnavailable");
                            return;
                        }
                        openChild(new FriendsGUI(plugin, player, ownViewData));
                    })
            );
            return;
        }

        if (isViewerFriend(targetId)) {
            ((FriendNetPlugin) plugin).getFriendGuiService().friendListView(player).whenComplete((ownViewData, throwable) ->
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        if (throwable != null) {
                            MessageManager.send(player, "commandFeedback.proxyBackendGuiUnavailable");
                            return;
                        }
                        openChild(new FriendDetailGUI(plugin, player, targetId, ownViewData));
                    })
            );
            return;
        }

        openChild(new PublicFriendActionGUI(plugin, player, targetId, viewData));
    }

    private ItemStack createFriendItem(FriendEntry friend) {
        UUID friendId = friend.friendId();
        return SpigotUtils.createPlayerHead((FriendNetPlugin) plugin, viewData, friendId, getDisplayName(friendId), createFriendLore(friend));
    }

    private List<String> createFriendLore(FriendEntry friend) {
        List<String> lore = new ArrayList<>();
        lore.add(locale("friendEntries.lore.status", Map.of("status", isOnline(friend.friendId())
                ? locale("friendEntries.status.online")
                : locale("friendEntries.status.offline"))));
        String serverName = currentServer(friend.friendId());
        if (serverName != null) {
            lore.add(locale("friendEntries.lore.server", Map.of("server", ChatColor.YELLOW + serverName)));
        }
        lore.add(locale("friendEntries.lore.favourite", Map.of("value", friend.favourite()
                ? locale("friendEntries.boolean.yes")
                : locale("friendEntries.boolean.no"))));
        lore.add("");
        lore.add(locale("friendEntries.lore.friendsSince", Map.of("date", ChatColor.YELLOW + formatTimestamp(friend.getFriendSince()))));
        lore.add(locale("friendEntries.lore.lastSeen", Map.of("date", ChatColor.YELLOW + formatTimestamp(lastSeen(friend.friendId())))));
        return lore;
    }

    private List<String> createViewedPlayerLore(int friendCount) {
        List<String> lore = new ArrayList<>();
        lore.add(locale("friendsGUI.playerSummary.statistics"));
        lore.add(locale("friendsGUI.playerSummary.totalFriends", Map.of("count", friendCount)));
        lore.add(locale("friendsGUI.playerSummary.firstSeen", Map.of("date", ChatColor.YELLOW + formatTimestamp(viewData.viewedFirstSeen()))));
        return lore;
    }

    private boolean isViewerFriend(UUID targetId) {
        ProxyFriendEntry proxyEntry = viewData.proxyEntry(targetId);
        if (proxyEntry != null) {
            return proxyEntry.friendOfViewer();
        }

        return ((FriendNetPlugin) plugin).getFriendService().areFriends(player.getUniqueId(), targetId);
    }

    private boolean isOnline(UUID targetId) {
        ProxyFriendEntry proxyEntry = viewData.proxyEntry(targetId);
        if (proxyEntry != null) {
            return proxyEntry.online();
        }

        return Bukkit.getPlayer(targetId) != null && ((FriendNetPlugin) plugin).getPlayerService().isOnline(targetId);
    }

    private String currentServer(UUID targetId) {
        ProxyFriendEntry proxyEntry = viewData.proxyEntry(targetId);
        if (proxyEntry == null || proxyEntry.currentServerName().isBlank()) {
            return null;
        }
        return proxyEntry.currentServerName();
    }

    private Timestamp lastSeen(UUID targetId) {
        ProxyFriendEntry proxyEntry = viewData.proxyEntry(targetId);
        if (proxyEntry != null && proxyEntry.lastSeenMillis() >= 0) {
            return new Timestamp(proxyEntry.lastSeenMillis());
        }

        if (((FriendNetPlugin) plugin).isProxyBackendMode()) {
            return null;
        }

        PlayerData playerData = SpigotUtils.getPlayerData((FriendNetPlugin) plugin, targetId);
        return playerData != null ? playerData.getLastSeen() : null;
    }

    private String getViewedDisplayName() {
        if (viewData.viewedDisplayName() != null && !viewData.viewedDisplayName().isBlank()) {
            return viewData.viewedDisplayName();
        }
        return getDisplayName(viewedPlayerId);
    }

    private String getDisplayName(UUID targetId) {
        ProxyFriendEntry proxyEntry = viewData.proxyEntry(targetId);
        if (proxyEntry != null && !proxyEntry.displayName().isBlank()) {
            return proxyEntry.displayName();
        }

        String displayName = SpigotUtils.getPlayerDisplayName((FriendNetPlugin) plugin, targetId);
        return displayName.isBlank() ? targetId.toString() : displayName;
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

    private void clampCurrentPage(int itemCount) {
        int maxPage = GUIUtils.CalculateMaxPage(itemCount, friendsPerPage) - 1;
        if (currentPage > maxPage) {
            currentPage = maxPage;
        }
        if (currentPage < 0) {
            currentPage = 0;
        }
    }
}
