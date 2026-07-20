package com.trickshotmlg.friendnet.core.database;

import com.trickshotmlg.friendnet.core_api.enums.DatabaseType;
import com.trickshotmlg.friendnet.core_api.interfaces.database.Database;
import com.trickshotmlg.friendnet.core_api.models.LocaleKey;
import com.trickshotmlg.friendnet.core_api.models.PlayerData;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.MySQLContainer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

public class MySqlMigrationIntegrationTest {
    private DatabaseServiceImpl databaseService;

    @Before
    public void setUp() {
        LocaleKey.clearRegistry();
        LocaleKey defaultLocale = LocaleKey.of("en");
        LocaleKey.setDefaultLocale(defaultLocale);
    }

    @After
    public void tearDown() {
        if (databaseService != null) {
            databaseService.stop();
        }
    }

    @Test
    public void testMySqlMigrationsAgainstRealDatabase() throws Exception {
        assumeTrue("Docker is required for MySQL integration tests", isDockerAvailable());

        try (MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4")) {
            mysql.start();
            runMigrationAndPersistenceTest(new MySQLDatabase(
                    mysql.getHost() + ":" + mysql.getFirstMappedPort(),
                    mysql.getDatabaseName(),
                    mysql.getUsername(),
                    mysql.getPassword(),
                    DatabaseType.MySQL
            ));
        }
    }

    @Test
    public void testMariaDbMigrationsAgainstRealDatabase() throws Exception {
        assumeTrue("Docker is required for MariaDB integration tests", isDockerAvailable());

        try (MariaDBContainer<?> mariaDb = new MariaDBContainer<>("mariadb:11.4")) {
            mariaDb.start();
            runMigrationAndPersistenceTest(new MySQLDatabase(
                    mariaDb.getHost() + ":" + mariaDb.getFirstMappedPort(),
                    mariaDb.getDatabaseName(),
                    mariaDb.getUsername(),
                    mariaDb.getPassword(),
                    DatabaseType.MariaDB
            ));
        }
    }

    private void runMigrationAndPersistenceTest(Database database) throws Exception {
        databaseService = new DatabaseServiceImpl(database);
        databaseService.init();
        databaseService.postInit();
        databaseService.postInit();
        databaseService.start();

        assertCanQueryTable("players");
        assertCanQueryTable("friendships");
        assertCanQueryTable("blocklist");
        assertEquals(1, countAppliedMigration(1));

        UUID playerId = UUID.randomUUID();
        PlayerData playerData = new PlayerData(playerId);
        playerData.setLastDisplayName("ContainerMigrationPlayer");
        playerData.setLocale(LocaleKey.of("en"));

        databaseService.save(playerData);

        Optional<PlayerData> loaded = databaseService.find(playerId, PlayerData.class);
        assertTrue(loaded.isPresent());
        assertEquals(playerId, loaded.get().getPlayerId());
        assertEquals("ContainerMigrationPlayer", loaded.get().getLastDisplayName());
    }

    private void assertCanQueryTable(String tableName) throws Exception {
        try (PreparedStatement ps = databaseService.getDatabase().getConnection().prepareStatement(
                "SELECT COUNT(*) FROM " + tableName);
             ResultSet rs = ps.executeQuery()) {
            assertTrue(rs.next());
        }
    }

    private int countAppliedMigration(int version) throws Exception {
        try (PreparedStatement ps = databaseService.getDatabase().getConnection().prepareStatement(
                "SELECT COUNT(*) FROM friendnet_schema_migrations WHERE version = ?")) {
            ps.setInt(1, version);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                return rs.getInt(1);
            }
        }
    }

    private boolean isDockerAvailable() {
        try {
            return DockerClientFactory.instance().isDockerAvailable();
        } catch (Throwable ignored) {
            return false;
        }
    }
}
