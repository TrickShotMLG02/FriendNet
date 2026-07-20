package com.trickshotmlg.friendnet.adapter_spigot.GUIs;

import com.trickshotmlg.friendnet.adapter_spigot.Actions.BlocklistActions;
import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
import com.trickshotmlg.friendnet.adapter_spigot.GUIs.Items.ActionItemStack;
import com.trickshotmlg.friendnet.adapter_spigot.Services.FriendGuiViewData;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.GUIUtils;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.MessageManager;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.ProxyActionResponseRenderer;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.SpigotUtils;
import com.trickshotmlg.friendnet.core_api.models.FriendshipData;
import com.trickshotmlg.friendnet.core_api.models.PlayerData;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyActionRequestPayload;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyActionType;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyFriendEntry;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
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
import java.util.Optional;
import java.util.UUID;

public class FriendDetailGUI extends AbstractGUI {

    private static final String DEFAULT_TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm";

    private final UUID friendId;
    private FriendGuiViewData viewData;

    public FriendDetailGUI(JavaPlugin plugin, Player player, UUID friendId) {
        this(plugin, player, friendId, null);
    }

    public FriendDetailGUI(JavaPlugin plugin, Player player, UUID friendId, FriendGuiViewData viewData) {
        super(plugin, player, 9 * 5, "titles.friendDetailGUI");
        this.friendId = friendId;
        this.viewData = viewData;
    }

    @Override
    protected String getInventoryTitle() {
        return locale("titles.friendDetailGUI", Map.of("target", getFriendDisplayName()));
    }

    @Override
    protected void buildInventory() {
        interactableSlots.clear();
        inventory.clear();

        Optional<FriendshipData> friendship = getFriendship();
        if (friendship.isEmpty()) {
            MessageManager.send(player, "friend.remove.sender.notFound", Map.of("target", getFriendDisplayName()));
            setInteractableItem(40, new ActionItemStack(
                    GUIUtils.CreateBackItem(player),
                    player,
                    () -> goBack(),
                    ActionItemStack.SoundProfile.NAVIGATION
            ));
            fillEmptySlots();
            return;
        }

        FriendshipData friendshipData = friendship.get();
        PlayerData playerData = SpigotUtils.getPlayerData((FriendNetPlugin) plugin, friendId);

        inventory.setItem(13, SpigotUtils.createPlayerHead(friendId, getFriendDisplayName(), createFriendLore(friendshipData, playerData)));

        setInteractableItem(28, new ActionItemStack(
                createFavouriteItem(friendshipData.isFavourite()),
                player,
                () -> toggleFavourite(friendshipData)
        ));

        setInteractableItem(30, new ActionItemStack(
                SpigotUtils.createItem(
                        Material.WRITABLE_BOOK,
                        player,
                        "gui",
                        "friendDetailGUI.buttons.message.displayName",
                        "friendDetailGUI.buttons.message.lore"
                ),
                player,
                this::sendMessageShortcut
        ));

        setInteractableItem(32, new ActionItemStack(
                SpigotUtils.createItem(
                        Material.BARRIER,
                        player,
                        "gui",
                        "friendDetailGUI.buttons.blockPlayer.displayName",
                        "friendDetailGUI.buttons.blockPlayer.lore"
                ),
                player,
                () -> openConfirmation(
                        "titles.confirmationGUI",
                        "confirmations.blockPlayer.displayName",
                        "confirmations.blockPlayer.lore",
                        Map.of("target", getFriendDisplayName()),
                        confirmed -> {
                            if (confirmed) {
                                blockPlayer();
                            }
                        }
                )
        ));

        setInteractableItem(34, new ActionItemStack(
                SpigotUtils.createItem(
                        Material.REDSTONE_BLOCK,
                        player,
                        "gui",
                        "friendDetailGUI.buttons.removeFriend.displayName",
                        "friendDetailGUI.buttons.removeFriend.lore"
                ),
                player,
                () -> openConfirmation(
                        "titles.confirmationGUI",
                        "confirmations.removeFriend.displayName",
                        "confirmations.removeFriend.lore",
                        Map.of("target", getFriendDisplayName()),
                        confirmed -> {
                            if (confirmed) {
                                removeFriend();
                            }
                        }
                )
        ));

        setInteractableItem(40, new ActionItemStack(
                GUIUtils.CreateBackItem(player),
                player,
                () -> goBack(),
                ActionItemStack.SoundProfile.NAVIGATION
        ));

        fillEmptySlots();
    }

    private ItemStack createFavouriteItem(boolean isFavourite) {
        return SpigotUtils.createItem(
                isFavourite ? Material.NETHER_STAR : Material.GRAY_DYE,
                player,
                "gui",
                isFavourite
                        ? "friendDetailGUI.buttons.favourite.remove.displayName"
                        : "friendDetailGUI.buttons.favourite.add.displayName",
                isFavourite
                        ? "friendDetailGUI.buttons.favourite.remove.lore"
                        : "friendDetailGUI.buttons.favourite.add.lore"
        );
    }

