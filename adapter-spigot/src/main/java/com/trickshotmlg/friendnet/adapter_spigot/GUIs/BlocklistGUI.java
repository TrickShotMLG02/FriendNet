package com.trickshotmlg.friendnet.adapter_spigot.GUIs;

import com.trickshotmlg.friendnet.adapter_spigot.Actions.BlocklistActions;
import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
import com.trickshotmlg.friendnet.adapter_spigot.GUIs.Items.ActionItemStack;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.GUIUtils;
import com.trickshotmlg.friendnet.adapter_spigot.Services.FriendGuiViewData;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.MessageManager;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.ProxyActionResponseRenderer;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.SpigotUtils;
import com.trickshotmlg.friendnet.core_api.models.BlocklistData;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyActionRequestPayload;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyActionType;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyFriendEntry;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BlocklistGUI extends AbstractGUI {
    private static final String DEFAULT_TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm";
    private final int blockedRows = 3;
    private final int blockedPerPage = blockedRows * 9;
    private int currentPage = 0;
    private FriendGuiViewData viewData;
    private FriendGuiViewData currentViewData;

    public BlocklistGUI(JavaPlugin plugin, Player player) {
        this(plugin, player, null);
    }

    public BlocklistGUI(JavaPlugin plugin, Player player, FriendGuiViewData viewData) {
        super(plugin, player, 9 * 5, "titles.blocklistGUI");
        this.viewData = viewData;
    }

    @Override
    protected void buildInventory() {
        interactableSlots.clear();
        inventory.clear();

        FriendNetPlugin friendNetPlugin = (FriendNetPlugin) plugin;
        BlocklistActions actions = new BlocklistActions(friendNetPlugin);
        currentViewData = friendNetPlugin.isProxyBackendMode() && viewData != null
                ? viewData
                : FriendGuiViewData.local(
                friendNetPlugin.getFriendService().getFriendships(player.getUniqueId()).stream().toList(),
                friendNetPlugin.getFriendService().getPendingRequests(player.getUniqueId()).stream().toList(),
                friendNetPlugin.getFriendService().getSentRequests(player.getUniqueId()).stream().toList(),
                actions.getBlockedPlayers(player.getUniqueId())
        );
        List<BlocklistData> blockedPlayers = currentViewData.blockedPlayers();
        clampCurrentPage(blockedPlayers.size());

        int startIndex = currentPage * blockedPerPage;
        int endIndex = Math.min(startIndex + blockedPerPage, blockedPlayers.size());
        List<BlocklistData> visibleBlockedPlayers = SpigotUtils.safeSubList(blockedPlayers, startIndex, endIndex);

        for (int i = 0; i < visibleBlockedPlayers.size(); i++) {
            BlocklistData blockedPlayer = visibleBlockedPlayers.get(i);
            setInteractableItem(i, new ActionItemStack(
                    createBlockedPlayerItem(blockedPlayer),
                    player,
                    () -> openConfirmation(
                            "titles.confirmationGUI",
                            "confirmations.unblockPlayer.displayName",
                            "confirmations.unblockPlayer.lore",
                            Map.of("target", getDisplayName(blockedPlayer.getBlockedId())),
                            confirmed -> {
                                if (confirmed) {
                                    unblock(actions, blockedPlayer.getBlockedId());
                                }
                            }
                    )
            ));
        }

        if (blockedPlayers.isEmpty()) {
            inventory.setItem(13, GUIUtils.CreateEmptyStateItem(player, "blocklistGUI.emptyListMessage"));
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

        if (endIndex < blockedPlayers.size()) {
            setInteractableItem(bottomRowStart + 8, new ActionItemStack(
                    GUIUtils.CreateNextPageItem(player),
                    player,
                    () -> {
                        int maxPage = GUIUtils.CalculateMaxPage(blockedPlayers.size(), blockedPerPage) - 1;
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
                        Material.NAME_TAG,
                        player,
                        "gui",
                        "blocklistGUI.buttons.addBlockedPlayer.displayName",
                        "blocklistGUI.buttons.addBlockedPlayer.lore"
                ),
                player,
                () -> openChild(new BlockPlayerInputGUI(plugin, player)),
                ActionItemStack.SoundProfile.NAVIGATION
        ));

        inventory.setItem(bottomRowStart + 4, GUIUtils.CreatePageIndicatorItem(
                player,
                currentPage,
                GUIUtils.CalculateMaxPage(blockedPlayers.size(), blockedPerPage)
        ));

        setInteractableItem(bottomRowStart + 5, new ActionItemStack(
                SpigotUtils.createItem(
                        Material.LAVA_BUCKET,
                        player,
                        "gui",
                        "blocklistGUI.buttons.clearBlocklist.displayName",
                        "blocklistGUI.buttons.clearBlocklist.lore"
                ),
                player,
                () -> openConfirmation(
                        "titles.confirmationGUI",
                        "confirmations.clearBlocklist.displayName",
                        "confirmations.clearBlocklist.lore",
                        Map.of(),
                        confirmed -> {
                            if (confirmed) {
                                clear(actions);
                            }
                        }
                )
        ));

        setInteractableItem(bottomRowStart + 7, new ActionItemStack(
                GUIUtils.CreateBackItem(player),
                player,
                () -> goBack(),
                ActionItemStack.SoundProfile.NAVIGATION
        ));

        fillEmptySlots();
    }

    private ItemStack createBlockedPlayerItem(BlocklistData blockedPlayer) {
        UUID blockedId = blockedPlayer.getBlockedId();
        return SpigotUtils.createPlayerHead(blockedId, getDisplayName(blockedId), createBlockedPlayerLore(blockedPlayer));
    }

    private String getDisplayName(UUID playerId) {
        ProxyFriendEntry proxyEntry = currentViewData != null ? currentViewData.proxyEntry(playerId) : null;
        if (proxyEntry != null && !proxyEntry.displayName().isBlank()) {
            return proxyEntry.displayName();
        }

        String displayName = SpigotUtils.getPlayerDisplayName((FriendNetPlugin) plugin, playerId);
        return displayName.isBlank() ? playerId.toString() : displayName;
    }

    private List<String> createBlockedPlayerLore(BlocklistData blockedPlayer) {
        List<String> lore = new ArrayList<>();
        lore.add(locale("blocklistGUI.blockedPlayer.lore.blockedAt", Map.of("date", ChatColor.YELLOW + formatTimestamp(blockedPlayer.getBlockedAt()))));
        lore.add(locale("blocklistGUI.blockedPlayer.lore.status", Map.of("status", formatOnlineStatus(blockedPlayer.getBlockedId()))));
        lore.add("");
        lore.add(locale("blocklistGUI.blockedPlayer.lore.clickToUnblock"));
        return lore;
    }

    private String formatOnlineStatus(UUID playerId) {
        ProxyFriendEntry proxyEntry = currentViewData != null ? currentViewData.proxyEntry(playerId) : null;
        if (proxyEntry != null) {
            return proxyEntry.online() ? locale("friendEntries.status.online") : locale("friendEntries.status.offline");
        }

        boolean online = Bukkit.getPlayer(playerId) != null && ((FriendNetPlugin) plugin).getPlayerService().isOnline(playerId);
        return online ? locale("friendEntries.status.online") : locale("friendEntries.status.offline");
    }

    private void unblock(BlocklistActions actions, UUID blockedId) {
        if (((FriendNetPlugin) plugin).isProxyBackendMode()) {
            executeProxyAction(new ProxyActionRequestPayload(ProxyActionType.UNBLOCK_PLAYER, blockedId, getDisplayName(blockedId), true));
            return;
        }

        actions.unblock(player, blockedId);
        buildInventory();
    }

    private void clear(BlocklistActions actions) {
        if (((FriendNetPlugin) plugin).isProxyBackendMode()) {
            executeProxyAction(new ProxyActionRequestPayload(ProxyActionType.CLEAR_BLOCKLIST, null, "", true));
            return;
        }

        actions.clear(player);
        currentPage = 0;
        buildInventory();
    }

    private void executeProxyAction(ProxyActionRequestPayload request) {
        ((FriendNetPlugin) plugin).getFriendGuiService().executeAction(player, request).whenComplete((response, throwable) ->
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (throwable != null) {
                        MessageManager.send(player, "commandFeedback.proxyBackendGuiUnavailable");
                        return;
                    }

                    ProxyActionResponseRenderer.render(player, response);
                    if (response.friendListView() != null) {
                        viewData = FriendGuiViewData.fromProxyPayload(player.getUniqueId(), response.friendListView());
                    }
                    currentPage = 0;
                    buildInventory();
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

    private void clampCurrentPage(int itemCount) {
        int maxPage = GUIUtils.CalculateMaxPage(itemCount, blockedPerPage) - 1;
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
