package com.trickshotmlg.friendnet.core;

import com.trickshotmlg.friendnet.core_api.interfaces.FriendNetLogger;

public final class Logger {

    private static boolean enableDebug;

    private static FriendNetLogger logger = new DefaultLogger();

    private Logger() {}

    public static void setLogger(FriendNetLogger newLogger) {
        if (newLogger == null) throw new IllegalArgumentException("Logger cannot be null");
        logger = newLogger;
    }

    /**
     *
     * @param status the status of debug enabled
     */
    public static void enableDebug(boolean status) {
        enableDebug = status;
    }

    /**
     * @param message the message to log
     */
    public static void info(String message) {
        logger.info(message);
    }

    /**
     * @param message the warning message to log
     */
    public static void warn(String message) {
        logger.warn(message);
    }

    /**
     * @param message   the error message to log
     * @param throwable the associated exception or error, may be {@code null}
     */
    public static void error(String message, Throwable throwable) {
        logger.error(message, throwable);
    }

    /**
     * @param message the debug message to log
     */
    public static void debug(String message) {
        if (enableDebug) logger.debug(message);
    }

    // default fallback if platform didn't initialize
    private static class DefaultLogger implements FriendNetLogger {
        @Override
        public void info(String message) { System.out.println("[FriendNet/INFO] " + message); }
        @Override
        public void warn(String message) { System.out.println("[FriendNet/WARN] " + message); }
        @Override
        public void error(String message, Throwable throwable) {
            System.err.println("[FriendNet/ERROR] " + message);
            throwable.printStackTrace(System.err);
        }
        @Override
        public void debug(String message) { System.out.println("[FriendNet/DEBUG] " + message); }
    }
}
