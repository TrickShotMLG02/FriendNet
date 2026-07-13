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

public class SentRequestsGUI extends AbstractGUI {
    private static final String DEFAULT_TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm";

    private final int requestRows = 4;
    private final int requestsPerPage = requestRows * 9;
    private int currentPage = 0;

    public SentRequestsGUI(JavaPlugin plugin, Player player) {
        super(plugin, player, 9 * 6, "titles.sentRequestsGUI");
    }

    @Override
    protected void buildInventory() {
        interactableSlots.clear();
        inventory.clear();

        List<FriendshipData> requests = ((FriendNetPlugin) plugin)
                .getFriendService()
                .getSentRequests(player.getUniqueId())
                .stream()
                .toList();
        clampCurrentPage(requests.size());

        int startIndex = currentPage * requestsPerPage;
        int endIndex = Math.min(startIndex + requestsPerPage, requests.size());
        List<FriendshipData> visibleRequests = SpigotUtils.safeSubList(requests, startIndex, endIndex);

        for (int i = 0; i < visibleRequests.size(); i++) {
            FriendshipData request = visibleRequests.get(i);
            UUID targetId = request.getOtherPlayerId(player.getUniqueId());
            setInteractableItem(i, new ActionItemStack(
                    createRequestItem(request, targetId),
                    player,
                    () -> openChild(new SentRequestDetailGUI(plugin, player, targetId)),
                    ActionItemStack.SoundProfile.NAVIGATION
            ));
        }

        if (requests.isEmpty()) {
            inventory.setItem(22, GUIUtils.CreateEmptyStateItem(player, "sentRequestsGUI.emptyListMessage"));
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

        if (endIndex < requests.size()) {
            setInteractableItem(bottomRowStart + 8, new ActionItemStack(
                    GUIUtils.CreateNextPageItem(player),
                    player,
                    () -> {
                        int maxPage = GUIUtils.CalculateMaxPage(requests.size(), requestsPerPage) - 1;
                        if (currentPage < maxPage) {
                            currentPage++;
                            buildInventory();
                        }
                    },
                    ActionItemStack.SoundProfile.NAVIGATION
            ));
        }

        setInteractableItem(bottomRowStart + 3, new ActionItemStack(
                GUIUtils.CreateLocalizedHead(
                        player,
                        GUIUtils.RED_X_TEXTURE,
                        "gui",
                        "sentRequestsGUI.buttons.cancelAll.displayName",
                        "sentRequestsGUI.buttons.cancelAll.lore"
                ),
                player,
                () -> openConfirmation(
                        "titles.confirmationGUI",
                        "confirmations.cancelAllRequests.displayName",
                        "confirmations.cancelAllRequests.lore",
                        Map.of(),
                        confirmed -> {
                            if (confirmed) {
                                new FriendRequestActions((FriendNetPlugin) plugin).cancelAllRequests(player);
                                currentPage = 0;
                                buildInventory();
                            }
                        }
                )
        ));

        inventory.setItem(bottomRowStart + 4, GUIUtils.CreatePageIndicatorItem(
                player,
                currentPage,
                GUIUtils.CalculateMaxPage(requests.size(), requestsPerPage)
        ));

        setInteractableItem(bottomRowStart + 5, new ActionItemStack(
                GUIUtils.CreateBackItem(player),
                player,
                this::goBack,
                ActionItemStack.SoundProfile.NAVIGATION
        ));

        fillEmptySlots();
    }

    private ItemStack createRequestItem(FriendshipData request, UUID targetId) {
        String displayName = SpigotUtils.getPlayerDisplayName((FriendNetPlugin) plugin, targetId);
        if (displayName.isBlank()) {
            displayName = targetId.toString();
        }

        return SpigotUtils.createPlayerHead(targetId, displayName, createRequestLore(request));
    }

    private List<String> createRequestLore(FriendshipData request) {
        List<String> lore = new ArrayList<>();
        lore.add(locale("sentRequestsGUI.requestEntry.lore.status", Map.of("status", ChatColor.YELLOW + locale("sentRequestsGUI.requestEntry.status.pending"))));
        lore.add(locale("sentRequestsGUI.requestEntry.lore.sentAt", Map.of("date", ChatColor.YELLOW + formatTimestamp(request.getRequestSentTime()))));
        lore.add("");
        lore.add(locale("sentRequestsGUI.requestEntry.lore.clickToOpen"));
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

    private void fillEmptySlots() {
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, SpigotUtils.createFillerGlass());
            }
        }
    }
}
