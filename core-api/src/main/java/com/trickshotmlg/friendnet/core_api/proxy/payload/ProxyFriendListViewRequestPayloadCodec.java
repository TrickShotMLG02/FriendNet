package com.trickshotmlg.friendnet.core_api.proxy.payload;

import com.trickshotmlg.friendnet.core_api.proxy.ProxyErrorCode;
import com.trickshotmlg.friendnet.core_api.proxy.ProxyProtocolException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

public final class ProxyFriendListViewRequestPayloadCodec {

    private ProxyFriendListViewRequestPayloadCodec() {
    }

    public static byte[] encode(UUID viewedPlayerId) {
        if (viewedPlayerId == null) {
            return new byte[0];
        }

        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (DataOutputStream output = new DataOutputStream(bytes)) {
                output.writeLong(viewedPlayerId.getMostSignificantBits());
                output.writeLong(viewedPlayerId.getLeastSignificantBits());
            }
            return bytes.toByteArray();
        } catch (IOException e) {
            throw new ProxyProtocolException(ProxyErrorCode.BAD_REQUEST, "Could not encode friend list view request.", e);
        }
    }

    public static Optional<UUID> decode(byte[] payload) {
        if (payload == null || payload.length == 0) {
            return Optional.empty();
        }

        try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(payload))) {
            return Optional.of(new UUID(input.readLong(), input.readLong()));
        } catch (IOException | RuntimeException e) {
            throw new ProxyProtocolException(ProxyErrorCode.BAD_REQUEST, "Could not decode friend list view request.", e);
        }
    }
}
