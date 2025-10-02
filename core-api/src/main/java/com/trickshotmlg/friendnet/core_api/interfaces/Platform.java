package com.trickshotmlg.friendnet.core_api.interfaces;

import java.util.Collection;
import java.util.UUID;

/**
 * Represents a platform-independent interface for interacting with the server
 * and its players.
 * <p>
 * This allows core modules (like FriendService) to interact with players
 * and scheduling tasks without depending on a specific server implementation
 * such as Spigot, Paper, BungeeCord, or Velocity.
 * </p>
 */
public interface Platform {

    /**
     * Retrieves a player by their unique ID.
     *
     * @param uuid the UUID of the player
     * @return the {@link PlatformPlayer} representing the player,
     *         or {@code null} if the player is not online or does not exist
     */
    PlatformPlayer getPlayer(UUID uuid);

    /**
     * Returns a collection of all currently online players.
     *
     * @return a {@link Collection} of {@link PlatformPlayer} objects
     *         representing all online players
     */
    Collection<PlatformPlayer> getOnlinePlayers();

    /**
     * Executes a task asynchronously.
     * <p>
     * Use this for tasks that might block or take time, such as
     * database operations or network calls, to avoid freezing the server.
     * </p>
     *
     * @param task the {@link Runnable} to execute asynchronously
     */
    void runAsync(Runnable task);

    /**
     * Executes a task synchronously on the main server thread.
     * <p>
     * Use this for operations that must run on the main thread,
     * such as interacting with the server API or player objects.
     * </p>
     *
     * @param task the {@link Runnable} to execute synchronously
     */
    void runSync(Runnable task);
}
