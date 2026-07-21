package com.trickshotmlg.friendnet.core_api.proxy.payload;

import com.trickshotmlg.friendnet.core_api.proxy.ProxyErrorCode;
import com.trickshotmlg.friendnet.core_api.proxy.ProxyProtocolException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public final class ProxyDisplayNameUpdatePayloadCodec {

    private ProxyDisplayNameUpdatePayloadCodec() {
    }

    public static byte[] encode(ProxyDisplayNameUpdatePayload payload) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (DataOutputStream output = new DataOutputStream(bytes)) {
                output.writeUTF(payload.displayName());
            }
            return bytes.toByteArray();
        } catch (IOException e) {
            throw new ProxyProtocolException(ProxyErrorCode.INTERNAL_ERROR, "Could not encode display name update.", e);
        }
    }

    public static ProxyDisplayNameUpdatePayload decode(byte[] data) {
        try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(data))) {
            return new ProxyDisplayNameUpdatePayload(input.readUTF());
        } catch (IOException e) {
            throw new ProxyProtocolException(ProxyErrorCode.BAD_REQUEST, "Could not decode display name update.", e);
        }
    }
}
