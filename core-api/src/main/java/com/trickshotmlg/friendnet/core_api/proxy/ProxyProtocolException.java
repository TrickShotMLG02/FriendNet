package com.trickshotmlg.friendnet.core_api.proxy;

public class ProxyProtocolException extends RuntimeException {

    private final ProxyErrorCode errorCode;

    public ProxyProtocolException(ProxyErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ProxyProtocolException(ProxyErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ProxyErrorCode getErrorCode() {
        return errorCode;
    }
}