    private List<String> createFriendLore(FriendshipData friendshipData, PlayerData playerData) {
        List<String> lore = new ArrayList<>();

        lore.add(locale("friendEntries.lore.status", Map.of("status", formatOnlineStatus())));
        lore.add(locale("friendEntries.lore.favourite", Map.of("value", formatBoolean(friendshipData.isFavourite()))));
        lore.add("");
        lore.add(locale("friendEntries.lore.friendsSince", Map.of("date", ChatColor.YELLOW + formatTimestamp(friendshipData.getFriendSince()))));
        lore.add(locale("friendEntries.lore.lastSeen", Map.of("date", formatLastSeen(playerData))));

        return lore;
    }

    private void toggleFavourite(FriendshipData friendshipData) {
        boolean newState = !friendshipData.isFavourite();
        friendshipData.setFavourite(newState);
        FriendNetPlugin friendNetPlugin = (FriendNetPlugin) plugin;
        friendNetPlugin.getFriendService().putFriendshipData(friendshipData);
        friendNetPlugin.getDatabaseService().save(friendshipData);
        MessageManager.send(player, newState ? "friend.favourite.enabled" : "friend.favourite.disabled", Map.of("target", getFriendDisplayName()));
        buildInventory();
    }

    private void removeFriend() {
        FriendNetPlugin friendNetPlugin = (FriendNetPlugin) plugin;
        if (friendNetPlugin.isProxyBackendMode()) {
            executeProxyAction(ProxyActionType.REMOVE_FRIEND, true, true);
            return;
        }

        boolean success = ((FriendNetPlugin) plugin).getFriendService().removeFriend(player.getUniqueId(), friendId);
        if (success) {
            MessageManager.send(player, "friend.remove.sender.success", Map.of("target", getFriendDisplayName()));
            goBack();
            return;
        }

        MessageManager.send(player, "friend.remove.sender.notFound", Map.of("target", getFriendDisplayName()));
        goBack();
    }

    private void blockPlayer() {
        FriendNetPlugin friendNetPlugin = (FriendNetPlugin) plugin;
        if (friendNetPlugin.isProxyBackendMode()) {
            executeProxyAction(ProxyActionType.BLOCK_PLAYER, true, true);
            return;
        }

        new BlocklistActions((FriendNetPlugin) plugin).block(player, friendId);
        goBack();
    }

    private void sendMessageShortcut() {
        String command = "/msg " + getFriendCommandName() + " ";
        player.closeInventory();

        TextComponent message = new TextComponent(locale("friendDetailGUI.message.prompt", Map.of("target", getFriendDisplayName())));
        message.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command));
        message.setHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(locale("friendDetailGUI.message.hover", Map.of("command", command))).create()
        ));
        player.spigot().sendMessage(message);
    }

    private Optional<FriendshipData> getFriendship() {
        FriendNetPlugin friendNetPlugin = (FriendNetPlugin) plugin;
        if (friendNetPlugin.isProxyBackendMode() && viewData != null) {
            return viewData.friends().stream()
                    .filter(friendship -> friendId.equals(friendship.getOtherPlayerId(player.getUniqueId())))
                    .findFirst();
        }

        return ((FriendNetPlugin) plugin).getFriendService().getFriendshipData(player.getUniqueId(), friendId);
    }

    private String getFriendDisplayName() {
        if (viewData != null) {
            ProxyFriendEntry proxyEntry = viewData.proxyEntry(friendId);
            if (proxyEntry != null && !proxyEntry.displayName().isBlank()) {
                return proxyEntry.displayName();
            }
        }

        String displayName = SpigotUtils.getPlayerDisplayName((FriendNetPlugin) plugin, friendId);
        return displayName.isBlank() ? friendId.toString() : displayName;
    }

    private String getFriendCommandName() {
        String name = SpigotUtils.getPlayerName(friendId);
        return name.isBlank() ? getFriendDisplayName() : name;
    }

    private String formatOnlineStatus() {
        return isFriendOnline() ? locale("friendEntries.status.online") : locale("friendEntries.status.offline");
    }

    private String formatBoolean(boolean value) {
        return value ? locale("friendEntries.boolean.yes") : locale("friendEntries.boolean.no");
    }

    private String formatLastSeen(PlayerData playerData) {
        if (isFriendOnline()) {
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

    private boolean isFriendOnline() {
        if (viewData != null) {
            ProxyFriendEntry proxyEntry = viewData.proxyEntry(friendId);
            if (proxyEntry != null) {
                return proxyEntry.online();
            }
        }

        return Bukkit.getPlayer(friendId) != null && ((FriendNetPlugin) plugin).getPlayerService().isOnline(friendId);
    }

    private void executeProxyAction(ProxyActionType actionType, boolean refreshFriendList, boolean closeToParent) {
        ProxyActionRequestPayload request = new ProxyActionRequestPayload(
                actionType,
                friendId,
                getFriendDisplayName(),
                refreshFriendList
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
                    if (closeToParent && response.success()) {
                        goBack();
                    } else {
                        buildInventory();
                    }
                })
        );
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
