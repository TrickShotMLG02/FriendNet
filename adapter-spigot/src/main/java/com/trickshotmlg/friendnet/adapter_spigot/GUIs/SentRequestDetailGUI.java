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

public class SentRequestDetailGUI extends AbstractGUI {
    private static final String DEFAULT_TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm";

    private final UUID targetId;

    public SentRequestDetailGUI(JavaPlugin plugin, Player player, UUID targetId) {
        super(plugin, player, 9 * 5, "titles.sentRequestDetailGUI");
        this.targetId = targetId;
    }

    @Override
    protected String getInventoryTitle() {
        return locale("titles.sentRequestDetailGUI", Map.of("target", getTargetDisplayName()));
    }

    @Override
    protected void buildInventory() {
        interactableSlots.clear();
        inventory.clear();

        Optional<FriendshipData> request = getSentRequest();
        if (request.isEmpty()) {
            MessageManager.send(player, "friendRequest.cancel.sender.notFound", Map.of("target", getTargetDisplayName()));
            setInteractableItem(40, new ActionItemStack(
                    GUIUtils.CreateBackItem(player),
                    player,
                    this::goBack,
                    ActionItemStack.SoundProfile.NAVIGATION
            ));
            fillEmptySlots();
            return;
        }

        inventory.setItem(13, createTargetItem(request.get()));

        setInteractableItem(29, new ActionItemStack(
                GUIUtils.CreateLocalizedHead(
                        player,
                        GUIUtils.RED_X_TEXTURE,
                        "gui",
                        "sentRequestDetailGUI.buttons.cancel.displayName",
                        "sentRequestDetailGUI.buttons.cancel.lore"
                ),
                player,
                this::cancelRequest
        ));

        setInteractableItem(31, new ActionItemStack(
                SpigotUtils.createItem(
                        Material.BARRIER,
                        player,
                        "gui",
                        "sentRequestDetailGUI.buttons.block.displayName",
                        "sentRequestDetailGUI.buttons.block.lore"
                ),
                player,
                this::blockTarget
        ));

        setInteractableItem(33, new ActionItemStack(
                GUIUtils.CreateBackItem(player),
                player,
                this::goBack,
                ActionItemStack.SoundProfile.NAVIGATION
        ));

        fillEmptySlots();
    }

    private ItemStack createTargetItem(FriendshipData request) {
        return SpigotUtils.createPlayerHead(targetId, getTargetDisplayName(), createTargetLore(request));
    }

    private List<String> createTargetLore(FriendshipData request) {
        List<String> lore = new ArrayList<>();
        lore.add(locale("sentRequestsGUI.requestEntry.lore.status", Map.of("status", ChatColor.YELLOW + locale("sentRequestsGUI.requestEntry.status.pending"))));
        lore.add(locale("sentRequestsGUI.requestEntry.lore.sentAt", Map.of("date", ChatColor.YELLOW + formatTimestamp(request.getRequestSentTime()))));
        return lore;
    }

    private void cancelRequest() {
        new FriendRequestActions(((FriendNetPlugin) plugin).getFriendService()).cancelRequest(player, getTarget());
        goBack();
    }

    private void blockTarget() {
        new BlocklistActions((FriendNetPlugin) plugin).block(player, targetId);
        goBack();
    }

    private Optional<FriendshipData> getSentRequest() {
        return ((FriendNetPlugin) plugin)
                .getFriendService()
                .getFriendshipData(player.getUniqueId(), targetId)
                .filter(request -> request.getFriendshipStatus() == FriendshipStatus.Pending)
                .filter(request -> player.getUniqueId().equals(request.getRequesterId()));
    }

    private OfflinePlayer getTarget() {
        return Bukkit.getOfflinePlayer(targetId);
    }

    private String getTargetDisplayName() {
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

    private void fillEmptySlots() {
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, SpigotUtils.createFillerGlass());
            }
        }
    }
}
