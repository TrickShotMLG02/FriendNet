package com.trickshotmlg.friendnet.adapter_spigot.Utils;

import com.trickshotmlg.friendnet.core.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Map;
import java.util.UUID;

public final class MessageManager {

    private static FileConfiguration messages;
    private static JavaPlugin plugin;

    private MessageManager() {
        // Prevent instantiation
    }

    /**
     * Initializes the message manager.
     * Should be called once from your plugin's onEnable().
     */
    public static void init(JavaPlugin pl) {
        plugin = pl;
        loadMessages();
    }

    /**
     * Loads or reloads the messages.yml file.
     */
    public static void loadMessages() {
        if (plugin == null) {
            throw new IllegalStateException("MessageManager not initialized! Call init() in onEnable().");
        }

        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }

        messages = YamlConfiguration.loadConfiguration(file);
        Logger.debug("Loading Messages from " + file.getName());
    }

    /**
     * Gets a message by key, supports nested paths like "requests.sent".
     */
    public static String get(String key) {
        if (messages == null) {
            throw new IllegalStateException("Messages not loaded! Call MessageManager.init() first.");
        }

        String msg = messages.getString(key);
        if (msg == null) {
            return ChatColor.RED + "Message not found: " + key;
        }

        String prefix = messages.getString("prefix");
        if (prefix.length() > 0) {
            msg = prefix + " " + msg;
        }

        msg = ChatColor.translateAlternateColorCodes('&', msg);

        return msg;
    }

    /**
     * Gets a formatted message with placeholders replaced.
     * Example: format("requests.sent", Map.of("target", "Steve"))
     */
    public static String format(String key, Map<String, String> placeholders) {

        String message = get(key);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        return message;
    }

    /**
     * Sends a message to a specific CommandSender (player or console).
     * Supports placeholders.
     */
    public static void send(CommandSender sender, String key, Map<String, String> placeholders) {
        if (sender == null) return;
        sender.sendMessage(format(key, placeholders));
    }

    /**
     * Sends a message to a specific CommandSender without placeholders.
     */
    public static void send(CommandSender sender, String key) {
        send(sender, key, Map.of());
    }

    /**
     * Sends a message to a specific online Player.
     * Supports placeholders.
     */
    public static void send(Player player, String key, Map<String, String> placeholders) {
        if (player != null && player.isOnline()) {
            player.sendMessage(format(key, placeholders));
        }
    }

    /**
     * Sends a message to a specific online Player without placeholders.
     */
    public static void send(Player player, String key) {
        send(player, key, Map.of());
    }

    /**
     * Sends a message to a player identified by a UUID.
     * Tries to find an online player.
     */
    public static void send(UUID playerId, String key, Map<String, String> placeholders) {
        if (playerId == null) return;

        Player player = Bukkit.getPlayer(playerId);
        if (player != null && player.isOnline()) {
            send(player, key, placeholders);
        }
    }

    /**
     * Sends a message to a player identified by a UUID without placeholders.
     */
    public static void send(UUID playerId, String key) {
        send(playerId, key, Map.of());
    }

    /**
     * Sends a message to an OfflinePlayer.
     * If they are online, it sends to them; otherwise ignored.
     */
    public static void send(OfflinePlayer offlinePlayer, String key, Map<String, String> placeholders) {
        if (offlinePlayer == null) return;

        if (offlinePlayer.isOnline()) {
            Player online = offlinePlayer.getPlayer();
            if (online != null) {
                send(online, key, placeholders);
            }
        } else {
            // optional: log or queue for later delivery
        }
    }

    /**
     * Sends a message to an OfflinePlayer without placeholders.
     */
    public static void send(OfflinePlayer offlinePlayer, String key) {
        send(offlinePlayer, key, Map.of());
    }
}
