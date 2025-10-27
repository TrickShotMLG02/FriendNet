package com.trickshotmlg.friendnet.core_api.models;

import java.sql.Timestamp;
import java.util.UUID;

public class BlocklistData {
    private UUID blockerId;
    private UUID blockedId;
    private Timestamp blockedAt;
}
