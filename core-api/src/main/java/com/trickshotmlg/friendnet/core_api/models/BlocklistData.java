package com.trickshotmlg.friendnet.core_api.models;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

public class BlocklistData {
    private final UUID blockerId;
    private final UUID blockedId;
    private final Timestamp blockedAt;

    public BlocklistData(UUID blockerId, UUID blockedId) {
        this(blockerId, blockedId, Timestamp.from(ZonedDateTime.ofInstant(Instant.now(), ZoneId.of("UTC")).toInstant()));
    }

    public BlocklistData(UUID blockerId, UUID blockedId, Timestamp blockedAt) {
        this.blockerId = blockerId;
        this.blockedId = blockedId;
        this.blockedAt = blockedAt;
    }

    public UUID getBlockerId() {
        return blockerId;
    }

    public UUID getBlockedId() {
        return blockedId;
    }

    public Timestamp getBlockedAt() {
        return blockedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BlocklistData other)) return false;
        return blockerId.equals(other.blockerId) && blockedId.equals(other.blockedId);
    }

    @Override
    public int hashCode() {
        return 31 * blockerId.hashCode() + blockedId.hashCode();
    }

    @Override
    public String toString() {
        return "BlocklistData{" +
                "blockerId=" + blockerId +
                ", blockedId=" + blockedId +
                ", blockedAt=" + blockedAt +
                '}';
    }
}
