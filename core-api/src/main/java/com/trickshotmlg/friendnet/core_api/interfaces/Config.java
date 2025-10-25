package com.trickshotmlg.friendnet.core_api.interfaces;

import java.io.File;

public interface Config {

    File getConfigFile();

    boolean LoadConfig();
    boolean SaveConfig();
    boolean ReloadConfig();

    boolean ResetConfig();
    boolean InitDefaults();

}
