package com.trickshotmlg.friendnet.core_api.interfaces;

/**
 * A platform-independent logger interface for the FriendNet plugin.
 * <p>
 * This interface abstracts away platform-specific logging APIs (e.g., Bukkit's
 * {@code JavaPlugin#getLogger()}, BungeeCord's {@code ProxyServer#getLogger()})
 * and allows core modules and services to log messages consistently across
 * different server platforms.
 * </p>
 * <p>
 * Implementations of this interface should handle message formatting, log levels,
 * and output according to the underlying platform. For example, a Spigot adapter
 * might prefix debug messages or print stack traces for errors.
 * </p>
 */
public interface FriendNetLogger {

    /**
     * Logs an informational message.
     * <p>
     * Informational messages should describe normal operations, such as
     * a player joining or a friend being added.
     * </p>
     *
     * @param message the message to log
     */
    void info(String message);

    /**
     * Logs a warning message.
     * <p>
     * Warning messages indicate a potential issue that does not prevent
     * the plugin from functioning but might require attention.
     * </p>
     *
     * @param message the warning message to log
     */
    void warn(String message);

    /**
     * Logs an error message along with an optional {@link Throwable}.
     * <p>
     * Error messages indicate failures or exceptions that might affect
     * the normal operation of the plugin. Implementations may print
     * the stack trace of the throwable if it is not {@code null}.
     * </p>
     *
     * @param message   the error message to log
     * @param throwable the associated exception or error, may be {@code null}
     */
    void error(String message, Throwable throwable);

    /**
     * Logs a debug message.
     * <p>
     * Debug messages are intended for development and troubleshooting.
     * They may be verbose and can be disabled in production environments.
     * Implementations may optionally add a "[DEBUG]" prefix or other markers.
     * </p>
     *
     * @param message the debug message to log
     */
    void debug(String message);
}
