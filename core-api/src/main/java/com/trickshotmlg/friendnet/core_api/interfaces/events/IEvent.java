package com.trickshotmlg.friendnet.core_api.interfaces.events;

import com.trickshotmlg.friendnet.core_api.enums.EventSource;

public interface IEvent {
    /**
     * Timestamp of when the event was created.
     */
    long getTimestamp();

    /**
     *
     * @return
     */
    EventSource getSource();
}
