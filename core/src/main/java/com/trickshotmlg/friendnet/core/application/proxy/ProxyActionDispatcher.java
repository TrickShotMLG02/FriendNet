package com.trickshotmlg.friendnet.core.application.proxy;

import com.trickshotmlg.friendnet.core.application.KnownPlayerLookup;
import com.trickshotmlg.friendnet.core.application.PlayerSettingsApplicationService;
import com.trickshotmlg.friendnet.core.application.command.CommandFeedbackUseCases;
import com.trickshotmlg.friendnet.core.application.command.CommandMessage;
import com.trickshotmlg.friendnet.core.application.command.CommandUseCaseResult;
import com.trickshotmlg.friendnet.core.application.command.FriendCommandUseCases;
import com.trickshotmlg.friendnet.core_api.models.LocaleKey;
import com.trickshotmlg.friendnet.core_api.proxy.ProxyErrorCode;
import com.trickshotmlg.friendnet.core_api.proxy.ProxyProtocolException;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyActionRequestPayload;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyActionResponsePayload;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyFriendListViewPayload;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyMessagePayload;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyMessageRecipient;

import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ProxyActionDispatcher {

    private final FriendCommandUseCases friendCommandUseCases;
    private final PlayerSettingsApplicationService playerSettingsService;
    private final KnownPlayerLookup knownPlayerLookup;
    private final Function<UUID, ProxyFriendListViewPayload> friendListViewFactory;

    public ProxyActionDispatcher(
            FriendCommandUseCases friendCommandUseCases,
            PlayerSettingsApplicationService playerSettingsService,
            KnownPlayerLookup knownPlayerLookup,
            Function<UUID, ProxyFriendListViewPayload> friendListViewFactory
    ) {
        this.friendCommandUseCases = friendCommandUseCases;
        this.playerSettingsService = playerSettingsService;
        this.knownPlayerLookup = knownPlayerLookup;
        this.friendListViewFactory = friendListViewFactory;
    }

    public ProxyActionResponsePayload dispatch(UUID actorId, String actorName, ProxyActionRequestPayload request) {
        if (actorId == null) {
            throw new ProxyProtocolException(ProxyErrorCode.PLAYER_REQUIRED, "Proxy action requires an actor player.");
        }

        CommandUseCaseResult result = switch (request.actionType()) {
            case ADD_FRIEND -> addFriend(actorId, actorName, request);
            case REMOVE_FRIEND -> friendCommandUseCases.removeFriend(actorId, requiredTargetId(request), targetName(request));
            case ACCEPT_REQUEST -> friendCommandUseCases.acceptRequest(actorId, actorName, requiredTargetId(request), targetName(request));
            case ACCEPT_ALL_REQUESTS -> friendCommandUseCases.acceptAllRequests(actorId, actorName);
            case DENY_REQUEST -> friendCommandUseCases.denyRequest(actorId, requiredTargetId(request), targetName(request));
            case DENY_ALL_REQUESTS -> friendCommandUseCases.denyAllRequests(actorId);
            case CANCEL_REQUEST -> friendCommandUseCases.cancelRequest(actorId, requiredTargetId(request), targetName(request));
            case CANCEL_ALL_REQUESTS -> friendCommandUseCases.cancelAllRequests(actorId);
            case BLOCK_PLAYER -> friendCommandUseCases.blockPlayer(actorId, requiredTargetId(request), targetName(request));
            case UNBLOCK_PLAYER -> friendCommandUseCases.unblockPlayer(actorId, requiredTargetId(request), targetName(request));
            case CLEAR_BLOCKLIST -> friendCommandUseCases.clearBlocklist(actorId);
            case SET_FAVOURITE -> friendCommandUseCases.setFavourite(actorId, requiredTargetId(request), targetName(request), request.enabled());
            case SET_ALLOW_FRIEND_REQUESTS -> setPlayerSetting(actorId, request, playerSettingsService::setAllowFriendRequests, "playerSettings.allowFriendRequests");
            case SET_SHOW_ONLINE_STATUS -> setPlayerSetting(actorId, request, playerSettingsService::setShowOnlineStatus, "playerSettings.showOnlineStatus");
            case SET_AUTO_ACCEPT_FRIENDS -> setPlayerSetting(actorId, request, playerSettingsService::setAutoAcceptFriends, "playerSettings.autoAcceptFriends");
            case SET_FRIEND_REQUEST_NOTIFICATIONS -> setPlayerSetting(actorId, request, playerSettingsService::setFriendRequestNotifications, "playerSettings.friendRequestNotifications");
            case SET_FRIEND_LIST_PUBLIC -> setPlayerSetting(actorId, request, playerSettingsService::setFriendListPublic, "playerSettings.friendListPublic");
            case SET_LOCALE -> setLocale(actorId, request);
        };

        ProxyFriendListViewPayload refreshedView = request.refreshFriendList()
                ? friendListViewFactory.apply(actorId)
                : null;
        return new ProxyActionResponsePayload(
                result.success(),
                result.messages().stream().map(this::toProxyMessage).toList(),
                refreshedView
        );
    }

    private CommandUseCaseResult setPlayerSetting(
            UUID actorId,
            ProxyActionRequestPayload request,
            BooleanSettingUpdater updater,
            String messageKeyPrefix
    ) {
        boolean success = updater.update(actorId, request.enabled());
        return CommandUseCaseResult.builder(success)
                .message(CommandMessage.sender(messageKeyPrefix + (request.enabled() ? ".enabled" : ".disabled")))
                .build();
    }

    private CommandUseCaseResult setLocale(UUID actorId, ProxyActionRequestPayload request) {
        String localeCode = request.targetName();
        if (localeCode.isBlank()) {
            return CommandUseCaseResult.builder(false)
                    .message(CommandMessage.sender("playerSettings.locale.changed"))
                    .build();
        }

        boolean success = playerSettingsService.setLocale(actorId, LocaleKey.getOrFallback(localeCode));
        return CommandUseCaseResult.builder(success)
                .message(CommandMessage.sender("playerSettings.locale.changed"))
                .build();
    }

    private CommandUseCaseResult addFriend(UUID actorId, String actorName, ProxyActionRequestPayload request) {
        String targetName = request.targetName();
        if (targetName.isBlank()) {
            return CommandFeedbackUseCases.playerNotFound();
        }

        return knownPlayerLookup.resolve(targetName)
                .map(target -> friendCommandUseCases.sendFriendRequest(actorId, actorName, target))
                .orElseGet(CommandFeedbackUseCases::playerNotFound);
    }

    private UUID requiredTargetId(ProxyActionRequestPayload request) {
        if (request.targetId() == null) {
            throw new ProxyProtocolException(ProxyErrorCode.BAD_REQUEST, "Proxy action requires a target player.");
        }
        return request.targetId();
    }

    private String targetName(ProxyActionRequestPayload request) {
        if (!request.targetName().isBlank()) {
            return request.targetName();
        }
        return knownPlayerLookup.displayName(requiredTargetId(request));
    }

    private ProxyMessagePayload toProxyMessage(CommandMessage message) {
        ProxyMessageRecipient recipient = switch (message.recipient()) {
            case SENDER -> ProxyMessageRecipient.SENDER;
            case PLAYER -> ProxyMessageRecipient.PLAYER;
        };

        Map<String, String> placeholders = message.placeholders().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> String.valueOf(entry.getValue())));
        return new ProxyMessagePayload(recipient, message.recipientId(), message.key(), placeholders);
    }

    @FunctionalInterface
    private interface BooleanSettingUpdater {
        boolean update(UUID playerId, boolean enabled);
    }
}
