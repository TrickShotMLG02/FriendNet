package com.trickshotmlg.friendnet.core.proxy;

import com.trickshotmlg.friendnet.core_api.proxy.ProxyErrorCode;
import com.trickshotmlg.friendnet.core_api.proxy.ProxyProtocolCodec;
import com.trickshotmlg.friendnet.core_api.proxy.ProxyProtocolException;
import com.trickshotmlg.friendnet.core_api.proxy.ProxyProtocolMessage;
import com.trickshotmlg.friendnet.core_api.proxy.ProxyRequestType;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyActionRequestPayload;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyActionRequestPayloadCodec;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyActionResponsePayload;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyActionResponsePayloadCodec;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyActionType;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyBackendGuiType;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyFriendEntry;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyFriendListViewPayload;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyFriendListViewPayloadCodec;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyMessagePayload;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyMessageRecipient;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyOpenBackendGuiPayloadCodec;
import junit.framework.TestCase;

import java.util.List;
import java.util.Map;
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
        long requestSentTimeMillis = 1784580000000L;
        long friendSinceMillis = 1784580100000L;
        long lastSeenMillis = 1784580200000L;
        ProxyFriendListViewPayload payload = new ProxyFriendListViewPayload(
                List.of(new ProxyFriendEntry(friendId, "Alex", true, "survival", true, requestSentTimeMillis, friendSinceMillis, -1L, lastSeenMillis)),
                List.of(new ProxyFriendEntry(requesterId, "Steve", false, "", false, requestSentTimeMillis, -1L, -1L, -1L)),
                List.of(),
                List.of(),
                false,
                false,
                true,
                false,
                false,
                "de"
        );

        ProxyFriendListViewPayload decoded = ProxyFriendListViewPayloadCodec.decode(
                ProxyFriendListViewPayloadCodec.encode(payload)
        );

        assertEquals(friendId, decoded.friends().get(0).playerId());
        assertEquals("survival", decoded.friends().get(0).currentServerName());
        assertEquals(friendSinceMillis, decoded.friends().get(0).friendSinceMillis());
        assertEquals(lastSeenMillis, decoded.friends().get(0).lastSeenMillis());
        assertEquals(requesterId, decoded.pendingRequests().get(0).playerId());
        assertEquals(requestSentTimeMillis, decoded.pendingRequests().get(0).requestSentTimeMillis());
        assertFalse(decoded.allowFriendRequests());
        assertFalse(decoded.showOnlineStatus());
        assertTrue(decoded.autoAcceptFriends());
        assertEquals("de", decoded.localeCode());
    }

    public void testActionPayloadRoundTrip() {
        UUID targetId = UUID.randomUUID();
        ProxyActionRequestPayload request = new ProxyActionRequestPayload(
                ProxyActionType.ACCEPT_REQUEST,
                targetId,
                "Alex",
                true
        );

        ProxyActionRequestPayload decodedRequest = ProxyActionRequestPayloadCodec.decode(
                ProxyActionRequestPayloadCodec.encode(request)
        );

        assertEquals(ProxyActionType.ACCEPT_REQUEST, decodedRequest.actionType());
        assertEquals(targetId, decodedRequest.targetId());
        assertEquals("Alex", decodedRequest.targetName());
        assertTrue(decodedRequest.refreshFriendList());

        ProxyActionResponsePayload response = new ProxyActionResponsePayload(
                true,
                List.of(new ProxyMessagePayload(
                        ProxyMessageRecipient.SENDER,
                        null,
                        "friendRequest.accept.sender.success",
                        Map.of("target", "Alex")
                )),
                new ProxyFriendListViewPayload(List.of(), List.of(), List.of(), List.of())
        );

        ProxyActionResponsePayload decodedResponse = ProxyActionResponsePayloadCodec.decode(
                ProxyActionResponsePayloadCodec.encode(response)
        );

        assertTrue(decodedResponse.success());
        assertEquals("friendRequest.accept.sender.success", decodedResponse.messages().get(0).key());
        assertEquals("Alex", decodedResponse.messages().get(0).placeholders().get("target"));
        assertNotNull(decodedResponse.friendListView());
    }

    public void testOpenBackendGuiPayloadRoundTrip() {
        ProxyBackendGuiType decoded = ProxyOpenBackendGuiPayloadCodec.decode(
                ProxyOpenBackendGuiPayloadCodec.encode(ProxyBackendGuiType.REQUESTS)
        );

        assertEquals(ProxyBackendGuiType.REQUESTS, decoded);
    }
}
