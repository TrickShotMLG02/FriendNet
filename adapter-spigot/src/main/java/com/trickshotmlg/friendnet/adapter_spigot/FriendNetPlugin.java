package com.trickshotmlg.friendnet.adapter_spigot;

import com.trickshotmlg.friendnet.FriendServiceImpl;
import com.trickshotmlg.friendnet.adapter_spigot.Commands.ReloadCommand;
import com.trickshotmlg.friendnet.adapter_spigot.Listeners.PlayerStatusListener;
import com.trickshotmlg.friendnet.core_api.interfaces.FriendNetLogger;
import com.trickshotmlg.friendnet.core_api.interfaces.FriendService;
import com.trickshotmlg.friendnet.core_api.interfaces.Platform;
import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.java.JavaPlugin;

public final class FriendNetPlugin extends JavaPlugin {
    /*
    https://github.com/TrickShotMLG02/MinecraftPluginDevelopment/blob/ModularLobby/ModularLobby/src/main/java/com/trickshotdev/modularlobby/Spigot/ModularLobby.java
    Check this for more about configs, message files and other stuff
    */


    private FriendService friendService;
    private Platform platform;

    @Override
    public void onEnable() {
        SpigotLogger.initialize(this);

        initializePlatform();
        initializeServices();
        registerListeners();
        getLogger().info("FriendNet enabled!");
    }

    private void initializePlatform() {
        this.platform = new SpigotPlatform(this);
    }

    private void initializeServices() {
        this.friendService = new FriendServiceImpl();
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerStatusListener(friendService), this);
    }

    private void registerCommands() {

        this.getCommand("friendsreload").setExecutor((CommandExecutor) new ReloadCommand(this, "friendnet.reload"));
    }

    public FriendService getFriendService() {
        return friendService;
    }


}
