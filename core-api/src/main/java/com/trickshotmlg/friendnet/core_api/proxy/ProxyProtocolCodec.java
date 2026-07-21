package com.trickshotmlg.friendnet.core_api.proxy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

public final class ProxyProtocolCodec {

    private ProxyProtocolCodec() {
    }

    public static ProxyProtocolMessage request(
            ProxyRequestType requestType,
            UUID playerId,
            String sourceServer,
            byte[] payload
    ) {
        return new ProxyProtocolMessage(
                ProxyMessageKind.REQUEST,
                FriendNetProxyProtocol.VERSION,
                UUID.randomUUID(),
                requestType,
                ProxyResponseStatus.SUCCESS,
                ProxyErrorCode.NONE,
                playerId,
                sourceServer,
                System.currentTimeMillis(),
                UUID.randomUUID(),
                payload,
                new byte[0]
        );
    }

    public static ProxyProtocolMessage response(
            ProxyProtocolMessage request,
            ProxyResponseStatus status,
            ProxyErrorCode errorCode,
            byte[] payload
    ) {
        return new ProxyProtocolMessage(
                ProxyMessageKind.RESPONSE,
                FriendNetProxyProtocol.VERSION,
                request.correlationId(),
                request.requestType(),
                status,
                errorCode,
                request.playerId(),
                request.sourceServer(),
                System.currentTimeMillis(),
                UUID.randomUUID(),
                payload,
                new byte[0]
        );
    }

    public static byte[] encodeSigned(ProxyProtocolMessage message, String token) {
        byte[] unsigned = encode(message, false);
        return encode(message.withSignature(ProxyProtocolHmac.sign(unsigned, token)), true);
    }

    public static ProxyProtocolMessage decode(byte[] data) {
        try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(data))) {
            ProxyMessageKind kind = ProxyMessageKind.values()[input.readInt()];
            int version = input.readInt();
            UUID correlationId = readUuid(input);
            ProxyRequestType requestType = ProxyRequestType.values()[input.readInt()];
            ProxyResponseStatus responseStatus = ProxyResponseStatus.values()[input.readInt()];
            ProxyErrorCode errorCode = ProxyErrorCode.values()[input.readInt()];
            UUID playerId = readNullableUuid(input);
            String sourceServer = input.readUTF();
            long timestampMillis = input.readLong();
            UUID nonce = readUuid(input);
            byte[] payload = readBytes(input);
            byte[] signature = readBytes(input);
            return new ProxyProtocolMessage(
                    kind,
                    version,
                    correlationId,
                    requestType,
                    responseStatus,
                    errorCode,
                    playerId,
                    sourceServer,
                    timestampMillis,
                    nonce,
                    payload,
                    signature
            );
        } catch (IOException | RuntimeException e) {
            throw new ProxyProtocolException(ProxyErrorCode.BAD_REQUEST, "Could not decode proxy protocol message.", e);
        }
    }

    public static void verify(ProxyProtocolMessage message, String token, long nowMillis) {
        if (message.version() != FriendNetProxyProtocol.VERSION) {
            throw new ProxyProtocolException(ProxyErrorCode.UNSUPPORTED_VERSION, "Unsupported proxy protocol version: " + message.version());
        }

        long skew = Math.abs(nowMillis - message.timestampMillis());
        if (skew > FriendNetProxyProtocol.MAX_CLOCK_SKEW_MILLIS) {
            throw new ProxyProtocolException(ProxyErrorCode.AUTHENTICATION_FAILED, "Proxy protocol message timestamp is stale.");
        }

        if (!ProxyProtocolHmac.verify(encode(message, false), message.signature(), token)) {
            throw new ProxyProtocolException(ProxyErrorCode.AUTHENTICATION_FAILED, "Proxy protocol signature is invalid.");
        }
    }

    private static byte[] encode(ProxyProtocolMessage message, boolean includeSignature) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (DataOutputStream output = new DataOutputStream(bytes)) {
                output.writeInt(message.kind().ordinal());
                output.writeInt(message.version());
                writeUuid(output, message.correlationId());
                output.writeInt(message.requestType().ordinal());
                output.writeInt(message.responseStatus().ordinal());
                output.writeInt(message.errorCode().ordinal());
                writeNullableUuid(output, message.playerId());
                output.writeUTF(message.sourceServer());
                output.writeLong(message.timestampMillis());
                writeUuid(output, message.nonce());
                writeBytes(output, message.payload());
                writeBytes(output, includeSignature ? message.signature() : new byte[0]);
            }
            return bytes.toByteArray();
        } catch (IOException e) {
            throw new ProxyProtocolException(ProxyErrorCode.INTERNAL_ERROR, "Could not encode proxy protocol message.", e);
        }
    }

    private static void writeUuid(DataOutputStream output, UUID uuid) throws IOException {
        output.writeLong(uuid.getMostSignificantBits());
        output.writeLong(uuid.getLeastSignificantBits());
    }

    private static UUID readUuid(DataInputStream input) throws IOException {
        return new UUID(input.readLong(), input.readLong());
    }

    private static void writeNullableUuid(DataOutputStream output, UUID uuid) throws IOException {
        output.writeBoolean(uuid != null);
        if (uuid != null) {
            writeUuid(output, uuid);
        }
    }

    private static UUID readNullableUuid(DataInputStream input) throws IOException {
        return input.readBoolean() ? readUuid(input) : null;
    }

    private static void writeBytes(DataOutputStream output, byte[] data) throws IOException {
        output.writeInt(data.length);
        output.write(data);
    }

    private static byte[] readBytes(DataInputStream input) throws IOException {
        int length = input.readInt();
        if (length < 0) {
            throw new IOException("Negative byte array length.");
        }
        byte[] data = new byte[length];
        input.readFully(data);
        return data;
    }
}
