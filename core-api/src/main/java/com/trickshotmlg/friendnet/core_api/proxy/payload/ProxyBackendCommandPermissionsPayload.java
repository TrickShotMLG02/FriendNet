package com.trickshotmlg.friendnet.core_api.proxy.payload;

import java.util.List;

public record ProxyBackendCommandPermissionsPayload(List<String> allowedCommandPaths) {

    public ProxyBackendCommandPermissionsPayload {
        allowedCommandPaths = List.copyOf(allowedCommandPaths);
    }
}
