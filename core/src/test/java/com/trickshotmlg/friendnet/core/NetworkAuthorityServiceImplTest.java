package com.trickshotmlg.friendnet.core;

import com.trickshotmlg.friendnet.core_api.enums.NetworkRole;
import com.trickshotmlg.friendnet.core_api.models.NetworkPlayerPresence;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NetworkAuthorityServiceImplTest {

    @Test
    public void networkRoleParsesSupportedModes() {
        assertEquals(NetworkRole.STANDALONE, NetworkRole.parse(null));
        assertEquals(NetworkRole.PROXY_AUTHORITY, NetworkRole.parse("Proxy"));
        assertEquals(NetworkRole.PROXY_BACKEND, NetworkRole.parse("proxy-backend"));
    }

    @Test
    public void authorityTracksOnlyOnlinePresences() {
        NetworkAuthorityServiceImpl service = new NetworkAuthorityServiceImpl(NetworkRole.PROXY_AUTHORITY);
        UUID onlinePlayer = UUID.randomUUID();
        UUID offlinePlayer = UUID.randomUUID();

        service.setPresence(new NetworkPlayerPresence(onlinePlayer, "Alex", "Alex", "hub", true, true, null));
        service.setPresence(new NetworkPlayerPresence(offlinePlayer, "Steve", "Steve", "survival", false, true, null));

        assertEquals(NetworkRole.PROXY_AUTHORITY, service.getNetworkRole());
        assertTrue(service.isProxyMode());
        assertTrue(service.ownsPersistentState());
        assertEquals(1, service.getOnlinePresences().size());
        assertEquals("hub", service.getPresence(onlinePlayer).flatMap(NetworkPlayerPresence::serverNameOptional).orElse(null));
    }

    @Test
    public void authorityCanRemovePresence() {
        NetworkAuthorityServiceImpl service = new NetworkAuthorityServiceImpl(NetworkRole.STANDALONE);
        UUID playerId = UUID.randomUUID();

        service.setPresence(new NetworkPlayerPresence(playerId, "Alex", "Alex", null, true, true, null));
        service.removePresence(playerId);

        assertFalse(service.getPresence(playerId).isPresent());
    }
}
