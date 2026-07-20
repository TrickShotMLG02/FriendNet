package com.trickshotmlg.friendnet.adapter_velocity.utils;

import com.trickshotmlg.friendnet.adapter_velocity.FriendNetVelocityPlugin;
import com.trickshotmlg.friendnet.core.application.command.CommandEvent;
import com.trickshotmlg.friendnet.core.application.command.CommandEventType;
import com.trickshotmlg.friendnet.core.application.command.CommandFeedbackUseCases;
import com.trickshotmlg.friendnet.core.application.command.CommandMessage;
import com.trickshotmlg.friendnet.core.application.command.CommandUseCaseResult;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;

import java.util.HashMap;
import java.util.Map;

public final class VelocityCommandResultRenderer {

    private VelocityCommandResultRenderer() {
    }

    public static void render(FriendNetVelocityPlugin plugin, CommandSource source, CommandUseCaseResult result) {
        for (CommandMessage message : result.messages()) {
            renderMessage(plugin, source, message);
        }

        for (CommandEvent event : result.events()) {
            renderEvent(plugin, event);
        }
    }

    public static void playersOnly(FriendNetVelocityPlugin plugin, CommandSource source) {
        render(plugin, source, CommandFeedbackUseCases.playersOnly());
    }

    public static void noPermission(FriendNetVelocityPlugin plugin, CommandSource source) {
        render(plugin, source, CommandFeedbackUseCases.noPermission());
    }

    private static void renderMessage(FriendNetVelocityPlugin plugin, CommandSource source, CommandMessage message) {
        switch (message.recipient()) {
            case SENDER -> plugin.getMessageManager().send(source, message.key(), message.placeholders());
            case PLAYER -> plugin.getServer().getPlayer(message.recipientId())
                    .ifPresent(player -> plugin.getMessageManager().send(player, message.key(), message.placeholders()));
        }
    }

    private static void renderEvent(FriendNetVelocityPlugin plugin, CommandEvent event) {
        if (event.type() == CommandEventType.FRIEND_REQUEST_SENT) {
            renderFriendRequestSent(plugin, event);
        }
    }

    private static void renderFriendRequestSent(FriendNetVelocityPlugin plugin, CommandEvent event) {
        Player target = plugin.getServer().getPlayer(event.targetId()).orElse(null);
        if (target == null) {
            return;
        }

        Map<String, Object> placeholders = new HashMap<>(event.placeholders());
        String senderName = event.placeholders().getOrDefault("sender", "").toString();
        placeholders.put("acceptRequest", actionButton(
                plugin,
                target,
                "chatButtons.acceptRequest.text",
                "chatButtons.acceptRequest.hover",
                "/friend accept " + senderName
        ));
        placeholders.put("denyRequest", actionButton(
                plugin,
                target,
                "chatButtons.denyRequest.text",
                "chatButtons.denyRequest.hover",
                "/friend deny " + senderName
        ));
        plugin.getMessageManager().send(target, "friendRequest.send.target.success", placeholders);
    }

    private static Component actionButton(
            FriendNetVelocityPlugin plugin,
            Player target,
            String textKey,
            String hoverKey,
            String command
    ) {
        Component hover = plugin.getMessageManager().component(target, hoverKey, Map.of(), false);
        return plugin.getMessageManager()
                .component(target, textKey, Map.of(), false)
                .clickEvent(ClickEvent.runCommand(command))
                .hoverEvent(HoverEvent.showText(hover));
    }
}
