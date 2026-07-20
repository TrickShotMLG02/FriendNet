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
            }
            return bytes.toByteArray();
        } catch (IOException e) {
            throw new ProxyProtocolException(ProxyErrorCode.INTERNAL_ERROR, "Could not encode friend list payload.", e);
        }
    }

    public static ProxyFriendListViewPayload decode(byte[] data) {
        try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(data))) {
            return new ProxyFriendListViewPayload(readEntries(input), readEntries(input), readEntries(input), readEntries(input));
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
            output.writeBoolean(entry.online());
            output.writeUTF(entry.currentServerName());
            output.writeBoolean(entry.favourite());
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
            boolean online = input.readBoolean();
            String currentServerName = input.readUTF();
            boolean favourite = input.readBoolean();
            entries.add(new ProxyFriendEntry(playerId, displayName, online, currentServerName, favourite));
        }
        return entries;
    }
}
