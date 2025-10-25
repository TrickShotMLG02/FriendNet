package com.trickshotmlg.friendnet.core_api.models;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

public class PlayerData {

    /**
     * The UUID of the player this data belongs to.
     */
    private final UUID playerId;

    private final Timestamp firstSeen;
    private Timestamp lastSeen;

    public PlayerData(UUID playerId) {
        this.playerId = playerId;

        Instant now = Instant.now();
        ZonedDateTime zdt = ZonedDateTime.ofInstant(now, ZoneId.of("UTC"));
        firstSeen = Timestamp.from(zdt.toInstant());
        lastSeen = firstSeen;
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
                ", firstSeen=" + firstSeen +
                ", lastSeen=" + lastSeen +
                '}';
    }
}
