package com.trickshotmlg.friendnet.core_api.interfaces.services;

import com.trickshotmlg.friendnet.core_api.enums.ServiceState;

public interface BaseService {
    void init();
    void postInit();
    void start();
    void stop();
    void destroy();

    String getName();
    ServiceState getState();
    boolean isRunning();
}
