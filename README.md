# FriendNet

FriendNet is a Minecraft friendship and social-system plugin currently implemented for Spigot/Paper servers. It provides friend lists, friend requests, favourites, blocklists, player privacy settings, localized GUIs, and persistent player data backed by SQLite, MySQL, or MariaDB.

The project is structured as a small multi-module Maven build:

- `core-api`: shared interfaces, enums, and data models.
- `core`: service implementations, permissions, database access, and migrations.
- `adapter-spigot`: Spigot/Paper plugin adapter, commands, listeners, GUIs, localization, and development-server tooling.

## Current Status

FriendNet currently runs in **Standalone** mode, where the Spigot/Paper server owns commands, caches, and database writes.

The intended long-term architecture is proxy-first:

- A proxy, such as Velocity or BungeeCord, should eventually become the source of truth for friendships.
- Spigot servers should keep only local caches and GUI/command adapters.
- Database writes should be centralized to avoid cross-server conflicts.

That proxy mode is not implemented yet. The current Spigot implementation is usable for local development and standalone servers.

## Features

- `/friend` command tree for managing friendships.
- `/friends` alias for opening the friend list.
- Friend requests with accept, deny, cancel, accept-all, and deny-all flows.
- Sent request UI with cancel support.
- Friend detail UI with remove, favourite toggle, block, and message shortcut.
- Favourites list with filtering.
- Blocklist UI and block/unblock commands.
- Personal settings UI:
  - allow friend requests
  - show/hide online status
  - auto-accept friend requests
  - friend request notifications
  - public/private friend list setting
  - language selection
- Localized GUI and chat messages.
- Custom skull textures for GUI icons.
- SQLite, MySQL, and MariaDB support.
- Versioned database migrations.
- Dev server Maven profiles for Paper and LuckPerms.

## Requirements

- Java 17
- Maven 3.x
- Paper/Spigot API 1.21
- For MySQL/MariaDB: a reachable database server
- For container integration tests: Docker

## Building

From the repository root:

```powershell
mvn -pl adapter-spigot -am package
```

The shaded plugin jar is created at:

```text
adapter-spigot/target/friendnet-spigot.jar
```

Install it by copying that jar into your server's `plugins` directory.

## Development Server

The Spigot adapter contains Maven profiles to prepare and run a local Paper development server. It also downloads LuckPerms automatically unless disabled.

Prepare the dev server and plugin:

```powershell
mvn -P dev-server -pl adapter-spigot -am -DskipTests verify
```

Run the dev server with debugger enabled on port `5005`:

```powershell
mvn -P dev-server,run-dev-server -pl adapter-spigot -am -DskipTests install
```

Useful Maven properties:

```text
-Dfriendnet.dev.minecraft.version=1.21
-Dfriendnet.dev.debug.port=5005
-Dfriendnet.dev.debug.suspend=n
-Dfriendnet.dev.luckperms.skip=true
-Dfriendnet.dev.server.download.force=true
```

The local dev server is created under:

```text
dev-server/
```

That directory is intentionally ignored by Git.

## Configuration

Default config:

```yaml
Mode: Standalone
DatabaseType: SQLite
PlayerDataFlushIntervalSeconds: 30

SupportedLocales:
  - en_US
  - de

DefaultLocale: "en_US"

MySQL:
  host: localhost:3306
  dbName: friendnet
  username: friendnet
  password: friendnet

SQLite:
  dbName: friendnet
```

Supported database types:

- `SQLite`
- `MySQL`
- `MariaDB`

`DatabaseType` parsing is case-insensitive. Invalid database types fail plugin startup with a detailed error and disable the plugin.

### Reload Behavior

`/friend reload` reloads config, locales, and messages.

It does **not** hot-swap the active database connection. Changes to these values require a server/plugin restart:

- `DatabaseType`
- `MySQL.host`
- `MySQL.dbName`
- `MySQL.username`
- `MySQL.password`
- `SQLite.dbName`
- `Mode`

