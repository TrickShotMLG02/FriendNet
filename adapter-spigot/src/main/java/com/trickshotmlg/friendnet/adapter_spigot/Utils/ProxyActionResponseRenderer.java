package com.trickshotmlg.friendnet.adapter_spigot.Utils;

import com.trickshotmlg.friendnet.core.application.command.CommandMessage;
import com.trickshotmlg.friendnet.core.application.command.CommandMessageRecipient;
import com.trickshotmlg.friendnet.core.application.command.CommandUseCaseResult;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyActionResponsePayload;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyMessagePayload;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyMessageRecipient;
import org.bukkit.command.CommandSender;

import java.util.Map;

public final class ProxyActionResponseRenderer {

    private ProxyActionResponseRenderer() {
    }

    public static void render(CommandSender sender, ProxyActionResponsePayload response) {
        CommandUseCaseResult.Builder result = CommandUseCaseResult.builder(response.success());
        for (ProxyMessagePayload message : response.messages()) {
            result.message(new CommandMessage(
                    message.recipient() == ProxyMessageRecipient.PLAYER ? CommandMessageRecipient.PLAYER : CommandMessageRecipient.SENDER,
                    message.recipientId(),
                    message.key(),
                    Map.copyOf(message.placeholders())
            ));
        }
        SpigotCommandResultRenderer.render(sender, result.build());
    }
}
