package com.trickshotmlg.friendnet.core.events;

import com.trickshotmlg.friendnet.core_api.interfaces.events.IEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class EventBus {
    // Map event class -> list of subscribers
    private static final Map<Class<? extends IEvent>, List<Consumer<IEvent>>> subscribers = new ConcurrentHashMap<>();

    /**
     * Subscribe to a specific event type.
     * @param eventType class of the event
     * @param handler function that handles the event
     * @param <T> type of the event
     */
    public static <T extends IEvent> void subscribe(Class<T> eventType, Consumer<T> handler) {
        subscribers.computeIfAbsent(eventType, k -> new ArrayList<>())
                .add((Consumer<IEvent>) handler);
    }

    /**
     * Unsubscribe a handler from a specific event type.
     */
    public static <T extends IEvent> void unsubscribe(Class<T> eventType, Consumer<T> handler) {
        List<Consumer<IEvent>> handlers = subscribers.get(eventType);
        if (handlers != null) {
            handlers.remove(handler);
        }
    }

    /**
     * Publish an event to all subscribers.
     */
    public static void publish(IEvent event) {
        Class<?> clazz = event.getClass();
        while (clazz != null) {
            List<Consumer<IEvent>> handlers = subscribers.get(clazz);
            if (handlers != null) {
                for (Consumer<IEvent> handler : new ArrayList<>(handlers)) {
                    try {
                        handler.accept(event);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
    }

    /**
     * Clear all subscribers
     */
    public static void clear() {
        subscribers.clear();
    }
}
