package com.trickshotmlg.friendnet.core_api.models;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

public class PlayerData {

    /// The UUID of the player this data belongs to.
    private final UUID playerId;
    private String lastPlayerName = null;
    private String lastDisplayName = null;
    private String skinTexture = null;
    private String skinSignature = null;
    private String lastServerName = null;


    // --- Settings --- //
    private boolean allowFriendRequests = true;
    private boolean showOnlineStatus = true;
    private boolean autoAcceptFriends = false;
    private boolean friendRequestNotifications = true;
    private boolean friendListPublic = false;
    private LocaleKey locale = new LocaleKey("en");

    public String getLastDisplayName() {
        return lastDisplayName;
    }

    public String getLastPlayerName() {
        return lastPlayerName;
    }

    public void setLastPlayerName(String lastPlayerName) {
        this.lastPlayerName = lastPlayerName;
    }

    public void setLastDisplayName(String lastDisplayName) {
        this.lastDisplayName = lastDisplayName;
    }

    public String getSkinTexture() {
        return skinTexture;
    }

    public void setSkinTexture(String skinTexture) {
        this.skinTexture = skinTexture;
    }

    public String getSkinSignature() {
        return skinSignature;
    }

    public void setSkinSignature(String skinSignature) {
        this.skinSignature = skinSignature;
    }

    public String getLastServerName() {
        return lastServerName;
    }

    public void setLastServerName(String lastServerName) {
        this.lastServerName = lastServerName;
    }

    public boolean isAllowFriendRequests() {
        return allowFriendRequests;
    }

    public boolean isShowOnlineStatus() {
        return showOnlineStatus;
    }

    public boolean isAutoAcceptFriends() {
        return autoAcceptFriends;
    }

    public boolean isFriendRequestNotifications() {
        return friendRequestNotifications;
    }

    public boolean isFriendListPublic() {
        return friendListPublic;
    }

    public void setAllowFriendRequests(boolean allowFriendRequests) {
        this.allowFriendRequests = allowFriendRequests;
    }

    public void setShowOnlineStatus(boolean showOnlineStatus) {
        this.showOnlineStatus = showOnlineStatus;
    }

    public void setAutoAcceptFriends(boolean autoAcceptFriends) {
        this.autoAcceptFriends = autoAcceptFriends;
    }

    public void setFriendRequestNotifications(boolean friendRequestNotifications) {
        this.friendRequestNotifications = friendRequestNotifications;
    }

    public void setFriendListPublic(boolean friendListPublic) {
        this.friendListPublic = friendListPublic;
    }

    public LocaleKey getLocale() {
        return locale;
    }

    public void setLocale(LocaleKey locale) {
        this.locale = locale;
    }

    /// The timestamp when the player first joined the server
    private final Timestamp firstSeen;

    /// The timestamp when the player status was last time online
    private Timestamp lastSeen;

    public PlayerData(UUID playerId) {
        this.playerId = playerId;

        Instant now = Instant.now();
        ZonedDateTime zdt = ZonedDateTime.ofInstant(now, ZoneId.of("UTC"));
        firstSeen = Timestamp.from(zdt.toInstant());
        lastSeen = firstSeen;
    }

    public PlayerData(UUID playerId, Timestamp firstSeen, Timestamp lastSeen) {
        this.playerId = playerId;
        this.firstSeen = firstSeen;
        this.lastSeen = lastSeen;
    }

    public void setLastSeen() {
        Instant now = Instant.now();
        ZonedDateTime zdt = ZonedDateTime.ofInstant(now, ZoneId.of("UTC"));
        lastSeen = Timestamp.from(zdt.toInstant());
    }

    public void setLastSeen(Timestamp timestamp) {
        lastSeen = timestamp;
    }

    /**
     * Returns the UUID of the player.
     *
     * @return the player's UUID
     */
    public UUID getPlayerId() {
        return playerId;
    }

    public Timestamp getFirstSeen() {
        return firstSeen;
    }

    public Timestamp getLastSeen() {
        return lastSeen;
    }

    @Override
    public String toString() {
        return "PlayerData{" +
                "playerId=" + playerId +
                ", lastPlayerName='" + lastPlayerName + '\'' +
                ", lastDisplayName='" + lastDisplayName + '\'' +
                ", hasSkinTexture=" + (skinTexture != null && !skinTexture.isBlank()) +
                ", lastServerName='" + lastServerName + '\'' +
                ", allow_friend_requests=" + allowFriendRequests +
                ", show_online_status=" + showOnlineStatus +
                ", auto_accept_friends=" + autoAcceptFriends +
                ", friend_request_notifications=" + friendRequestNotifications +
                ", friend_list_public=" + friendListPublic +
                ", locale=" + (locale != null ? locale.getCode() : null) +
                ", firstSeen=" + firstSeen +
                ", lastSeen=" + lastSeen +
                '}';
    }
}
