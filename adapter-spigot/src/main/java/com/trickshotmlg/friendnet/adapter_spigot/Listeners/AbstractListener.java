package com.trickshotmlg.friendnet.adapter_spigot.Listeners;

import com.trickshotmlg.friendnet.core.Logger;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class AbstractListener implements Listener {

    private final JavaPlugin plugin;

    public AbstractListener(JavaPlugin plugin) {
        this.plugin = plugin;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        Logger.debug("Registered Listener: " + this);
    }

    @Override
    public String toString() {
        return  getClass().getName() + "{" +
                /*
                "name='" + name + '\'' +
                ", permission='" + permission + '\'' +
                ", description='" + description + '\'' +
                ", usage='" + getUsageMessage() + '\'' +
                */
                '}';
    }
}
