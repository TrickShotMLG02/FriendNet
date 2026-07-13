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

public class FavoriteFriendsGUI extends AbstractGUI {

    private static final String DEFAULT_TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm";

    private final int friendRows = 3;
    private final int friendsPerPage = friendRows * 9;
    private int currentPage = 0;

    public FavoriteFriendsGUI(JavaPlugin plugin, Player player) {
        super(plugin, player, 9 * 5, "titles.favoriteFriendsGUI");
    }

    @Override
    protected void buildInventory() {
        interactableSlots.clear();
        inventory.clear();

        List<FriendshipData> favoriteFriends = ((FriendNetPlugin) plugin)
                .getFriendService()
                .getFriendships(player.getUniqueId())
                .stream()
                .filter(FriendshipData::isFavourite)
                .toList();
        favoriteFriends = applyFilters(favoriteFriends, FriendFilterGUI.getFilterState(player));

        clampCurrentPage(favoriteFriends.size());
        int startIndex = currentPage * friendsPerPage;
        int endIndex = Math.min(startIndex + friendsPerPage, favoriteFriends.size());
        int favoriteFriendCount = favoriteFriends.size();
        List<FriendshipData> visibleFriends = SpigotUtils.safeSubList(favoriteFriends, startIndex, endIndex);

        for (int i = 0; i < visibleFriends.size(); i++) {
            FriendshipData friend = visibleFriends.get(i);
            UUID friendId = friend.getOtherPlayerId(player.getUniqueId());
            setInteractableItem(i, new ActionItemStack(
                    createFriendItem(friend),
                    player,
                    () -> openChild(new FriendDetailGUI(plugin, player, friendId)),
                    ActionItemStack.SoundProfile.NAVIGATION
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

        if (endIndex < favoriteFriends.size()) {
            setInteractableItem(bottomRowStart + 8, new ActionItemStack(
                    GUIUtils.CreateNextPageItem(player),
                    player,
                    () -> {
                        int maxPage = GUIUtils.CalculateMaxPage(favoriteFriendCount, friendsPerPage) - 1;
                        if (currentPage < maxPage) {
                            currentPage++;
                            buildInventory();
                        }
                    },
                    ActionItemStack.SoundProfile.NAVIGATION
            ));
        }

        setInteractableItem(bottomRowStart + 3, new ActionItemStack(
                SpigotUtils.createItem(
                        Material.HOPPER,
                        player,
                        "gui",
                        "friendsGUI.buttons.filter.displayName",
                        "friendsGUI.buttons.filter.lore"
                ),
                player,
                () -> openChild(new FriendFilterGUI(plugin, player, false)),
                ActionItemStack.SoundProfile.NAVIGATION
        ));

        int maxPage = GUIUtils.CalculateMaxPage(favoriteFriendCount, friendsPerPage);
        inventory.setItem(bottomRowStart + 4, GUIUtils.CreatePageIndicatorItem(player, currentPage, maxPage));

        setInteractableItem(bottomRowStart + 5, new ActionItemStack(
                GUIUtils.CreateBackItem(player),
                player,
                () -> goBack(),
                ActionItemStack.SoundProfile.NAVIGATION
        ));

        fillEmptySlots();
    }

    private ItemStack createFriendItem(FriendshipData friend) {
        UUID friendId = friend.getOtherPlayerId(player.getUniqueId());
        String friendName = SpigotUtils.getPlayerDisplayName((FriendNetPlugin) plugin, friendId);
        PlayerData playerData = ((FriendNetPlugin) plugin).getPlayerService().getPlayerData(friendId);

        return SpigotUtils.createPlayerHead(friendId, friendName, createFriendLore(friend, friendId, playerData));
    }

    private List<String> createFriendLore(FriendshipData friend, UUID friendId, PlayerData playerData) {
        List<String> lore = new ArrayList<>();

        lore.add(locale("friendEntries.lore.status", Map.of("status", formatOnlineStatus(friend))));
        lore.add("");
        lore.add(locale("friendEntries.lore.friendsSince", Map.of("date", ChatColor.YELLOW + formatTimestamp(friend.getFriendSince()))));
        lore.add(locale("friendEntries.lore.lastSeen", Map.of("date", formatLastSeen(friend, playerData))));

        return lore;
    }

    private String formatOnlineStatus(FriendshipData friend) {
        return isFriendOnline(friend) ? locale("friendEntries.status.online") : locale("friendEntries.status.offline");
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

    private List<FriendshipData> applyFilters(List<FriendshipData> favoriteFriends, FriendFilterState filterState) {
        Comparator<FriendshipData> comparator = createFriendComparator();

        if (filterState.isSortByRecentlySeen()) {
            comparator = Comparator.comparing(friend -> getLastSeen(friend), Comparator.nullsLast(Comparator.reverseOrder()));
        }

        if (filterState.isReverseSort()) {
            comparator = comparator.reversed();
        }

        Comparator<FriendshipData> finalComparator = comparator;
        return favoriteFriends.stream()
                .filter(friend -> !filterState.isOnlineOnly() || isFriendOnline(friend))
                .filter(friend -> matchesNameSearch(friend, filterState.getNameSearchQuery()))
                .sorted(finalComparator)
                .toList();
    }

    private Comparator<FriendshipData> createFriendComparator() {
        return Comparator
                .comparing((FriendshipData friend) -> !isFriendOnline(friend))
                .thenComparing(friend -> getFriendDisplayName(friend).toLowerCase())
                .thenComparing(FriendshipData::getFriendSince, Comparator.nullsLast(Comparator.reverseOrder()));
    }

    private Timestamp getLastSeen(FriendshipData friend) {
        PlayerData playerData = ((FriendNetPlugin) plugin).getPlayerService().getPlayerData(friend.getOtherPlayerId(player.getUniqueId()));
        return playerData != null ? playerData.getLastSeen() : null;
    }

    private String getFriendDisplayName(FriendshipData friend) {
        return SpigotUtils.getPlayerDisplayName((FriendNetPlugin) plugin, friend.getOtherPlayerId(player.getUniqueId()));
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
            currentPage = Math.max(0, maxPage);
        }
    }

    private void fillEmptySlots() {
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, SpigotUtils.createFillerGlass());
            }
        }
    }
}
