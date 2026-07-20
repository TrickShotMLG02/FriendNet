package com.trickshotmlg.friendnet.core_api.proxy;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class ProxyProtocolHmac {

    private static final String ALGORITHM = "HmacSHA256";

    private ProxyProtocolHmac() {
    }

    public static byte[] sign(byte[] data, String token) {
        if (token == null || token.isBlank()) {
            throw new ProxyProtocolException(ProxyErrorCode.AUTHENTICATION_FAILED, "Connection token is blank.");
        }

        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(token.getBytes(StandardCharsets.UTF_8), ALGORITHM));
            return mac.doFinal(data);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new ProxyProtocolException(ProxyErrorCode.INTERNAL_ERROR, "Could not create protocol signature.", e);
        }
    }

    public static boolean verify(byte[] data, byte[] signature, String token) {
        if (signature == null || signature.length == 0) {
            return false;
        }

        return MessageDigest.isEqual(sign(data, token), signature);
    }
}