This is intentional. Replacing a live database connection safely would require flushing dirty data, stopping queues, closing the current connection, migrating the new database, swapping services, and rolling back on failure.

## Database Migrations

Database schema is managed by explicit versioned migrations in:

```text
core/src/main/java/com/trickshotmlg/friendnet/core/database/DatabaseMigrations.java
```

The migration runner stores applied versions in:

```text
friendnet_schema_migrations
```

Rules:

- Never edit an old migration after it has been released or used.
- Add a new `DatabaseMigration` with the next version number.
- Keep migration SQL compatible with SQLite, MySQL, and MariaDB when possible.
- If a change needs database-specific SQL, extend the migration system deliberately instead of hiding branching logic in random services.

Example:

```java
new DatabaseMigration(
        2,
        "Add player nickname",
        List.of("ALTER TABLE players ADD COLUMN nickname VARCHAR(64)")
)
```

When adding or changing persisted fields, update all relevant pieces:

- model in `core-api`
- migration in `core`
- database mapping in `DatabaseServiceImpl`
- SQL queries/upserts in `SQLQueries`
- tests

## Localization

Locale files live in:

```text
adapter-spigot/src/main/resources/Locales/
```

Runtime copies are written under:

```text
plugins/FriendNet/Locales/
```

FriendNet supports root-language fallback. For example, `en_US` can fall back to `en` for missing keys. If no matching key is found, the localization path itself is shown, which makes missing keys visible during development.

GUI language entries may include custom skull texture strings where supported by the GUI.

## Commands

Main commands:

```text
/friend
/friends
```

Important subcommands:

```text
/friend list
/friend add <player>
/friend remove <player>
/friend block <player>
/friend unblock <player>
/friend requests
/friend accept <player>
/friend acceptall
/friend deny <player>
/friend denyall
/friend cancel <player>
/friend reload
```

Most player-name command paths resolve known players from FriendNet player data. Some commands intentionally suggest only sensible online or relationship-specific candidates in tab completion.

## Permissions

Permissions are defined in:

```text
core/src/main/java/com/trickshotmlg/friendnet/core/permissions/PermissionHolder.java
```

`plugin.yml` is generated during the Maven build by:

```text
adapter-spigot/src/main/java/com/trickshotmlg/friendnet/adapter_spigot/Utils/PluginYMLGenerator.java
```

Generated permissions use the FriendNet permission tree and default to `false`, so a permissions plugin such as LuckPerms is recommended.

## Testing

Run core tests:

```powershell
mvn -pl core -am test
```

Run adapter compile:

```powershell
mvn -pl adapter-spigot -am -DskipTests compile
```

The core test suite includes:

- service tests
- permission tests
- SQLite migration integration tests
- MySQL/MariaDB migration integration tests using Testcontainers

MySQL/MariaDB container tests require Docker. If Docker is not available, they are skipped.

## Important Notices

- This plugin is still under active development.
- Standalone mode is currently the implemented mode.
- Proxy mode is planned but not available yet.
- Database connection changes require restart.
- Development database files and IntelliJ local state are intentionally ignored by Git.
- The plugin targets Java 17 and Paper/Spigot 1.21.
- Some APIs and package boundaries may still change before a stable release.

## Contributing

Contributions should keep the current project shape intact unless there is a clear reason to change it.

Recommended workflow:

1. Create a focused branch for one feature or fix.
2. Keep commits scoped and readable.
3. Add or update tests when changing database, service, command, or GUI behavior.
4. Run the relevant Maven checks before submitting changes.
5. Do not edit old migrations; add new migration versions.
6. Update localization files for new user-facing messages or GUI text.
7. Avoid committing generated server data, local database files, or IDE workspace state.

Before opening a pull request, run:

```powershell
mvn -pl core -am test
mvn -pl adapter-spigot -am -DskipTests compile
```

If your change affects MySQL or MariaDB behavior, run the tests with Docker available so the container integration tests execute.

## License

FriendNet is licensed under the MIT License. See [LICENSE](LICENSE).
