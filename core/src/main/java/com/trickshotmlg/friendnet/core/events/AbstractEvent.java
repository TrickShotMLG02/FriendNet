package com.trickshotmlg.friendnet.core.events;

import com.trickshotmlg.friendnet.core_api.enums.EventSource;
import com.trickshotmlg.friendnet.core_api.interfaces.events.IEvent;

import java.util.StringJoiner;

public abstract class AbstractEvent implements IEvent {
    private final long timestamp;
    private final EventSource source;

    public AbstractEvent(EventSource source) {
        this.timestamp = System.currentTimeMillis();
        this.source = source;
    }

    @Override
    public long getTimestamp() { return timestamp; }

    @Override
    public EventSource getSource() { return source; }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(", ", getClass().getSimpleName() + "{", "}");
        sj.add("event_source=" + source);
        sj.add("timestamp=" + timestamp);

        // Automatically include all declared fields from the subclass
        try {
            for (var field : this.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                Object value = field.get(this);
                sj.add(field.getName() + "=" + value);
            }
        } catch (IllegalAccessException e) {
            sj.add("error_accessing_fields");
        }

        return sj.toString();
    }
}
