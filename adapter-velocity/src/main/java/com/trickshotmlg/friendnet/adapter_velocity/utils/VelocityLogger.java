package com.trickshotmlg.friendnet.adapter_velocity.utils;

import com.trickshotmlg.friendnet.core_api.interfaces.FriendNetLogger;

public class VelocityLogger implements FriendNetLogger {

    private final org.slf4j.Logger logger;

    public VelocityLogger(org.slf4j.Logger logger) {
        this.logger = logger;
    }

    @Override
    public void info(String message) {
        logger.info(message);
    }

    @Override
    public void warn(String message) {
        logger.warn(message);
    }

    @Override
    public void error(String message, Throwable throwable) {
        if (throwable == null) {
            logger.error(message);
        } else {
            logger.error(message, throwable);
        }
    }

    @Override
    public void debug(String message) {
        logger.info("[DEBUG] {}", message);
    }
}
