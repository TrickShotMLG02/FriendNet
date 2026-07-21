package com.trickshotmlg.friendnet.core_api.proxy.payload;

import com.trickshotmlg.friendnet.core_api.proxy.ProxyErrorCode;
import com.trickshotmlg.friendnet.core_api.proxy.ProxyProtocolException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public final class ProxyOpenBackendGuiPayloadCodec {

    private ProxyOpenBackendGuiPayloadCodec() {
    }

    public static byte[] encode(ProxyBackendGuiType guiType) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            DataOutputStream output = new DataOutputStream(bytes);
            output.writeUTF(guiType.name());
            output.flush();
            return bytes.toByteArray();
        } catch (IOException e) {
            throw new ProxyProtocolException(ProxyErrorCode.BAD_REQUEST, "Could not encode backend GUI request.", e);
        }
    }

    public static ProxyBackendGuiType decode(byte[] payload) {
        try {
            DataInputStream input = new DataInputStream(new ByteArrayInputStream(payload));
            return ProxyBackendGuiType.valueOf(input.readUTF());
        } catch (IOException | IllegalArgumentException e) {
            throw new ProxyProtocolException(ProxyErrorCode.BAD_REQUEST, "Could not decode backend GUI request.", e);
        }
    }
}
