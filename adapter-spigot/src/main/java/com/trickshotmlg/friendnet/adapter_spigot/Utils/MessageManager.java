package com.trickshotmlg.friendnet.adapter_spigot.Utils;

import com.trickshotmlg.friendnet.core.Logger;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    public static String get(String key, boolean prependPrefix) {
        if (messages == null) {
            throw new IllegalStateException("Messages not loaded! Call MessageManager.init() first.");
        }

        String msg = messages.getString(key);
        if (msg == null) {
            return ChatColor.RED + "Message not found: " + key;
        }

        String prefix = messages.getString("prefix");
        if (prefix.length() > 0 && prependPrefix) {
            msg = prefix + " " + msg;
        }

        msg = ChatColor.translateAlternateColorCodes('&', msg);

        return msg;
    }

    public static String get(String key) {
        return get(key, true);
    }

    public static BaseComponent[] formatComponent(String key, Map<String, Object> placeholders, boolean prependPrefix) {
        String raw = get(key, prependPrefix);

        Pattern pattern = Pattern.compile("%([^%]+)%");
        Matcher matcher = pattern.matcher(raw);
        List<BaseComponent> components = new ArrayList<>();

        net.md_5.bungee.api.ChatColor lastColor = net.md_5.bungee.api.ChatColor.WHITE;
        int lastEnd = 0;

        while (matcher.find()) {
            // text before placeholder
            if (matcher.start() > lastEnd) {
                String part = raw.substring(lastEnd, matcher.start());
                components.addAll(Arrays.asList(TextComponent.fromLegacyText(part)));
                lastColor = extractLastColor(part, lastColor);
            }

            String matcherKey = matcher.group(1);
            Object replacement = placeholders.get(matcherKey);

            BaseComponent[] replComponents;
            if (replacement instanceof BaseComponent) replComponents = new BaseComponent[]{(BaseComponent) replacement};
            else if (replacement instanceof BaseComponent[]) replComponents = (BaseComponent[]) replacement;
            else replComponents = TextComponent.fromLegacyText(replacement != null ? replacement.toString() : matcher.group(0));

            // apply last color if placeholder has no color
            for (BaseComponent comp : replComponents) {
                if (comp.getColorRaw() == null) comp.setColor(lastColor);
            }

            components.addAll(Arrays.asList(replComponents));
            lastEnd = matcher.end();
        }

        // remaining text
        if (lastEnd < raw.length()) {
            components.addAll(Arrays.asList(TextComponent.fromLegacyText(raw.substring(lastEnd))));
        }
        return components.toArray(new BaseComponent[0]);
    }

    public static BaseComponent[] formatComponent(String key, Map<String, Object> placeholders) {
        return formatComponent(key, placeholders, true);
    }

    private static net.md_5.bungee.api.ChatColor extractLastColor(String text, net.md_5.bungee.api.ChatColor fallback) {
        net.md_5.bungee.api.ChatColor color = fallback;
        for (int i = 0; i < text.length() - 1; i++) {
            char c = text.charAt(i);
            if ((c == '§' || c == '&')) {
                net.md_5.bungee.api.ChatColor code = net.md_5.bungee.api.ChatColor.getByChar(text.charAt(i + 1));
                if (code != null) {
                    if (!code.equals(net.md_5.bungee.api.ChatColor.RESET)) {
                        color = code; // update to last color
                    } else if (code == net.md_5.bungee.api.ChatColor.RESET) {
                        color = net.md_5.bungee.api.ChatColor.WHITE; // reset to default
                    }
                }
            }
        }
        return color;
    }




    /**
     * Creates a clickable text component from a message key.
     *
     * @param messageKey      Key in messages.yml for the text
     * @param placeholders    Placeholders to replace in the text
     * @param hoverMessageKey Optional key for hover text (can be null)
     * @param hoverPlaceholders Placeholders for hover text
     * @param action          Optional click action (can be null)
     * @param value           Value for click action (command, URL, etc.)
     */
    public static TextComponent createButton(
            String messageKey,
            Map<String, Object> placeholders,
            String hoverMessageKey,
            Map<String, Object> hoverPlaceholders,
            ClickEvent.Action action,
            String value
    ) {
        // format main message
        BaseComponent[] base = MessageManager.formatComponent(messageKey, placeholders, false);
        TextComponent component = new TextComponent();
        for (BaseComponent c : base) component.addExtra(c);

        // set hover if provided
        if (hoverMessageKey != null) {
            BaseComponent[] hover = MessageManager.formatComponent(
                    hoverMessageKey,
                    hoverPlaceholders != null ? hoverPlaceholders : Map.of(),
                    false
            );
            component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover));
        }

        // set click if provided
        if (action != null && value != null) {
            component.setClickEvent(new ClickEvent(action, value));
        }

        return component;
    }

    /**
     * Shortcut for buttons without hover text.
     */
    public static TextComponent createButton(
            String messageKey,
            Map<String, Object> placeholders,
            ClickEvent.Action action,
            String value
    ) {
        return createButton(messageKey, placeholders, null, null, action, value);
    }

    /**
     * Shortcut for buttons with no placeholders and no hover text.
     */
    public static TextComponent createButton(String messageKey, ClickEvent.Action action, String value) {
        return createButton(messageKey, Map.of(), null, null, action, value);
    }




    /**
     * Gets a formatted message with placeholders replaced.
     * Example: format("requests.sent", Map.of("target", "Steve"))
     */
    @Deprecated
    public static String format(String key, Map<String, Object> placeholders) {
        /*
        String message = get(key);
        for (Map.Entry<String, Object> entry : placeholders.entrySet()) {
            message = message.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        return message;
         */
        throw new UnsupportedOperationException("This is going to be removed.");
    }

    /**
     * Sends a message to a specific CommandSender (player or console).
     * Supports placeholders.
     */
    public static void send(CommandSender sender, String key, Map<String, Object> placeholders) {
        if (sender == null) return;
        sender.spigot().sendMessage(formatComponent(key, placeholders));
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
    public static void send(Player player, String key, Map<String, Object> placeholders) {
        if (player != null && player.isOnline()) {
            player.spigot().sendMessage(formatComponent(key, placeholders));
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
    public static void send(UUID playerId, String key, Map<String, Object> placeholders) {
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
    public static void send(OfflinePlayer offlinePlayer, String key, Map<String, Object> placeholders) {
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
