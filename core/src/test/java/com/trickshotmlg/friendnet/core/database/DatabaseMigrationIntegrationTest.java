package com.trickshotmlg.friendnet.core.database;

import com.trickshotmlg.friendnet.core_api.enums.DatabaseType;
import com.trickshotmlg.friendnet.core_api.models.LocaleKey;
import com.trickshotmlg.friendnet.core_api.models.PlayerData;
import junit.framework.TestCase;

import java.io.File;
import java.nio.file.Files;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;

public class DatabaseMigrationIntegrationTest extends TestCase {
    private File tempDirectory;
    private DatabaseServiceImpl databaseService;

    @Override
    protected void setUp() throws Exception {
        LocaleKey.clearRegistry();
        LocaleKey defaultLocale = LocaleKey.of("en");
        LocaleKey.setDefaultLocale(defaultLocale);

        tempDirectory = Files.createTempDirectory("friendnet-db-test").toFile();
        databaseService = new DatabaseServiceImpl(new SQLiteDatabase(tempDirectory, "friendnet-test"));
    }

    @Override
    protected void tearDown() throws Exception {
        if (databaseService != null) {
            databaseService.stop();
        }

        if (tempDirectory != null && tempDirectory.exists()) {
            Files.walk(tempDirectory.toPath())
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> path.toFile().delete());
        }
    }

    public void testMigrationsCreateSchemaAndAreIdempotent() throws Exception {
        databaseService.init();
        databaseService.postInit();
        databaseService.postInit();

        assertTrue(tableExists("players"));
        assertTrue(tableExists("friendships"));
        assertTrue(tableExists("blocklist"));
        assertTrue(tableExists("friendnet_schema_migrations"));
        assertEquals(1, countAppliedMigration(1));
    }

    public void testMigratedSchemaCanPersistPlayerData() throws Exception {
        databaseService.init();
        databaseService.postInit();
        databaseService.start();

        UUID playerId = UUID.randomUUID();
        PlayerData playerData = new PlayerData(playerId);
        playerData.setLastDisplayName("MigrationTestPlayer");
        playerData.setLocale(LocaleKey.of("en"));

        databaseService.save(playerData);

        Optional<PlayerData> loaded = databaseService.find(playerId, PlayerData.class);
        assertTrue(loaded.isPresent());
        assertEquals(playerId, loaded.get().getPlayerId());
        assertEquals("MigrationTestPlayer", loaded.get().getLastDisplayName());
    }

    public void testMySqlJdbcUrlsUseStableConnectionOptions() throws Exception {
        String mysqlUrl = MySQLDatabase.buildJdbcUrl("localhost:3306", "friendnet", DatabaseType.MySQL);
        String mariaDbUrl = MySQLDatabase.buildJdbcUrl("localhost:3306", "friendnet", DatabaseType.MariaDB);

        assertTrue(mysqlUrl.startsWith("jdbc:mysql://localhost:3306/friendnet?"));
        assertTrue(mysqlUrl.contains("useSSL=false"));
        assertTrue(mysqlUrl.contains("allowPublicKeyRetrieval=false"));
        assertTrue(mysqlUrl.contains("connectTimeout=10000"));
        assertTrue(mysqlUrl.contains("socketTimeout=30000"));
        assertTrue(mysqlUrl.contains("tcpKeepAlive=true"));

        assertTrue(mariaDbUrl.startsWith("jdbc:mariadb://localhost:3306/friendnet?"));
        assertTrue(mariaDbUrl.contains("connectTimeout=10000"));
        assertTrue(mariaDbUrl.contains("socketTimeout=30000"));
        assertTrue(mariaDbUrl.contains("tcpKeepAlive=true"));
    }

    public void testMySqlJdbcUrlUsesConfiguredSslAndPublicKeyRetrieval() throws Exception {
        String mysqlUrl = MySQLDatabase.buildJdbcUrl("localhost:3306", "friendnet", DatabaseType.MySQL, true, true);
        String mariaDbUrl = MySQLDatabase.buildJdbcUrl("localhost:3306", "friendnet", DatabaseType.MariaDB, true, true);

        assertTrue(mysqlUrl.contains("useSSL=true"));
        assertTrue(mysqlUrl.contains("allowPublicKeyRetrieval=true"));
        assertTrue(mariaDbUrl.contains("useSsl=true"));
    }

    private boolean tableExists(String tableName) throws Exception {
        try (PreparedStatement ps = databaseService.getDatabase().getConnection().prepareStatement(
                "SELECT name FROM sqlite_master WHERE type = 'table' AND name = ?")) {
            ps.setString(1, tableName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
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
}
