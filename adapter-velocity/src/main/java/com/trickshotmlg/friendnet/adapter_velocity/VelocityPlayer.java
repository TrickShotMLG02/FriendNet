package com.trickshotmlg.friendnet.adapter_velocity;

import com.trickshotmlg.friendnet.core_api.interfaces.PlatformPlayer;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;

import java.util.UUID;

public class VelocityPlayer implements PlatformPlayer {

    private final Player player;

    public VelocityPlayer(Player player) {
        this.player = player;
    }

    @Override
    public UUID getUniqueId() {
        return player.getUniqueId();
    }

    @Override
    public String getName() {
        return player.getUsername();
    }

    @Override
    public String getDisplayName() {
        return player.getUsername();
    }

    @Override
    public void sendMessage(String message) {
        player.sendMessage(Component.text(message));
    }

    @Override
    public boolean isOnline() {
        return player.isActive();
    }

    @Override
    public boolean hasPermission(String permission) {
        return player.hasPermission(permission);
    }

    @Override
    public String toString() {
        return toStringRepresentation();
    }
}
