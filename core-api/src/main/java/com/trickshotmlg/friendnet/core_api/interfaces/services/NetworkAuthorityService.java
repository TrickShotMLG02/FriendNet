package com.trickshotmlg.friendnet.core_api.interfaces.services;

import com.trickshotmlg.friendnet.core_api.enums.NetworkRole;
import com.trickshotmlg.friendnet.core_api.models.NetworkPlayerPresence;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface NetworkAuthorityService {

    NetworkRole getNetworkRole();

    default boolean isProxyMode() {
        return getNetworkRole().isProxyMode();
    }

    default boolean ownsPersistentState() {
        return getNetworkRole().ownsPersistentState();
    }

    default boolean delegatesPersistentState() {
        return getNetworkRole().delegatesPersistentState();
    }

    Optional<NetworkPlayerPresence> getPresence(UUID playerId);

    Collection<NetworkPlayerPresence> getOnlinePresences();

    void setPresence(NetworkPlayerPresence presence);

    void removePresence(UUID playerId);
}
