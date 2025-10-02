package com.trickshotmlg.friendnet.core_api.interfaces;

import java.util.UUID;

/**
 * Represents a player in a platform-independent way.
 * <p>
 * This interface abstracts away the specifics of the server implementation
 * (e.g., Spigot, BungeeCord, Velocity) and allows core systems like
 * {@link com.trickshotmlg.friendnet.core_api.interfaces.FriendService}
 * to interact with players generically.
 * </p>
 */
public interface PlatformPlayer {

    /**
     * Returns the unique identifier of the player.
     *
     * @return the {@link UUID} of the player
     */
    UUID getUniqueId();

    /**
     * Returns the name of the player.
     *
     * @return the player's name as a {@link String}
     */
    String getName();

    /**
     * Sends a message to the player.
     * <p>
     * Implementations should handle sending the message appropriately
     * for the underlying platform (e.g., Bukkit's {@code Player#sendMessage}).
     * </p>
     *
     * @param message the message to send to the player
     */
    void sendMessage(String message);

    /**
     * Checks whether the player is currently online.
     *
     * @return {@code true} if the player is online, {@code false} otherwise
     */
    boolean isOnline();
}
