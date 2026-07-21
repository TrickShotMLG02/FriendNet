package com.trickshotmlg.friendnet.core_api.proxy;

import java.util.Arrays;
import java.util.UUID;

public record ProxyProtocolMessage(
        ProxyMessageKind kind,
        int version,
        UUID correlationId,
        ProxyRequestType requestType,
        ProxyResponseStatus responseStatus,
        ProxyErrorCode errorCode,
        UUID playerId,
        String sourceServer,
        long timestampMillis,
        UUID nonce,
        byte[] payload,
        byte[] signature
) {
    public ProxyProtocolMessage {
        if (kind == null) {
            throw new IllegalArgumentException("Message kind cannot be null.");
        }
        if (correlationId == null) {
            throw new IllegalArgumentException("Correlation id cannot be null.");
        }
        if (requestType == null) {
            throw new IllegalArgumentException("Request type cannot be null.");
        }
        if (nonce == null) {
            throw new IllegalArgumentException("Nonce cannot be null.");
        }
        responseStatus = responseStatus == null ? ProxyResponseStatus.SUCCESS : responseStatus;
        errorCode = errorCode == null ? ProxyErrorCode.NONE : errorCode;
        sourceServer = sourceServer == null ? "" : sourceServer;
        payload = payload == null ? new byte[0] : Arrays.copyOf(payload, payload.length);
        signature = signature == null ? new byte[0] : Arrays.copyOf(signature, signature.length);
    }

    @Override
    public byte[] payload() {
        return Arrays.copyOf(payload, payload.length);
    }

    @Override
    public byte[] signature() {
        return Arrays.copyOf(signature, signature.length);
    }

    public ProxyProtocolMessage withSignature(byte[] signature) {
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
    }
}
