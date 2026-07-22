package com.trickshotmlg.friendnet.core_api.proxy.payload;

import com.trickshotmlg.friendnet.core_api.proxy.ProxyErrorCode;
import com.trickshotmlg.friendnet.core_api.proxy.ProxyProtocolException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ProxyFriendListViewPayloadCodec {

    private ProxyFriendListViewPayloadCodec() {
    }

    public static byte[] encode(ProxyFriendListViewPayload payload) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (DataOutputStream output = new DataOutputStream(bytes)) {
                writeEntries(output, payload.friends());
                writeEntries(output, payload.pendingRequests());
                writeEntries(output, payload.sentRequests());
                writeEntries(output, payload.blockedPlayers());
                output.writeBoolean(payload.allowFriendRequests());
                output.writeBoolean(payload.showOnlineStatus());
                output.writeBoolean(payload.autoAcceptFriends());
                output.writeBoolean(payload.friendRequestNotifications());
                output.writeBoolean(payload.friendListPublic());
                output.writeUTF(payload.localeCode());
                output.writeLong(payload.viewerFirstSeenMillis());
                output.writeBoolean(payload.viewedPlayerId() != null);
                if (payload.viewedPlayerId() != null) {
                    output.writeLong(payload.viewedPlayerId().getMostSignificantBits());
                    output.writeLong(payload.viewedPlayerId().getLeastSignificantBits());
                }
                output.writeUTF(payload.viewedDisplayName());
                output.writeLong(payload.viewedFirstSeenMillis());
                output.writeBoolean(payload.viewedFriendListPublic());
            }
            return bytes.toByteArray();
        } catch (IOException e) {
            throw new ProxyProtocolException(ProxyErrorCode.INTERNAL_ERROR, "Could not encode friend list payload.", e);
        }
    }

    public static ProxyFriendListViewPayload decode(byte[] data) {
        try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(data))) {
            List<ProxyFriendEntry> friends = readEntries(input);
            List<ProxyFriendEntry> pendingRequests = readEntries(input);
            List<ProxyFriendEntry> sentRequests = readEntries(input);
            List<ProxyFriendEntry> blockedPlayers = readEntries(input);
            boolean allowFriendRequests = input.readBoolean();
            boolean showOnlineStatus = input.readBoolean();
            boolean autoAcceptFriends = input.readBoolean();
            boolean friendRequestNotifications = input.readBoolean();
            boolean friendListPublic = input.readBoolean();
            String localeCode = input.readUTF();
            long viewerFirstSeenMillis = input.readLong();
            UUID viewedPlayerId = null;
            String viewedDisplayName = "";
            long viewedFirstSeenMillis = -1L;
            boolean viewedFriendListPublic = true;
            if (input.available() > 0 && input.readBoolean()) {
                viewedPlayerId = new UUID(input.readLong(), input.readLong());
            }
            if (input.available() > 0) {
                viewedDisplayName = input.readUTF();
            }
            if (input.available() > 0) {
                viewedFirstSeenMillis = input.readLong();
            }
            if (input.available() > 0) {
                viewedFriendListPublic = input.readBoolean();
            }
            return new ProxyFriendListViewPayload(
                    friends,
                    pendingRequests,
                    sentRequests,
                    blockedPlayers,
                    allowFriendRequests,
                    showOnlineStatus,
                    autoAcceptFriends,
                    friendRequestNotifications,
                    friendListPublic,
                    localeCode,
                    viewerFirstSeenMillis,
                    viewedPlayerId,
                    viewedDisplayName,
                    viewedFirstSeenMillis,
                    viewedFriendListPublic
            );
        } catch (IOException | RuntimeException e) {
            throw new ProxyProtocolException(ProxyErrorCode.BAD_REQUEST, "Could not decode friend list payload.", e);
        }
    }

    private static void writeEntries(DataOutputStream output, List<ProxyFriendEntry> entries) throws IOException {
        output.writeInt(entries.size());
        for (ProxyFriendEntry entry : entries) {
            output.writeLong(entry.playerId().getMostSignificantBits());
            output.writeLong(entry.playerId().getLeastSignificantBits());
            output.writeUTF(entry.displayName());
            output.writeUTF(entry.skinTexture());
            output.writeUTF(entry.skinSignature());
            output.writeBoolean(entry.online());
            output.writeUTF(entry.currentServerName());
            output.writeBoolean(entry.favourite());
            output.writeLong(entry.requestSentTimeMillis());
            output.writeLong(entry.friendSinceMillis());
            output.writeLong(entry.blockedAtMillis());
            output.writeLong(entry.lastSeenMillis());
            output.writeBoolean(entry.friendOfViewer());
            output.writeBoolean(entry.requestSentByViewer());
            output.writeBoolean(entry.requestReceivedByViewer());
        }
    }

    private static List<ProxyFriendEntry> readEntries(DataInputStream input) throws IOException {
        int size = input.readInt();
        if (size < 0) {
            throw new IOException("Negative friend entry count.");
        }

        List<ProxyFriendEntry> entries = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            UUID playerId = new UUID(input.readLong(), input.readLong());
            String displayName = input.readUTF();
            String skinTexture = input.readUTF();
            String skinSignature = input.readUTF();
            boolean online = input.readBoolean();
            String currentServerName = input.readUTF();
            boolean favourite = input.readBoolean();
            long requestSentTimeMillis = input.readLong();
            long friendSinceMillis = input.readLong();
            long blockedAtMillis = input.readLong();
            long lastSeenMillis = input.readLong();
            boolean friendOfViewer = false;
            boolean requestSentByViewer = false;
            boolean requestReceivedByViewer = false;
            if (input.available() > 0) {
                friendOfViewer = input.readBoolean();
                requestSentByViewer = input.readBoolean();
                requestReceivedByViewer = input.readBoolean();
            }
            entries.add(new ProxyFriendEntry(
                    playerId,
                    displayName,
                    skinTexture,
                    skinSignature,
                    online,
                    currentServerName,
                    favourite,
                    requestSentTimeMillis,
                    friendSinceMillis,
                    blockedAtMillis,
                    lastSeenMillis,
                    friendOfViewer,
                    requestSentByViewer,
                    requestReceivedByViewer
            ));
        }
        return entries;
    }
}
