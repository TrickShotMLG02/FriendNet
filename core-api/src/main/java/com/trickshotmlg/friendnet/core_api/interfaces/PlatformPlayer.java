package com.trickshotmlg.friendnet.core_api.interfaces;

import java.util.UUID;

public interface PlatformPlayer {
    UUID getUniqueId();
    String getName();
    void sendMessage(String message);
    boolean isOnline();
}
