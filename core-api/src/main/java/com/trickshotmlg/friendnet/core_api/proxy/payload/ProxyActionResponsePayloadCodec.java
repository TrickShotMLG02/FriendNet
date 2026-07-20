package com.trickshotmlg.friendnet.core_api.proxy.payload;

import com.trickshotmlg.friendnet.core_api.proxy.ProxyErrorCode;
import com.trickshotmlg.friendnet.core_api.proxy.ProxyProtocolException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ProxyActionResponsePayloadCodec {

    private ProxyActionResponsePayloadCodec() {
    }

    public static byte[] encode(ProxyActionResponsePayload payload) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            DataOutputStream output = new DataOutputStream(bytes);
            output.writeBoolean(payload.success());
            output.writeInt(payload.messages().size());
            for (ProxyMessagePayload message : payload.messages()) {
                output.writeUTF(message.recipient().name());
                output.writeBoolean(message.recipientId() != null);
                if (message.recipientId() != null) {
                    output.writeLong(message.recipientId().getMostSignificantBits());
                    output.writeLong(message.recipientId().getLeastSignificantBits());
                }
                output.writeUTF(message.key());
                output.writeInt(message.placeholders().size());
                for (Map.Entry<String, String> entry : message.placeholders().entrySet()) {
                    output.writeUTF(entry.getKey());
                    output.writeUTF(entry.getValue());
                }
            }
            output.writeBoolean(payload.friendListView() != null);
            if (payload.friendListView() != null) {
                byte[] viewPayload = ProxyFriendListViewPayloadCodec.encode(payload.friendListView());
                output.writeInt(viewPayload.length);
                output.write(viewPayload);
            }
            output.flush();
            return bytes.toByteArray();
        } catch (IOException e) {
            throw new ProxyProtocolException(ProxyErrorCode.BAD_REQUEST, "Could not encode proxy action response.", e);
        }
    }

    public static ProxyActionResponsePayload decode(byte[] payload) {
        try {
            DataInputStream input = new DataInputStream(new ByteArrayInputStream(payload));
            boolean success = input.readBoolean();
            int messageCount = input.readInt();
            java.util.List<ProxyMessagePayload> messages = new java.util.ArrayList<>(messageCount);
            for (int i = 0; i < messageCount; i++) {
                ProxyMessageRecipient recipient = ProxyMessageRecipient.valueOf(input.readUTF());
                UUID recipientId = null;
                if (input.readBoolean()) {
                    recipientId = new UUID(input.readLong(), input.readLong());
                }
                String key = input.readUTF();
                int placeholderCount = input.readInt();
                Map<String, String> placeholders = new HashMap<>(placeholderCount);
                for (int j = 0; j < placeholderCount; j++) {
                    placeholders.put(input.readUTF(), input.readUTF());
                }
                messages.add(new ProxyMessagePayload(recipient, recipientId, key, placeholders));
            }

            ProxyFriendListViewPayload friendListView = null;
            if (input.readBoolean()) {
                byte[] viewPayload = input.readNBytes(input.readInt());
                friendListView = ProxyFriendListViewPayloadCodec.decode(viewPayload);
            }
            return new ProxyActionResponsePayload(success, messages, friendListView);
        } catch (IOException | IllegalArgumentException e) {
            throw new ProxyProtocolException(ProxyErrorCode.BAD_REQUEST, "Could not decode proxy action response.", e);
        }
    }
}
