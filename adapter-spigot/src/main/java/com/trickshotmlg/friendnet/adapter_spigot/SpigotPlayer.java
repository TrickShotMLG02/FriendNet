package com.trickshotmlg.friendnet.adapter_spigot;

import com.trickshotmlg.friendnet.core_api.interfaces.PlatformPlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Implementation of {@link PlatformPlayer} for Spigot/Bukkit.
 * <p>
 * Wraps a Bukkit {@link Player} object to provide a platform-independent
 * interface for accessing player information and sending messages.
 * </p>
 */
public class SpigotPlayer implements PlatformPlayer {

    /**
     * The underlying Bukkit player instance.
     */
    private final Player player;

    /**
     * Creates a new {@link SpigotPlayer} wrapper around a Bukkit {@link Player}.
     *
     * @param player the Bukkit player to wrap
     */
    public SpigotPlayer(Player player) {
        this.player = player;
    }

    /**
     * Returns the unique ID of the player.
     *
     * @return the {@link UUID} of the player
     */
    @Override
    public UUID getUniqueId() {
        return player.getUniqueId();
    }

    /**
     * Returns the name of the player.
     *
     * @return the player's name
     */
    @Override
    public String getName() {
        return player.getName();
    }

    /**
     * Sends a message to the player.
     *
     * @param message the message to send
     */
    @Override
    public void sendMessage(String message) {
        player.sendMessage(message);
    }

    /**
     * Checks if the player is currently online.
     *
     * @return {@code true} if the player is online, {@code false} otherwise
     */
    @Override
    public boolean isOnline() {
        return player.isOnline();
    }

    /**
     * @param permission the permission to check
     * @return
     */
    @Override
    public boolean hasPermission(String permission) {
        return player.hasPermission(permission);
    }
}
