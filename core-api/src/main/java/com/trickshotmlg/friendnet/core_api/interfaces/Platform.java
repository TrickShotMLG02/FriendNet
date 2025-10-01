package com.trickshotmlg.friendnet.core_api.interfaces;

import java.util.Collection;
import java.util.UUID;

public interface Platform {
    PlatformPlayer getPlayer(UUID uuid);
    Collection<PlatformPlayer> getOnlinePlayers();
    void runAsync(Runnable task);
    void runSync(Runnable task);
}
