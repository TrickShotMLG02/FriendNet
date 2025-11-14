package com.trickshotmlg.friendnet.core_api.interfaces.events;

public interface IEventSerializer<T extends IEvent> {
    byte[] serialize(T event);
    T deserialize(byte[] data);
}
