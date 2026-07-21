package com.trickshotmlg.friendnet.core_api.models;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

public class FavouriteData {
    private final UUID favouriterId;
    private final UUID favouriteId;
    private final Timestamp createdAt;

    public FavouriteData(UUID favouriterId, UUID favouriteId) {
        this(favouriterId, favouriteId, Timestamp.from(ZonedDateTime.ofInstant(Instant.now(), ZoneId.of("UTC")).toInstant()));
    }

    public FavouriteData(UUID favouriterId, UUID favouriteId, Timestamp createdAt) {
        this.favouriterId = favouriterId;
        this.favouriteId = favouriteId;
        this.createdAt = createdAt;
    }

    public UUID getFavouriterId() {
        return favouriterId;
    }

    public UUID getFavouriteId() {
        return favouriteId;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }
}
