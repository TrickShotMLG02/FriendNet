package com.trickshotmlg.friendnet.adapter_spigot.GUIs;

import com.trickshotmlg.friendnet.adapter_spigot.Actions.BlocklistActions;
import com.trickshotmlg.friendnet.adapter_spigot.Actions.FriendRequestActions;
import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
import com.trickshotmlg.friendnet.adapter_spigot.GUIs.Items.ActionItemStack;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.GUIUtils;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.MessageManager;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.SpigotUtils;
import com.trickshotmlg.friendnet.core_api.enums.FriendshipStatus;
import com.trickshotmlg.friendnet.core_api.models.FriendshipData;
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

    public FriendRequestDetailGUI(JavaPlugin plugin, Player player, UUID requesterId) {
        super(plugin, player, 9 * 5, "titles.friendRequestDetailGUI");
        this.requesterId = requesterId;
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
                this::denyRequest
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
                this::blockRequester
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
        new FriendRequestActions(((FriendNetPlugin) plugin).getFriendService()).acceptRequest(player, getRequester());
        goBack();
    }

    private void denyRequest() {
        new FriendRequestActions(((FriendNetPlugin) plugin).getFriendService()).denyRequest(player, getRequester());
        goBack();
    }

    private void blockRequester() {
        new BlocklistActions((FriendNetPlugin) plugin).block(player, requesterId);
        goBack();
    }

    private Optional<FriendshipData> getPendingRequest() {
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
        String displayName = SpigotUtils.getPlayerDisplayName((FriendNetPlugin) plugin, requesterId);
        return displayName.isBlank() ? requesterId.toString() : displayName;
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
