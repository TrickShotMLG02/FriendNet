package com.trickshotmlg.friendnet.adapter_spigot;

import com.trickshotmlg.friendnet.core_api.interfaces.PlatformPlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class SpigotPlayer implements PlatformPlayer {

    private final Player player;

    public SpigotPlayer(Player player) {
        this.player = player;
    }

    @Override
    public UUID getUniqueId() {
        return player.getUniqueId();
    }

    @Override
    public String getName() {
        return player.getName();
    }

    @Override
    public void sendMessage(String message) {
        player.sendMessage(message);
    }

    @Override
    public boolean isOnline() {
        return player.isOnline();
    }
}
