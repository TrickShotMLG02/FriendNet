package com.trickshotmlg.friendnet.adapter_spigot.GUIs;

import com.trickshotmlg.friendnet.adapter_spigot.Actions.FriendRequestActions;
import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
import com.trickshotmlg.friendnet.adapter_spigot.GUIs.Items.ActionItemStack;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.GUIUtils;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.SpigotUtils;
import com.trickshotmlg.friendnet.core_api.models.FriendshipData;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RequestsGUI extends AbstractGUI {
    private static final String DEFAULT_TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm";
    private final int friendRows = 4;

    private int currentPage = 0;
    private final int requestsPerPage = friendRows * 9;

    public RequestsGUI(JavaPlugin plugin, Player player) {
        super(
                plugin,
                player,
                9 * 6,
                "titles.friendRequestsGUI"
        );
    }

    @Override
    protected void buildInventory() {
        // Clear previous contents
        interactableSlots.clear();
        inventory.clear();

        List<FriendshipData> requests = ((FriendNetPlugin) plugin).getFriendService().getPendingRequests(player.getUniqueId()).stream().toList();
        clampCurrentPage(requests.size());

        int startIndex = currentPage * requestsPerPage;
        int endIndex = Math.min(startIndex + requestsPerPage, requests.size());


        List<FriendshipData> visibleRequests = SpigotUtils.safeSubList(requests, startIndex, endIndex);

        // Populate friends for this page
        for (int i = 0; i < visibleRequests.size(); i++) {
            FriendshipData request = visibleRequests.get(i);
            UUID requesterId = request.getRequesterId();
            setInteractableItem(i, new ActionItemStack(
                    createFriendItem(request),
                    player,
                    () -> openChild(new FriendRequestDetailGUI(plugin, player, requesterId)),
                    ActionItemStack.SoundProfile.NAVIGATION
            ));
        }

        // Navigation buttons
        int bottomRowStart = inventory.getSize() - 9;

        // Previous page
        if (currentPage > 0) {
            setInteractableItem(bottomRowStart,
                    new ActionItemStack(
                            GUIUtils.CreatePreviousPageItem(player),
                            player,
                            () -> {
                                if (currentPage > 0) {
                                    currentPage--;
                                    buildInventory();
                                }
                            },
                            ActionItemStack.SoundProfile.NAVIGATION
                    )
            );
            //String texture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmVkOWQ1YzJiNDgwNzA1OGQ5ODdjNmUxZDYzMDBhMWNjNGI5ZWVlN2IxNmYxZjBhY2FjMTRmZmNkMWE5Njk5ZiJ9fX0=";
            //inventory.setItem(bottomRowStart, SpigotUtils.getSkull(texture, "§ePrevious Page", 1));
        }

        // Next page
        if (endIndex < requests.size()) {

            setInteractableItem(bottomRowStart + 8,
                    new ActionItemStack(
                            GUIUtils.CreateNextPageItem(player),
                            player,
                            () -> {
                                int maxPage = (int) Math.ceil((double) requests.size() / requestsPerPage) - 1;
                                if (currentPage < maxPage) {
                                    currentPage++;
                                    buildInventory();
                                }
                            },
                            ActionItemStack.SoundProfile.NAVIGATION
                    )
            );
            //String texture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTg3YmFhNDc2NzIzNGMwMWMwNGI4YmJlYjUxOGEwNTNkY2U3MzlmNGEwNDM1OGE0MjQzMDJmYjRhMDE3MmY4In19fQ==";
            //inventory.setItem(bottomRowStart + 8, SpigotUtils.getSkull(texture, "§ePrevious Page", 1));
        }

        // Page Display Item
        {
            int maxPage = GUIUtils.CalculateMaxPage(requests.size(), requestsPerPage);
            inventory.setItem(bottomRowStart + 4, GUIUtils.CreatePageIndicatorItem(player, currentPage, maxPage));
        }

        // Deny All Item
        {
            ItemStack denyAllItem = GUIUtils.CreateLocalizedHead(
                    player,
                    GUIUtils.RED_X_TEXTURE,
                    "gui",
                    "friendRequestsGUI.buttons.denyAllRequests.displayName",
                    "friendRequestsGUI.buttons.denyAllRequests.lore"
            );
            setInteractableItem(bottomRowStart + 3,
                    new ActionItemStack(
                            denyAllItem,
                            player,
                            () -> {
                                new FriendRequestActions(((FriendNetPlugin) plugin).getFriendService())
                                        .denyAllRequests(player);

                                buildInventory();
                            }
                    )
            );
        }

        // Page Display Item
        //inventory.setItem(bottomRowStart + 4, SpigotUtils.createItem(Material.PAPER, "§7Page " + (currentPage + 1)));

        // Sent Requests Item
        {
            ItemStack sentRequestsItem = GUIUtils.CreateLocalizedHead(
                    player,
                    GUIUtils.BOOKS_TEXTURE,
                    "gui",
                    "friendRequestsGUI.buttons.sentRequests.displayName",
                    "friendRequestsGUI.buttons.sentRequests.lore"
            );
            setInteractableItem(bottomRowStart + 1,
                    new ActionItemStack(
                            sentRequestsItem,
                            player,
                            () -> openChild(new SentRequestsGUI(plugin, player)),
                            ActionItemStack.SoundProfile.NAVIGATION
                    )
            );
        }

        // Back Item
        {
            setInteractableItem(bottomRowStart + 7,
                    new ActionItemStack(
                            GUIUtils.CreateBackItem(player),
                            player,
                            () -> goBack(),
                            ActionItemStack.SoundProfile.NAVIGATION
                    )
            );
        }

        // Accept All Item
        {
            ItemStack acceptAllItem = GUIUtils.CreateLocalizedHead(
                    player,
                    GUIUtils.CHECK_TEXTURE,
                    "gui",
                    "friendRequestsGUI.buttons.acceptAllRequests.displayName",
                    "friendRequestsGUI.buttons.acceptAllRequests.lore"
            );
            setInteractableItem(bottomRowStart + 5,
                    new ActionItemStack(
                            acceptAllItem,
                            player,
                            () -> {
                                new FriendRequestActions(((FriendNetPlugin) plugin).getFriendService())
                                        .acceptAllRequests(player);

                                buildInventory();
                            }
                    )
            );
        }

        // Filler for aesthetics
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, SpigotUtils.createFillerGlass());
            }
        }
    }

    @Deprecated
    private void refreshPage() {
        buildInventory();
        player.updateInventory();
    }

    private ItemStack createFriendItem(FriendshipData friend) {

        UUID friendID = friend.getOtherPlayerId(this.player.getUniqueId());
        String friendName = SpigotUtils.getPlayerDisplayName((FriendNetPlugin) plugin, friendID);
        if (friendName.isBlank()) {
            friendName = friendID.toString();
        }

        return SpigotUtils.createPlayerHead(friendID, friendName, createRequestLore(friend));
    }

    private List<String> createRequestLore(FriendshipData request) {
        List<String> lore = new ArrayList<>();
        lore.add(locale("friendRequestsGUI.requestEntry.lore.status", Map.of("status", ChatColor.YELLOW + locale("friendRequestsGUI.requestEntry.status.pending"))));
        lore.add(locale("friendRequestsGUI.requestEntry.lore.sentAt", Map.of("date", ChatColor.YELLOW + formatTimestamp(request.getRequestSentTime()))));
        lore.add("");
        lore.add(locale("friendRequestsGUI.requestEntry.lore.clickToOpen"));
        return lore;
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

    private void clampCurrentPage(int itemCount) {
        int maxPage = GUIUtils.CalculateMaxPage(itemCount, requestsPerPage) - 1;
        if (currentPage > maxPage) {
            currentPage = Math.max(0, maxPage);
        }
    }

    private String locale(String key) {
        return FriendNetPlugin.LocaleManager.getMessage(player.getUniqueId(), "gui", key);
    }

    private String locale(String key, Map<String, Object> placeholders) {
        return FriendNetPlugin.LocaleManager.getMessage(player.getUniqueId(), "gui", key, placeholders);
    }
}
