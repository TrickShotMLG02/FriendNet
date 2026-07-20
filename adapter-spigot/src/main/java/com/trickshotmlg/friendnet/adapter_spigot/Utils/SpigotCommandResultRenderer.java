package com.trickshotmlg.friendnet.adapter_spigot.Utils;

import com.trickshotmlg.friendnet.core.application.command.CommandEvent;
import com.trickshotmlg.friendnet.core.application.command.CommandEventType;
import com.trickshotmlg.friendnet.core.application.command.CommandFeedbackUseCases;
import com.trickshotmlg.friendnet.core.application.command.CommandMessage;
import com.trickshotmlg.friendnet.core.application.command.CommandUseCaseResult;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public final class SpigotCommandResultRenderer {

    private SpigotCommandResultRenderer() {
    }

    public static void render(CommandSender sender, CommandUseCaseResult result) {
        for (CommandMessage message : result.messages()) {
            renderMessage(sender, message);
        }

        for (CommandEvent event : result.events()) {
            renderEvent(event);
        }
    }

    public static void playersOnly(CommandSender sender) {
        render(sender, CommandFeedbackUseCases.playersOnly());
    }

    public static void usage(CommandSender sender, String usage) {
        render(sender, CommandFeedbackUseCases.usage(usage));
    }

    public static void playerNotFound(CommandSender sender) {
        render(sender, CommandFeedbackUseCases.playerNotFound());
    }

    public static void noPermission(CommandSender sender) {
        render(sender, CommandFeedbackUseCases.noPermission());
    }

    private static void renderMessage(CommandSender sender, CommandMessage message) {
        switch (message.recipient()) {
            case SENDER -> MessageManager.send(sender, message.key(), message.placeholders());
            case PLAYER -> MessageManager.send(message.recipientId(), message.key(), message.placeholders());
        }
    }

    private static void renderEvent(CommandEvent event) {
        if (event.type() == CommandEventType.FRIEND_REQUEST_SENT) {
            renderFriendRequestSent(event);
        }
    }

    private static void renderFriendRequestSent(CommandEvent event) {
        Player target = Bukkit.getPlayer(event.targetId());
        if (target == null) {
            return;
        }

        Object senderName = event.placeholders().getOrDefault("sender", "");
        TextComponent accept = MessageManager.createButton(
                event.targetId(),
                "chatButtons.acceptRequest.text",
                Map.of(),
                "chatButtons.acceptRequest.hover",
                Map.of(),
                ClickEvent.Action.RUN_COMMAND,
                "/friend accept " + senderName
        );
        TextComponent deny = MessageManager.createButton(
                event.targetId(),
                "chatButtons.denyRequest.text",
                Map.of(),
                "chatButtons.denyRequest.hover",
                Map.of(),
                ClickEvent.Action.RUN_COMMAND,
                "/friend deny " + senderName
        );

        Map<String, Object> placeholders = new HashMap<>(event.placeholders());
        placeholders.put("acceptRequest", accept);
        placeholders.put("denyRequest", deny);
        MessageManager.send(target, "friendRequest.send.target.success", placeholders);
    }
}
