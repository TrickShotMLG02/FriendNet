package com.trickshotmlg.friendnet.adapter_spigot.GUIs;

import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
import com.trickshotmlg.friendnet.adapter_spigot.GUIs.Items.ActionItemStack;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.GUIUtils;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.SpigotUtils;
import com.trickshotmlg.friendnet.core_api.models.FriendshipData;
import com.trickshotmlg.friendnet.core_api.models.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
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

public class FriendsGUI extends AbstractGUI {

    private static final String DEFAULT_TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm";

    private final int friendRows = 4;

    private int currentPage = 0;
    private final int friendsPerPage = friendRows * 9;

    public FriendsGUI(JavaPlugin plugin, Player player, List<FriendshipData> friends, List<FriendshipData> requests) {
        //super(plugin, player, ((friends.size() - 1) / 9 + 1) * 9, "Your Friends");
        super(plugin, player, 9 * 6, "titles.friendsGUI");
    }

    @Override
    protected void buildInventory() {
        // Clear previous contents
        interactableSlots.clear();
        inventory.clear();

        FriendNetPlugin friendNetPlugin = (FriendNetPlugin) plugin;
        FriendFilterState filterState = FriendFilterGUI.getFilterState(player);
        List<FriendshipData> friends = applyFilters(
                friendNetPlugin.getFriendService().getFriendships(player.getUniqueId()).stream().toList(),
                filterState
        );
        List<FriendshipData> requests = ((FriendNetPlugin) plugin).getFriendService().getPendingRequests(player.getUniqueId()).stream().toList();


        clampCurrentPage(friends.size());
        int startIndex = currentPage * friendsPerPage;
        int endIndex = Math.min(startIndex + friendsPerPage, friends.size());

        List<FriendshipData> visibleFriends = SpigotUtils.safeSubList(friends, startIndex, endIndex);

        // Populate friends for this page
        for (int i = 0; i < visibleFriends.size(); i++) {
            FriendshipData friend = visibleFriends.get(i);
            ItemStack friendItem = createFriendItem(friend);
            UUID friendId = friend.getOtherPlayerId(player.getUniqueId());
            setInteractableItem(i, new ActionItemStack(
                    friendItem,
                    player,
                    () -> openChild(new FriendDetailGUI(plugin, player, friendId)),
                    ActionItemStack.SoundProfile.NAVIGATION
            ));
        }

        if (friends.isEmpty()) {
            inventory.setItem(22, GUIUtils.CreateEmptyStateItem(player, "friendsGUI.emptyListMessage"));
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
        if (endIndex < friends.size()) {
            setInteractableItem(bottomRowStart + 8,
                    new ActionItemStack(
                            GUIUtils.CreateNextPageItem(player),
                            player,
                            () -> {
                                int maxPage = (int) Math.ceil((double) friends.size() / friendsPerPage) - 1;
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

        // Block List Item
        {
            ActionItemStack actionItemStack = new ActionItemStack(
                    SpigotUtils.createItem(
                            Material.BARRIER,
                            player,
                            "gui",
                            "friendsGUI.buttons.blocklist.displayName",
                            "friendsGUI.buttons.blocklist.lore"
                    ),
                    player,
                    () -> this.openChild(new BlocklistGUI(plugin, player)),
                    ActionItemStack.SoundProfile.NAVIGATION
            );

            setInteractableItem(bottomRowStart + 3 - 9, actionItemStack);
        }

        // Player Head
        {
            inventory.setItem(
                    bottomRowStart + 4 - 9,
                    SpigotUtils.createPlayerHead(
                            player.getUniqueId(),
                            player.getDisplayName(),
                            List.of(
                                    locale("friendsGUI.playerSummary.statistics"),
                                    locale("friendsGUI.playerSummary.totalFriends", Map.of("count", friends.size())),
                                    locale("friendsGUI.playerSummary.totalRequests", Map.of("count", requests.size()))
                            )
                    )
            );
        }

        // Pending Requests Item
        {
            ActionItemStack actionItemStack = new ActionItemStack(
                    // TODO: Use Book if no new requests, use book and quill on pending requests
                    SpigotUtils.createItem(
                            requests.size() > 0 ? Material.WRITABLE_BOOK : Material.BOOK,
                            player,
                            "gui",
                            "friendsGUI.buttons.requests.displayName",
                            "friendsGUI.buttons.requests.lore"
                    ),
                    player,
                    () -> {
                        this.openChild(new RequestsGUI(plugin, player));
                    },
                    ActionItemStack.SoundProfile.NAVIGATION
            );

            setInteractableItem(bottomRowStart + 5 - 9, actionItemStack);
        }

        // Personal Settings Item
        {
            ActionItemStack actionItemStack = new ActionItemStack(
                    SpigotUtils.createItem(
                            Material.COMPARATOR,
                            player,
                            "gui",
                            "friendsGUI.buttons.personalSettings.displayName",
                            "friendsGUI.buttons.personalSettings.lore"
                    ),
                    player,
                    () -> this.openChild(new PersonalSettingsGUI(plugin, player)),
                    ActionItemStack.SoundProfile.NAVIGATION
            );

            setInteractableItem(bottomRowStart + 3, actionItemStack);
        }

        // Favorite Friends Item
        {
            ActionItemStack actionItemStack = new ActionItemStack(
                    SpigotUtils.createItem(
                            Material.NETHER_STAR,
                            player,
                            "gui",
                            "friendsGUI.buttons.favorites.displayName",
                            "friendsGUI.buttons.favorites.lore"
                    ),
                    player,
                    () -> this.openChild(new FavoriteFriendsGUI(plugin, player)),
                    ActionItemStack.SoundProfile.NAVIGATION
            );

            setInteractableItem(bottomRowStart + 6, actionItemStack);
        }

        // Page Display Item
        {
            int maxPage = GUIUtils.CalculateMaxPage(friends.size(), friendsPerPage);
            inventory.setItem(bottomRowStart + 4, GUIUtils.CreatePageIndicatorItem(player, currentPage, maxPage));
        }

        // Filter Item
        {
            ActionItemStack actionItemStack = new ActionItemStack(
                    SpigotUtils.createItem(
                            Material.HOPPER,
                            player,
                            "gui",
                            "friendsGUI.buttons.filter.displayName",
                            "friendsGUI.buttons.filter.lore"
                    ),
                    player,
                    () -> this.openChild(new FriendFilterGUI(plugin, player)),
                    ActionItemStack.SoundProfile.NAVIGATION
            );

            setInteractableItem(bottomRowStart + 5, actionItemStack);
        }

        // Filler for aesthetics
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, SpigotUtils.createFillerGlass());
            }
        }
    }

    /**
     * @param player  The player that clicked
     * @param slot    The slot number that was clicked
     * @param clicked The ItemStack that was clicked
     */
    @Override
    public void handleClick(Player player, int slot, ItemStack clicked) {

    }

    @Deprecated
    private void refreshPage() {
        buildInventory();
        player.updateInventory();
    }

    private ItemStack createFriendItem(FriendshipData friend) {

        UUID friendID = friend.getOtherPlayerId(this.player.getUniqueId());
        String friendName = SpigotUtils.getPlayerDisplayName((FriendNetPlugin) plugin, friendID);
        PlayerData playerData = SpigotUtils.getPlayerData((FriendNetPlugin) plugin, friendID);

        return SpigotUtils.createPlayerHead(friendID, friendName, createFriendLore(friend, friendID, playerData));
    }

    private List<String> createFriendLore(FriendshipData friend, UUID friendId, PlayerData playerData) {
        List<String> lore = new ArrayList<>();

        lore.add(locale("friendEntries.lore.status", Map.of("status", formatOnlineStatus(friend, friendId))));
        lore.add(locale("friendEntries.lore.favourite", Map.of("value", formatBoolean(friend.isFavourite()))));
        lore.add("");
        lore.add(locale("friendEntries.lore.friendsSince", Map.of("date", ChatColor.YELLOW + formatTimestamp(friend.getFriendSince()))));
        lore.add(locale("friendEntries.lore.lastSeen", Map.of("date", formatLastSeen(friend, playerData))));

        return lore;
    }

    private String formatOnlineStatus(FriendshipData friend, UUID friendId) {
        if (isFriendOnline(friend)) {
            return locale("friendEntries.status.online");
        }

        PlayerData playerData = ((FriendNetPlugin) plugin).getPlayerService().getPlayerData(friendId);
        if (playerData == null || playerData.getLastSeen() == null) {
            return locale("friendEntries.status.offline");
        }

        return locale("friendEntries.status.offline");
    }

    private String formatBoolean(boolean value) {
        return value ? locale("friendEntries.boolean.yes") : locale("friendEntries.boolean.no");
    }

    private String formatLastSeen(FriendshipData friend, PlayerData playerData) {
        if (isFriendOnline(friend)) {
            return locale("friendEntries.lastSeen.now");
        }

        return ChatColor.YELLOW + formatTimestamp(playerData != null ? playerData.getLastSeen() : null);
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

    private List<FriendshipData> applyFilters(List<FriendshipData> friends, FriendFilterState filterState) {
        FriendNetPlugin friendNetPlugin = (FriendNetPlugin) plugin;
        Comparator<FriendshipData> comparator = Comparator
                .comparing((FriendshipData friend) -> !isFriendOnline(friend))
                .thenComparing(friend -> getFriendDisplayName(friend).toLowerCase())
                .thenComparing(FriendshipData::getFriendSince, Comparator.nullsLast(Comparator.reverseOrder()));

        if (filterState.isSortByRecentlySeen()) {
            comparator = Comparator.comparing(friend -> getLastSeen(friend), Comparator.nullsLast(Comparator.reverseOrder()));
        }

        if (filterState.isReverseSort()) {
            comparator = comparator.reversed();
        }

        Comparator<FriendshipData> finalComparator = comparator;
        List<FriendshipData> filteredFriends = friends.stream()
                .filter(friend -> !filterState.isFavoritesOnly() || friend.isFavourite())
                .filter(friend -> !filterState.isOnlineOnly() || isFriendOnline(friend))
                .filter(friend -> matchesNameSearch(friend, filterState.getNameSearchQuery()))
                .sorted(finalComparator)
                .toList();

        return filteredFriends;
    }

    private String getFriendDisplayName(FriendshipData friend) {
        return SpigotUtils.getPlayerDisplayName((FriendNetPlugin) plugin, friend.getOtherPlayerId(player.getUniqueId()));
    }

    private Timestamp getLastSeen(FriendshipData friend) {
        PlayerData playerData = SpigotUtils.getPlayerData((FriendNetPlugin) plugin, friend.getOtherPlayerId(player.getUniqueId()));
        return playerData != null ? playerData.getLastSeen() : null;
    }

    private boolean isFriendOnline(FriendshipData friend) {
        UUID friendId = friend.getOtherPlayerId(player.getUniqueId());
        return Bukkit.getPlayer(friendId) != null && ((FriendNetPlugin) plugin).getPlayerService().isOnline(friendId);
    }

    private boolean matchesNameSearch(FriendshipData friend, String query) {
        if (query == null || query.isBlank()) {
            return true;
        }

        String normalizedName = normalizeSearchText(getFriendDisplayName(friend));
        String normalizedQuery = normalizeSearchText(query);

        if (normalizedQuery.isBlank()) {
            return true;
        }

        if (normalizedName.contains(normalizedQuery)) {
            return true;
        }

        return fuzzyContains(normalizedName, normalizedQuery);
    }

    private boolean fuzzyContains(String name, String query) {
        int threshold = Math.max(1, query.length() / 3);

        if (levenshteinDistance(name, query, threshold) <= threshold) {
            return true;
        }

        int windowSize = query.length();
        for (int i = 0; i <= name.length() - windowSize; i++) {
            String window = name.substring(i, i + windowSize);
            if (levenshteinDistance(window, query, threshold) <= threshold) {
                return true;
            }
        }

        return false;
    }

    private int levenshteinDistance(String left, String right, int maxDistance) {
        if (Math.abs(left.length() - right.length()) > maxDistance) {
            return maxDistance + 1;
        }

        int[] previous = new int[right.length() + 1];
        int[] current = new int[right.length() + 1];

        for (int j = 0; j <= right.length(); j++) {
            previous[j] = j;
        }

        for (int i = 1; i <= left.length(); i++) {
            current[0] = i;
            int rowMin = current[0];

            for (int j = 1; j <= right.length(); j++) {
                int cost = left.charAt(i - 1) == right.charAt(j - 1) ? 0 : 1;
                current[j] = Math.min(
                        Math.min(current[j - 1] + 1, previous[j] + 1),
                        previous[j - 1] + cost
                );
                rowMin = Math.min(rowMin, current[j]);
            }

            if (rowMin > maxDistance) {
                return maxDistance + 1;
            }

            int[] swap = previous;
            previous = current;
            current = swap;
        }

        return previous[right.length()];
    }

    private String normalizeSearchText(String value) {
        return value == null ? "" : value.toLowerCase().replaceAll("[^a-z0-9]", "");
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
