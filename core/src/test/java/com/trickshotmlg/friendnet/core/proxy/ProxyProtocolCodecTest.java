package com.trickshotmlg.friendnet.core.proxy;

import com.trickshotmlg.friendnet.core_api.proxy.ProxyErrorCode;
import com.trickshotmlg.friendnet.core_api.proxy.ProxyProtocolCodec;
import com.trickshotmlg.friendnet.core_api.proxy.ProxyProtocolException;
import com.trickshotmlg.friendnet.core_api.proxy.ProxyProtocolMessage;
import com.trickshotmlg.friendnet.core_api.proxy.ProxyRequestType;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyFriendEntry;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyFriendListViewPayload;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyFriendListViewPayloadCodec;
import junit.framework.TestCase;

import java.util.List;
import java.util.UUID;

public class ProxyProtocolCodecTest extends TestCase {

    public void testSignedRequestRoundTripVerifiesWithSharedToken() {
        String token = "secret-token";
        UUID playerId = UUID.randomUUID();
        ProxyProtocolMessage request = ProxyProtocolCodec.request(
                ProxyRequestType.FRIEND_LIST_VIEW,
                playerId,
                "lobby",
                new byte[0]
        );

        ProxyProtocolMessage decoded = ProxyProtocolCodec.decode(ProxyProtocolCodec.encodeSigned(request, token));
        ProxyProtocolCodec.verify(decoded, token, System.currentTimeMillis());

        assertEquals(playerId, decoded.playerId());
        assertEquals("lobby", decoded.sourceServer());
        assertEquals(ProxyRequestType.FRIEND_LIST_VIEW, decoded.requestType());
    }

    public void testWrongTokenFailsVerification() {
        ProxyProtocolMessage request = ProxyProtocolCodec.request(
                ProxyRequestType.FRIEND_LIST_VIEW,
                UUID.randomUUID(),
                "lobby",
                new byte[0]
        );

        ProxyProtocolMessage decoded = ProxyProtocolCodec.decode(ProxyProtocolCodec.encodeSigned(request, "expected"));

        try {
            ProxyProtocolCodec.verify(decoded, "actual", System.currentTimeMillis());
            fail("Expected HMAC verification to fail.");
        } catch (ProxyProtocolException e) {
            assertEquals(ProxyErrorCode.AUTHENTICATION_FAILED, e.getErrorCode());
        }
    }

    public void testFriendListPayloadRoundTrip() {
        UUID friendId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        ProxyFriendListViewPayload payload = new ProxyFriendListViewPayload(
                List.of(new ProxyFriendEntry(friendId, "Alex", true, "survival")),
                List.of(new ProxyFriendEntry(requesterId, "Steve", false, ""))
        );

        ProxyFriendListViewPayload decoded = ProxyFriendListViewPayloadCodec.decode(
                ProxyFriendListViewPayloadCodec.encode(payload)
        );

        assertEquals(friendId, decoded.friends().get(0).playerId());
        assertEquals("survival", decoded.friends().get(0).currentServerName());
        assertEquals(requesterId, decoded.pendingRequests().get(0).playerId());
    }
}
