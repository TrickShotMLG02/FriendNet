package com.trickshotmlg.friendnet.core.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CommentedYamlConfigUpdater {

    private static final Pattern KEY_PATTERN = Pattern.compile("^([A-Za-z0-9_-]+):(.*)$");
    private static final DateTimeFormatter BACKUP_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private CommentedYamlConfigUpdater() {
    }

    public static UpdateResult update(Path configPath, String bundledTemplate) throws IOException {
        Files.createDirectories(configPath.getParent());

        String templateHash = sha256(bundledTemplate);
        Path hashPath = configPath.resolveSibling(configPath.getFileName() + ".template.sha256");
        if (Files.notExists(configPath)) {
            Files.writeString(configPath, bundledTemplate, StandardCharsets.UTF_8);
            Files.writeString(hashPath, templateHash, StandardCharsets.UTF_8);
            return UpdateResult.CREATED;
        }

        if (Files.exists(hashPath) && templateHash.equals(Files.readString(hashPath, StandardCharsets.UTF_8).trim())) {
            return UpdateResult.UNCHANGED;
        }

        String existing = Files.readString(configPath, StandardCharsets.UTF_8);
        String merged = mergeTemplateWithExistingValues(bundledTemplate, existing);
        if (merged.equals(existing)) {
            Files.writeString(hashPath, templateHash, StandardCharsets.UTF_8);
            return UpdateResult.UNCHANGED;
        }

        Path backupPath = backupPath(configPath);
        Files.copy(configPath, backupPath);
        Files.writeString(configPath, merged, StandardCharsets.UTF_8);
        Files.writeString(hashPath, templateHash, StandardCharsets.UTF_8);
        return UpdateResult.UPDATED;
    }

    static String mergeTemplateWithExistingValues(String template, String existing) {
        Map<String, Value> existingValues = parseLeafValues(existing);
        List<String> templateLines = template.lines().toList();
        List<String> output = new ArrayList<>();
        ArrayDeque<PathSegment> stack = new ArrayDeque<>();

        for (int i = 0; i < templateLines.size(); i++) {
            String line = templateLines.get(i);
            Optional<KeyLine> keyLine = keyLine(line);
            if (keyLine.isEmpty()) {
                output.add(line);
                continue;
            }

            KeyLine key = keyLine.get();
            popStack(stack, key.indent());
            String path = path(stack, key.key());
            Value value = existingValues.get(path);
            if (value == null) {
                output.add(line);
                if (key.value().isBlank()) {
                    stack.addLast(new PathSegment(key.indent(), key.key()));
                }
                continue;
            }

            if (value instanceof ScalarValue scalarValue) {
                output.add(line.substring(0, line.indexOf(':') + 1) + " " + scalarValue.value());
                continue;
            }

            ListValue listValue = (ListValue) value;
            output.add(line.substring(0, line.indexOf(':') + 1));
            String listIndent = listIndent(templateLines, i, key.indent());
            for (String item : listValue.items()) {
                output.add(listIndent + "- " + item);
            }
            i = skipTemplateList(templateLines, i, key.indent());
        }

        return String.join(System.lineSeparator(), output) + System.lineSeparator();
    }

    private static Map<String, Value> parseLeafValues(String content) {
        List<String> lines = content.lines().toList();
        Map<String, Value> values = new HashMap<>();
        ArrayDeque<PathSegment> stack = new ArrayDeque<>();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            Optional<KeyLine> keyLine = keyLine(line);
            if (keyLine.isEmpty()) {
                continue;
            }

            KeyLine key = keyLine.get();
            popStack(stack, key.indent());
            String path = path(stack, key.key());
            if (!key.value().isBlank()) {
                values.put(path, new ScalarValue(key.value().trim()));
                continue;
            }

            List<String> listItems = readListItems(lines, i, key.indent());
            if (!listItems.isEmpty()) {
                values.put(path, new ListValue(listItems));
                i = skipTemplateList(lines, i, key.indent());
                continue;
            }

            stack.addLast(new PathSegment(key.indent(), key.key()));
        }

        return values;
    }

    private static Optional<KeyLine> keyLine(String line) {
        if (line.isBlank() || line.stripLeading().startsWith("#")) {
            return Optional.empty();
        }

        int indent = indent(line);
        Matcher matcher = KEY_PATTERN.matcher(line.stripLeading());
        if (!matcher.matches()) {
            return Optional.empty();
        }

        return Optional.of(new KeyLine(indent, matcher.group(1), matcher.group(2)));
    }

    private static List<String> readListItems(List<String> lines, int keyIndex, int parentIndent) {
        List<String> items = new ArrayList<>();
        for (int i = keyIndex + 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isBlank() || line.stripLeading().startsWith("#")) {
                continue;
            }
            if (indent(line) <= parentIndent) {
                break;
            }
            String stripped = line.stripLeading();
            if (!stripped.startsWith("- ")) {
                break;
            }
            items.add(stripped.substring(2).trim());
        }
        return items;
    }

    private static int skipTemplateList(List<String> lines, int keyIndex, int parentIndent) {
        int lastListLine = keyIndex;
        for (int i = keyIndex + 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isBlank() || line.stripLeading().startsWith("#")) {
                continue;
            }
            if (indent(line) <= parentIndent || !line.stripLeading().startsWith("- ")) {
                break;
            }
            lastListLine = i;
        }
        return lastListLine;
    }

    private static String listIndent(List<String> lines, int keyIndex, int parentIndent) {
        for (int i = keyIndex + 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isBlank() || line.stripLeading().startsWith("#")) {
                continue;
            }
            if (indent(line) <= parentIndent || !line.stripLeading().startsWith("- ")) {
                break;
            }
            return line.substring(0, indent(line));
        }
        return " ".repeat(parentIndent + 2);
    }

    private static void popStack(ArrayDeque<PathSegment> stack, int indent) {
        while (!stack.isEmpty() && stack.peekLast().indent() >= indent) {
            stack.removeLast();
        }
    }

    private static String path(ArrayDeque<PathSegment> stack, String key) {
        List<String> parts = new ArrayList<>();
        for (PathSegment segment : stack) {
            parts.add(segment.key());
        }
        parts.add(key);
        return String.join(".", parts);
    }

    private static int indent(String line) {
        int indent = 0;
        while (indent < line.length() && line.charAt(indent) == ' ') {
            indent++;
        }
        return indent;
    }

    private static Path backupPath(Path configPath) {
        String timestamp = LocalDateTime.now().format(BACKUP_TIMESTAMP);
        return configPath.resolveSibling(configPath.getFileName() + ".backup-" + timestamp);
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available.", e);
        }
    }

    public enum UpdateResult {
        CREATED,
        UPDATED,
        UNCHANGED
    }

    private sealed interface Value permits ScalarValue, ListValue {
    }

    private record ScalarValue(String value) implements Value {
    }

    private record ListValue(List<String> items) implements Value {
    }

    private record KeyLine(int indent, String key, String value) {
    }

    private record PathSegment(int indent, String key) {
    }
}
