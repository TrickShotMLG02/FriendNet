package com.trickshotmlg.friendnet.adapter_spigot.Listeners;

import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
import com.trickshotmlg.friendnet.adapter_spigot.SpigotPlayer;
import com.trickshotmlg.friendnet.core.database.SQLQueries;
import com.trickshotmlg.friendnet.core_api.interfaces.services.FriendService;
import com.trickshotmlg.friendnet.core_api.interfaces.services.PlayerService;
import com.trickshotmlg.friendnet.core_api.interfaces.database.DatabaseConnection;
import com.trickshotmlg.friendnet.core_api.models.PlayerData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

/**
 * Listener class for player join/quit events.
 */
public class PlayerStatusListener implements Listener {

    private final FriendService friendService;
    private final PlayerService playerService;

    private final FriendNetPlugin plugin;

    public PlayerStatusListener(FriendService friendService, PlayerService playerService, FriendNetPlugin plugin) {
        this.friendService = friendService;
        this.playerService = playerService;
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        SpigotPlayer spigotPlayer = new SpigotPlayer(event.getPlayer());

        friendService.setOnline(spigotPlayer.getUniqueId(), true);


        try {
            DatabaseConnection conn = plugin.getDatabaseService().getDatabase().getConnection();

            try (PreparedStatement ps = conn.prepareStatement(SQLQueries.TABLE_PLAYERS_GET_PLAYER)) {
                ps.setObject(1, spigotPlayer.getUniqueId());

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        UUID id = UUID.fromString(rs.getString("player_id"));
                        Timestamp firstSeen = rs.getTimestamp("first_seen");
                        Timestamp lastSeen = rs.getTimestamp("last_seen");
                        PlayerData player = new PlayerData(id, firstSeen, lastSeen);
                        playerService.putPlayerData(player);

                        log("Player loaded: " + player);
                    } else {
                        log("Player not found in database, creating a new one");
                        PlayerData playerData = playerService.initPlayer(spigotPlayer.getUniqueId());
                    }
                }
            }
        } catch (SQLException ex) {
            System.err.println("SQLException: " + ex.getMessage());
        }

        log(spigotPlayer.getName() + " joined!");
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        SpigotPlayer spigotPlayer = new SpigotPlayer(event.getPlayer());

        friendService.setOnline(spigotPlayer.getUniqueId(), false);
        playerService.setLastSeen(spigotPlayer.getUniqueId());

        try {
            DatabaseConnection conn = plugin.getDatabaseService().getDatabase().getConnection();

            try (PreparedStatement ps = conn.prepareStatement(SQLQueries.TABLE_PLAYERS_UPSERT_LASTSEEN)) {

                PlayerData player = playerService.getPlayerData(spigotPlayer.getUniqueId());

                ps.setObject(1, player.getPlayerId());
                ps.setTimestamp(2, player.getFirstSeen());
                ps.setTimestamp(3, player.getLastSeen());

                ps.executeUpdate();
            }
        } catch (SQLException ex) {
            System.err.println("SQLException: " + ex.getMessage());
        }

        log(spigotPlayer.getName() + " quit!");
    }

    private void log(String message) {
        System.out.println("[FriendNet] " + message); // simple logging
    }
}
