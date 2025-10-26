package com.trickshotmlg.friendnet.core_api.interfaces.services;

public interface ServiceManager {

    boolean registerService(BaseService service);
    boolean unregisterService(BaseService service);
    BaseService getService(String name);

    void init();
    void postInit();
    void start();
    void stop();
    void destroy();

}
