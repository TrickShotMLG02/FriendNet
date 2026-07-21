package com.trickshotmlg.friendnet.adapter_spigot.Utils;

import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
import com.trickshotmlg.friendnet.core_api.interfaces.services.PlayerService;
import com.trickshotmlg.friendnet.core_api.models.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SpigotUtils {
    private static final Pattern SKIN_URL_PATTERN = Pattern.compile("\"url\"\\s*:\\s*\"([^\"]+)\"");

    private SpigotUtils() {
        // prevent instantiation
    }

    /**
     * Get online Player from UUID.
     */
    public static Optional<Player> getOnlinePlayer(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        return Optional.ofNullable(player);
    }

    /**
     * Get OfflinePlayer from UUID (may be online or offline).
     */
    public static OfflinePlayer getOfflinePlayer(UUID uuid) {
        return Bukkit.getOfflinePlayer(uuid);
    }

    /**
     * Get a player's display name if online, otherwise fall back to name or empty string.
     */
    public static String getPlayerDisplayName(UUID uuid) {
        return getPlayerDisplayName(null, uuid);
    }

    public static String getPlayerDisplayName(FriendNetPlugin plugin, UUID uuid) {

        Optional<Player> onlinePlayer = getOnlinePlayer(uuid);

        // player is currently online, fetch current display name
        if (onlinePlayer.isPresent()) {
            return onlinePlayer.get().getDisplayName();
        }

        // try to fetch last known player display name
        if (plugin != null) {
            PlayerData pd = getPlayerData(plugin, uuid);

            if (pd != null && pd.getLastDisplayName() != null && !pd.getLastDisplayName().isBlank()) {
                return pd.getLastDisplayName();
            }
        }

        // if all fails, fall back to bukkit functionality
        return getOnlinePlayer(uuid)
                .map(Player::getDisplayName)
                .orElseGet(() -> getOfflinePlayer(uuid).getName() != null ? getOfflinePlayer(uuid).getName() : "");
    }

    public static PlayerData getPlayerData(FriendNetPlugin plugin, UUID uuid) {
        if (plugin == null || uuid == null) {
            return null;
        }

        PlayerService playerService = plugin.getPlayerService();
        PlayerData playerData = playerService.getPlayerData(uuid);
        if (playerData != null) {
            return playerData;
        }

        return plugin.getDatabaseService()
                .find(uuid, PlayerData.class)
                .map(loadedPlayerData -> {
                    playerService.putPlayerData(loadedPlayerData);
                    return loadedPlayerData;
                })
                .orElse(null);
    }

    /**
     * Get a player's name if online, otherwise fall back to offline player name.
     */
    public static String getPlayerName(UUID uuid) {
        return getOnlinePlayer(uuid)
                .map(Player::getName)
                .orElseGet(() -> getOfflinePlayer(uuid).getName() != null ? getOfflinePlayer(uuid).getName() : "");
    }

    /**
     * Check if a player is online by UUID.
     */
    public static boolean isOnline(UUID uuid) {
        return getOnlinePlayer(uuid).isPresent();
    }

    /**
     * Try to get a UUID from a username. This only works for players who have joined before.
     */
    public static Optional<UUID> getUUIDFromName(String name) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(name);
        if (player != null) {
            return Optional.of(player.getUniqueId());
        }
        return Optional.empty();
    }

    /**
     * Safely cast a CommandSender to Player if possible.
     */
    public static Optional<Player> asPlayer(org.bukkit.command.CommandSender sender) {
        if (sender instanceof Player player) {
            return Optional.of(player);
        }
        return Optional.empty();
    }

    /**
     * Creates a player head item for a specific player.
     *
     * @param playerId UUID of the player whose skin to use
     * @param displayName Display name of the item
     * @param lore Lore lines
     * @param amount Item stack amount
     * @return ItemStack of a player head
     */
    public static ItemStack createPlayerHead(UUID playerId, String displayName, List<String> lore, int amount) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD, amount);
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        if (meta != null) {
            applyPlayerHeadProfile(meta, playerId, displayName);
            applyItemMeta(meta, displayName, lore);

            head.setItemMeta(meta);
        }

        return head;
    }

    // Optional overload for single amount (default to 1)
    public static ItemStack createPlayerHead(UUID playerId, String displayName, List<String> lore) {
        return createPlayerHead(playerId, displayName, lore, 1);
    }

    /**
     * Creates a player head with a custom texture.
     *
     * @param base64Texture Base64 encoded Minecraft texture JSON, a textures.minecraft.net URL, or a texture hash.
     * @param displayName Display name of the item
     * @param lore Lore lines
     * @param amount Item stack amount
     * @return ItemStack of a custom textured player head
     */
    public static ItemStack createCustomPlayerHead(String base64Texture, String displayName, List<String> lore, int amount) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD, amount);
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        if (meta != null) {
            if (base64Texture != null && !base64Texture.isBlank()) {
                applyCustomHeadTexture(meta, base64Texture);
            }

            applyItemMeta(meta, displayName, lore);
            head.setItemMeta(meta);
        }

        return head;
    }

    public static ItemStack createCustomPlayerHead(String base64Texture, String displayName, List<String> lore) {
        return createCustomPlayerHead(base64Texture, displayName, lore, 1);
    }

    public static ItemStack setItemLore(ItemStack item, List<String> lore) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    public static ItemStack setGlint(ItemStack item, boolean glint) {
        if (item == null) {
            return null;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setEnchantmentGlintOverride(glint);
            item.setItemMeta(meta);
        }

        return item;
    }

    public static ItemStack createFillerGlass() {
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1);
        ItemMeta meta = glass.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            glass.setItemMeta(meta);
        }
        return glass;
    }

    public static ItemStack createItem(Material material, String displayName) {
        return createItem(material, displayName, null, 1);
    }

    public static ItemStack createItem(Material material, String displayName, List<String> lore) {
        return createItem(material, displayName, lore, 1);
    }

    public static ItemStack createItem(Material material, Player player, String type, String displayNameKey, String loreKey, int amount) {
        String displayName = FriendNetPlugin.LocaleManager.getMessage(player.getUniqueId(), type, displayNameKey);
        List<String> lore = SpigotUtils.parseStringList(
                FriendNetPlugin.LocaleManager.getMessage(player.getUniqueId(), type, loreKey)
        );

        return createItem(material, displayName, lore, amount);
    }

    public static ItemStack createItem(Material material, Player player, String type, String displayNameKey, String loreKey) {
        return createItem(material, player, type, displayNameKey, loreKey, 1);
    }

    public static ItemStack createItem(Material material, String displayName, List<String> lore, int amount) {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        applyItemMeta(meta, displayName, lore);

        item.setItemMeta(meta);
        return item;
    }

    private static void applyItemMeta(ItemMeta meta, String displayName, List<String> lore) {
        if (displayName != null && !displayName.isEmpty()) {
            meta.setDisplayName(displayName);
        }

        if (lore != null && !lore.isEmpty()) {
            meta.setLore(lore);
        }
    }

    private static void applyPlayerHeadProfile(SkullMeta meta, UUID playerId, String displayName) {
        Player onlinePlayer = Bukkit.getPlayer(playerId);
        if (onlinePlayer != null) {
            meta.setOwningPlayer(onlinePlayer);
            return;
        }

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerId);
        String profileName = offlinePlayer.getName();
        if (profileName != null && !profileName.isBlank()) {
            meta.setOwningPlayer(offlinePlayer);
            return;
        }

        // Unknown synthetic/dev UUIDs should remain unresolved so Paper does not query Mojang for them.
    }

    private static String normalizeBase64Texture(String texture) {
        String normalizedTexture = texture.trim();

        if (normalizedTexture.startsWith("http://") || normalizedTexture.startsWith("https://")) {
            return encodeSkinUrl(normalizedTexture);
        }

        if (normalizedTexture.matches("[a-fA-F0-9]{32,}")) {
            return encodeSkinUrl("http://textures.minecraft.net/texture/" + normalizedTexture);
        }

        String decoded = new String(Base64.getDecoder().decode(normalizedTexture), StandardCharsets.UTF_8);
        Matcher matcher = SKIN_URL_PATTERN.matcher(decoded);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Texture payload does not contain a skin URL");
        }

        return normalizedTexture;
    }

    private static String encodeSkinUrl(String skinUrl) {
        String payload = "{\"textures\":{\"SKIN\":{\"url\":\"" + skinUrl + "\"}}}";
        return Base64.getEncoder().encodeToString(payload.getBytes(StandardCharsets.UTF_8));
    }

    private static void applyCustomHeadTexture(SkullMeta meta, String texture) {
        String normalizedTexture = normalizeBase64Texture(texture);
        String skinUrl = extractSkinUrl(normalizedTexture);

        try {
            UUID profileId = UUID.nameUUIDFromBytes(normalizedTexture.getBytes(StandardCharsets.UTF_8));
            PlayerProfile profile = Bukkit.createPlayerProfile(profileId, "FriendNetHead");
            PlayerTextures textures = profile.getTextures();
            textures.setSkin(new URL(skinUrl));
            profile.setTextures(textures);
            meta.setOwnerProfile(profile);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Texture payload contains an invalid skin URL: " + skinUrl, e);
        }
    }

    private static String extractSkinUrl(String base64Texture) {
        String decoded = new String(Base64.getDecoder().decode(base64Texture), StandardCharsets.UTF_8);
        Matcher matcher = SKIN_URL_PATTERN.matcher(decoded);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Texture payload does not contain a skin URL");
        }

        return matcher.group(1);
    }

    public static <T> List<T> safeSubList(List<T> list, int start, int end) {
        if (list == null || list.isEmpty() || start >= list.size()) return Collections.emptyList();
        end = Math.min(end, list.size());
        return list.subList(start, end);
    }

    public static net.md_5.bungee.api.ChatColor extractLastColor(String text, net.md_5.bungee.api.ChatColor fallback) {
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

    public static List<String> parseStringList(String input) {
        if (input == null || input.isEmpty()) return Collections.emptyList();

        // Remove leading and trailing brackets if present
        input = input.trim();
        if (input.contains("\n")) {
            return Arrays.stream(input.split("\\R"))
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .toList();
        }

        if (input.startsWith("[") && input.endsWith("]")) {
            input = input.substring(1, input.length() - 1);
        }

        // Split by comma, preserving color codes or other formatting
        String[] parts = input.split(",(?=(?:[^§]*§[^,]*)*$)");

        List<String> result = new ArrayList<>();
        for (String part : parts) {
            result.add(part.trim());
        }

        return result;
    }
}
