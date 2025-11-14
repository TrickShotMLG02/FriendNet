package com.trickshotmlg.friendnet.core.events;

import com.trickshotmlg.friendnet.core_api.enums.EventSource;
import com.trickshotmlg.friendnet.core_api.events.AbstractEvent;
import com.trickshotmlg.friendnet.core_api.interfaces.PlatformPlayer;

public class PlayerJoinEvent extends AbstractEvent {
    private final PlatformPlayer player;

    public PlayerJoinEvent(EventSource source, PlatformPlayer player) {
        super(source);
        this.player = player;
    }

    public PlatformPlayer getPlayer() {
        return player;
    }
}
