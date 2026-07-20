package com.trickshotmlg.friendnet.core_api.proxy.payload;

import com.trickshotmlg.friendnet.core_api.proxy.ProxyErrorCode;
import com.trickshotmlg.friendnet.core_api.proxy.ProxyProtocolException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

public final class ProxyActionRequestPayloadCodec {

    private ProxyActionRequestPayloadCodec() {
    }

    public static byte[] encode(ProxyActionRequestPayload payload) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            DataOutputStream output = new DataOutputStream(bytes);
            output.writeUTF(payload.actionType().name());
            output.writeBoolean(payload.targetId() != null);
            if (payload.targetId() != null) {
                output.writeLong(payload.targetId().getMostSignificantBits());
                output.writeLong(payload.targetId().getLeastSignificantBits());
            }
            output.writeUTF(payload.targetName());
            output.writeBoolean(payload.refreshFriendList());
            output.flush();
            return bytes.toByteArray();
        } catch (IOException e) {
            throw new ProxyProtocolException(ProxyErrorCode.BAD_REQUEST, "Could not encode proxy action request.", e);
        }
    }

    public static ProxyActionRequestPayload decode(byte[] payload) {
        try {
            DataInputStream input = new DataInputStream(new ByteArrayInputStream(payload));
            ProxyActionType actionType = ProxyActionType.valueOf(input.readUTF());
            UUID targetId = null;
            if (input.readBoolean()) {
                targetId = new UUID(input.readLong(), input.readLong());
            }
            String targetName = input.readUTF();
            boolean refreshFriendList = input.readBoolean();
            return new ProxyActionRequestPayload(actionType, targetId, targetName, refreshFriendList);
        } catch (IOException | IllegalArgumentException e) {
            throw new ProxyProtocolException(ProxyErrorCode.BAD_REQUEST, "Could not decode proxy action request.", e);
        }
    }
}
