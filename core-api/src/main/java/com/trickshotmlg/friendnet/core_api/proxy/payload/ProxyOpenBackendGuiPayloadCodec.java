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

public final class ProxyOpenBackendGuiPayloadCodec {

    private ProxyOpenBackendGuiPayloadCodec() {
    }

    public static byte[] encode(ProxyBackendGuiType guiType) {
        return encode(guiType, null);
    }

    public static byte[] encode(ProxyBackendGuiType guiType, UUID viewedPlayerId) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            DataOutputStream output = new DataOutputStream(bytes);
            output.writeUTF(guiType.name());
            output.writeBoolean(viewedPlayerId != null);
            if (viewedPlayerId != null) {
                output.writeLong(viewedPlayerId.getMostSignificantBits());
                output.writeLong(viewedPlayerId.getLeastSignificantBits());
            }
            output.flush();
            return bytes.toByteArray();
        } catch (IOException e) {
            throw new ProxyProtocolException(ProxyErrorCode.BAD_REQUEST, "Could not encode backend GUI request.", e);
        }
    }

    public static ProxyBackendGuiType decode(byte[] payload) {
        return decodeRequest(payload).guiType();
    }

    public static ProxyOpenBackendGuiPayload decodeRequest(byte[] payload) {
        try {
            DataInputStream input = new DataInputStream(new ByteArrayInputStream(payload));
            ProxyBackendGuiType guiType = ProxyBackendGuiType.valueOf(input.readUTF());
            UUID viewedPlayerId = null;
            if (input.available() > 0 && input.readBoolean()) {
                viewedPlayerId = new UUID(input.readLong(), input.readLong());
            }
            return new ProxyOpenBackendGuiPayload(guiType, Optional.ofNullable(viewedPlayerId));
        } catch (IOException | IllegalArgumentException e) {
            throw new ProxyProtocolException(ProxyErrorCode.BAD_REQUEST, "Could not decode backend GUI request.", e);
        }
    }

    public record ProxyOpenBackendGuiPayload(ProxyBackendGuiType guiType, Optional<UUID> viewedPlayerId) {
    }
}
