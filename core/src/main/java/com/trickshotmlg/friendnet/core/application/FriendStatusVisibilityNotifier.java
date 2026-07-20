package com.trickshotmlg.friendnet.core.application;

import java.util.UUID;

public interface FriendStatusVisibilityNotifier {
    FriendStatusVisibilityNotifier NONE = new FriendStatusVisibilityNotifier() {
        @Override
        public void notifyOnline(UUID playerId) {
        }

        @Override
        public void notifyOffline(UUID playerId) {
        }
    };

    void notifyOnline(UUID playerId);

    void notifyOffline(UUID playerId);
}
