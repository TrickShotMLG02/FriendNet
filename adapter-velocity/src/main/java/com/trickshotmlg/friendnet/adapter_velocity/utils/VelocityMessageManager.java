package com.trickshotmlg.friendnet.adapter_velocity.utils;

import com.trickshotmlg.friendnet.adapter_velocity.FriendNetVelocityPlugin;
import com.trickshotmlg.friendnet.core.Logger;
import com.trickshotmlg.friendnet.core_api.models.LocaleKey;
import com.trickshotmlg.friendnet.core_api.models.PlayerData;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VelocityMessageManager {

    private static final String MESSAGE_FILE_TYPE = "messages";
    private static final Pattern LOCALE_PATTERN = Pattern.compile("_(\\w{2}(?:_\\w{2})?)$");

    private final FriendNetVelocityPlugin plugin;
    private final Map<String, Map<LocaleKey, Map<String, Object>>> messages = new HashMap<>();

    public VelocityMessageManager(FriendNetVelocityPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadMessages() {
        loadLocaleRegistry();
        copyBundledLocaleFiles();
        loadLocaleFiles();
    }

    public void send(CommandSource source, String key) {
        send(source, key, Map.of());
    }

    public void send(CommandSource source, String key, Map<String, Object> placeholders) {
        if (source == null) {
            return;
        }

        source.sendMessage(format(sourceLocale(source), key, placeholders, true));
    }

    private Component format(LocaleKey locale, String key, Map<String, Object> placeholders, boolean prependPrefix) {
        String message = getMessage(locale, MESSAGE_FILE_TYPE, key);
        for (Map.Entry<String, Object> entry : placeholders.entrySet()) {
            message = message.replace("%" + entry.getKey() + "%", entry.getValue() != null ? entry.getValue().toString() : "");
        }

        if (prependPrefix) {
            String prefix = getMessage(locale, MESSAGE_FILE_TYPE, "prefix");
            if (prefix != null && !prefix.isBlank() && !"prefix".equals(prefix)) {
                message = prefix + " " + message;
            }
        }

        return LegacyComponentSerializer.legacyAmpersand().deserialize(message);
    }

    private String getMessage(LocaleKey locale, String type, String path) {
        Map<LocaleKey, Map<String, Object>> localeMessages = messages.get(type);
        if (localeMessages == null || localeMessages.isEmpty()) {
            return path;
        }

        LocaleKey defaultLocale = LocaleKey.getDefaultLocale();
        Map<String, Object> exact = locale != null ? localeMessages.get(locale) : null;
        Map<String, Object> root = null;
        if (locale != null && locale.getLanguage() != null && !locale.getLanguage().equalsIgnoreCase(locale.toString())) {
            root = localeMessages.get(new LocaleKey(locale.getLanguage()));
        }
        Map<String, Object> fallback = defaultLocale != null ? localeMessages.get(defaultLocale) : null;

        String value = tryGetString(exact, path);
        if (value == null) value = tryGetString(root, path);
        if (value == null) value = tryGetString(fallback, path);

        return value != null && !value.isBlank() ? value : path;
    }

    private String tryGetString(Map<String, Object> values, String path) {
        if (values == null) {
            return null;
        }

        Object value = get(values, path);
        if (value instanceof List<?> list && !list.isEmpty()) {
            List<String> lines = new ArrayList<>();
            for (Object item : list) {
                if (item != null) {
                    lines.add(item.toString());
                }
            }
            return String.join("\n", lines);
        }

        return value != null ? value.toString() : null;
    }

    private Object get(Map<String, Object> values, String path) {
        String[] parts = path.split("\\.");
        Object current = values;
        for (String part : parts) {
            if (!(current instanceof Map<?, ?> map)) {
                return null;
            }
            current = map.get(part);
        }
        return current;
    }

    private LocaleKey sourceLocale(CommandSource source) {
        if (source instanceof Player player && plugin.getPlayerService() != null) {
            PlayerData playerData = plugin.getPlayerService().getPlayerData(player.getUniqueId());
            if (playerData != null && playerData.getLocale() != null) {
                return playerData.getLocale();
            }
        }

        return LocaleKey.getDefaultLocale();
    }

    private void loadLocaleRegistry() {
        LocaleKey.clearRegistry();
        for (String code : plugin.getConfig().getStringList("SupportedLocales")) {
            LocaleKey.of(code);
        }

        if (LocaleKey.values().isEmpty()) {
            LocaleKey.of("en_US");
        }

        LocaleKey configuredDefault = new LocaleKey(plugin.getConfig().getString("DefaultLocale", "en_US"));
        Optional<LocaleKey> registeredDefault = LocaleKey.fetch(configuredDefault.getCode());
        LocaleKey.setDefaultLocale(registeredDefault.orElse(LocaleKey.values().stream().findFirst().orElse(configuredDefault)));
    }

    private void copyBundledLocaleFiles() {
        Path localesDirectory = plugin.getDataDirectory().resolve("Locales");
        try {
            Files.createDirectories(localesDirectory);
            for (String resourceName : getBundledLocaleResourceNames()) {
                Path target = plugin.getDataDirectory().resolve(resourceName);
                if (Files.notExists(target)) {
                    try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourceName)) {
                        if (inputStream != null) {
                            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not copy bundled Velocity locale files", e);
        }
    }

    private void loadLocaleFiles() {
        messages.clear();
        Path localesDirectory = plugin.getDataDirectory().resolve("Locales");
        if (!Files.isDirectory(localesDirectory)) {
            return;
        }

        try (var paths = Files.list(localesDirectory)) {
            paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".yml"))
                    .forEach(this::loadLocaleFile);
        } catch (IOException e) {
            throw new IllegalStateException("Could not load Velocity locale files", e);
        }
    }

    private void loadLocaleFile(Path file) {
        String filename = file.getFileName().toString();
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1) {
            return;
        }

        String nameWithoutExtension = filename.substring(0, lastDot);
        Matcher matcher = LOCALE_PATTERN.matcher(nameWithoutExtension);
        if (!matcher.find()) {
            return;
        }

        String type = nameWithoutExtension.substring(0, matcher.start()).toLowerCase();
        LocaleKey locale = new LocaleKey(matcher.group(1));
        if (!isSupportedLocale(locale)) {
            return;
        }

        try (InputStream inputStream = Files.newInputStream(file)) {
            Object loaded = new Yaml().load(inputStream);
            if (loaded instanceof Map<?, ?> loadedMap) {
                messages.computeIfAbsent(type, ignored -> new HashMap<>())
                        .put(locale, (Map<String, Object>) loadedMap);
                Logger.debug("Loaded Velocity locale file " + filename);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not load Velocity locale file " + filename, e);
        }
    }

    private boolean isSupportedLocale(LocaleKey locale) {
        if (LocaleKey.exists(locale)) {
            return true;
        }

        for (LocaleKey supportedLocale : LocaleKey.values()) {
            if (supportedLocale.getLanguage().equals(locale.getLanguage())) {
                return true;
            }
        }
        return false;
    }

    private List<String> getBundledLocaleResourceNames() {
        List<String> resourceNames = new ArrayList<>();

        try {
            URL jarUrl = getClass().getProtectionDomain().getCodeSource().getLocation();
            Path codeSource = Path.of(jarUrl.toURI());

            if (Files.isDirectory(codeSource)) {
                Path localesDirectory = codeSource.resolve("Locales");
                if (Files.isDirectory(localesDirectory)) {
                    try (var paths = Files.walk(localesDirectory)) {
                        paths
                                .filter(Files::isRegularFile)
                                .filter(path -> path.getFileName().toString().endsWith(".yml"))
                                .forEach(path -> resourceNames.add(
                                        "Locales/" + localesDirectory.relativize(path).toString().replace("\\", "/")
                                ));
                    }
                }
            } else {
                try (JarFile jarFile = new JarFile(codeSource.toFile())) {
                    Enumeration<JarEntry> entries = jarFile.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        String name = entry.getName();
                        if (!entry.isDirectory() && name.startsWith("Locales/") && name.endsWith(".yml")) {
                            resourceNames.add(name);
                        }
                    }
                }
            }
        } catch (IOException | URISyntaxException e) {
            Logger.warn("Could not discover bundled Velocity locale files: " + e.getMessage());
        }

        Collections.sort(resourceNames);
        return resourceNames;
    }
}
