package com.trickshotmlg.friendnet.adapter_spigot;

import com.trickshotmlg.friendnet.core_api.interfaces.Platform;
import com.trickshotmlg.friendnet.core_api.interfaces.PlatformPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

public class SpigotPlatform implements Platform {

    private final FriendNetPlugin plugin;

    public SpigotPlatform(FriendNetPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public PlatformPlayer getPlayer(UUID uuid) {
        Player spigotPlayer = Bukkit.getPlayer(uuid);
        return (spigotPlayer != null) ? new SpigotPlayer(spigotPlayer) : null;
    }

    @Override
    public Collection<PlatformPlayer> getOnlinePlayers() {
        return Bukkit.getOnlinePlayers().stream()
                .map(SpigotPlayer::new)
                .collect(Collectors.toList());
    }

    @Override
    public void runAsync(Runnable task) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
    }

    @Override
    public void runSync(Runnable task) {
        Bukkit.getScheduler().runTask(plugin, task);
    }
}
