package com.trickshotmlg.friendnet.adapter_velocity;

import com.trickshotmlg.friendnet.core_api.interfaces.Platform;
import com.trickshotmlg.friendnet.core_api.interfaces.PlatformPlayer;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

public class VelocityPlatform implements Platform {

    private final ProxyServer server;
    private final FriendNetVelocityPlugin plugin;

    public VelocityPlatform(ProxyServer server, FriendNetVelocityPlugin plugin) {
        this.server = server;
        this.plugin = plugin;
    }

    @Override
    public PlatformPlayer getPlayer(UUID uuid) {
        return server.getPlayer(uuid)
                .map(VelocityPlayer::new)
                .orElse(null);
    }

    @Override
    public Collection<PlatformPlayer> getOnlinePlayers() {
        return server.getAllPlayers().stream()
                .map(VelocityPlayer::new)
                .collect(Collectors.toList());
    }

    @Override
    public void runAsync(Runnable task) {
        server.getScheduler().buildTask(plugin, task).schedule();
    }

    @Override
    public void runSync(Runnable task) {
        task.run();
    }
}
