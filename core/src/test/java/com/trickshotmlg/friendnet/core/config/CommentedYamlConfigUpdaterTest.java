package com.trickshotmlg.friendnet.core.config;

import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CommentedYamlConfigUpdaterTest {

    @Test
    public void testMergeKeepsTemplateCommentsAndExistingValues() {
        String template = """
                # New template comment
                Mode: Standalone

                SupportedLocales:
                  - en_US
                  - de

                MySQL:
                  host: localhost:3306
                  username: friendnet
                """;
        String existing = """
                Mode: Proxy
                SupportedLocales:
                  - de
                  - gsw_CH
                MySQL:
                  host: db:3306
                  username: custom
                OldKey: ignored
                """;

        String merged = CommentedYamlConfigUpdater.mergeTemplateWithExistingValues(template, existing);

        assertTrue(merged.contains("# New template comment"));
        assertTrue(merged.contains("Mode: Proxy"));
        assertTrue(merged.contains("  - de"));
        assertTrue(merged.contains("  - gsw_CH"));
        assertTrue(merged.contains("  host: db:3306"));
        assertTrue(merged.contains("  username: custom"));
        assertFalse(merged.contains("OldKey"));
    }

    @Test
    public void testUpdateCreatesMissingFileWithTemplate() throws Exception {
        Path directory = Files.createTempDirectory("friendnet-config-test");
        Path config = directory.resolve("config.yml");

        CommentedYamlConfigUpdater.UpdateResult result = CommentedYamlConfigUpdater.update(config, "# Comment\nDebug: false\n");

        assertEquals(CommentedYamlConfigUpdater.UpdateResult.CREATED, result);
        assertEquals("# Comment\nDebug: false\n", Files.readString(config));
        assertTrue(Files.exists(directory.resolve("config.yml.template.sha256")));
    }

    @Test
    public void testUpdateBacksUpAndMergesWhenTemplateChanges() throws Exception {
        Path directory = Files.createTempDirectory("friendnet-config-test");
        Path config = directory.resolve("config.yml");

        CommentedYamlConfigUpdater.update(config, "Debug: false\n");
        Files.writeString(config, "Debug: true\n");

        CommentedYamlConfigUpdater.UpdateResult result = CommentedYamlConfigUpdater.update(config, "# New\nDebug: false\nMode: Standalone\n");

        assertEquals(CommentedYamlConfigUpdater.UpdateResult.UPDATED, result);
        String updated = Files.readString(config);
        assertTrue(updated.contains("# New"));
        assertTrue(updated.contains("Debug: true"));
        assertTrue(updated.contains("Mode: Standalone"));
        assertTrue(Files.list(directory).anyMatch(path -> path.getFileName().toString().startsWith("config.yml.backup-")));
    }

    @Test
    public void testUpdateDoesNotRewriteWhenTemplateHashIsUnchanged() throws Exception {
        Path directory = Files.createTempDirectory("friendnet-config-test");
        Path config = directory.resolve("config.yml");
        String template = "Debug: false\n";

        CommentedYamlConfigUpdater.update(config, template);
        Files.writeString(config, "Debug: true\n");

        CommentedYamlConfigUpdater.UpdateResult result = CommentedYamlConfigUpdater.update(config, template);

        assertEquals(CommentedYamlConfigUpdater.UpdateResult.UNCHANGED, result);
        assertEquals("Debug: true\n", Files.readString(config));
    }
}
