package com.trickshotmlg.friendnet.core;

import com.trickshotmlg.friendnet.core_api.enums.NetworkRole;
import com.trickshotmlg.friendnet.core_api.interfaces.services.NetworkAuthorityService;
import com.trickshotmlg.friendnet.core_api.models.NetworkPlayerPresence;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class NetworkAuthorityServiceImpl implements NetworkAuthorityService {

    private final NetworkRole networkRole;
    private final Map<UUID, NetworkPlayerPresence> presences = new ConcurrentHashMap<>();

    public NetworkAuthorityServiceImpl(NetworkRole networkRole) {
        this.networkRole = networkRole == null ? NetworkRole.STANDALONE : networkRole;
    }

    @Override
    public NetworkRole getNetworkRole() {
        return networkRole;
    }

    @Override
    public Optional<NetworkPlayerPresence> getPresence(UUID playerId) {
        if (playerId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(presences.get(playerId));
    }

    @Override
    public Collection<NetworkPlayerPresence> getOnlinePresences() {
        return presences.values().stream()
                .filter(NetworkPlayerPresence::online)
                .toList();
    }

    @Override
    public void setPresence(NetworkPlayerPresence presence) {
        if (presence == null) {
            throw new IllegalArgumentException("Presence cannot be null.");
        }
        presences.put(presence.playerId(), presence);
    }

    @Override
    public void removePresence(UUID playerId) {
        if (playerId != null) {
            presences.remove(playerId);
        }
    }
}
