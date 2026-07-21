package com.trickshotmlg.friendnet.core_api.proxy.payload;

import com.trickshotmlg.friendnet.core_api.proxy.ProxyErrorCode;
import com.trickshotmlg.friendnet.core_api.proxy.ProxyProtocolException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class ProxyBackendCommandPermissionsPayloadCodec {

    private ProxyBackendCommandPermissionsPayloadCodec() {
    }

    public static byte[] encode(ProxyBackendCommandPermissionsPayload payload) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (DataOutputStream output = new DataOutputStream(bytes)) {
                output.writeInt(payload.allowedCommandPaths().size());
                for (String commandPath : payload.allowedCommandPaths()) {
                    output.writeUTF(commandPath);
                }
            }
            return bytes.toByteArray();
        } catch (IOException e) {
            throw new ProxyProtocolException(ProxyErrorCode.INTERNAL_ERROR, "Could not encode backend command permissions.", e);
        }
    }

    public static ProxyBackendCommandPermissionsPayload decode(byte[] data) {
        try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(data))) {
            int size = input.readInt();
            if (size < 0) {
                throw new IOException("Negative backend command permission count.");
            }

            List<String> allowedCommandPaths = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                allowedCommandPaths.add(input.readUTF());
            }
            return new ProxyBackendCommandPermissionsPayload(allowedCommandPaths);
        } catch (IOException e) {
            throw new ProxyProtocolException(ProxyErrorCode.BAD_REQUEST, "Could not decode backend command permissions.", e);
        }
    }
}
