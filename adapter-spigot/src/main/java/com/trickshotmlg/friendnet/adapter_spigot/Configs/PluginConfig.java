package com.trickshotmlg.friendnet.adapter_spigot.Configs;

import com.trickshotmlg.friendnet.core_api.interfaces.Config;

import java.io.File;

public class PluginConfig implements Config {
    /**
     * @return
     */
    @Override
    public File getConfigFile() {
        return null;
    }

    /**
     * @return
     */
    @Override
    public boolean LoadConfig() {
        return false;
    }

    /**
     * @return
     */
    @Override
    public boolean SaveConfig() {
        return false;
    }

    /**
     * @return
     */
    @Override
    public boolean ReloadConfig() {
        return false;
    }

    /**
     * @return
     */
    @Override
    public boolean ResetConfig() {
        return false;
    }

    /**
     * @return
     */
    @Override
    public boolean InitDefaults() {
        return false;
    }
}
