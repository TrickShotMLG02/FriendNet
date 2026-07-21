package com.trickshotmlg.friendnet.adapter_spigot.Configs;

import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
import com.trickshotmlg.friendnet.core.Logger;
import com.trickshotmlg.friendnet.core_api.interfaces.AbstractConfig;
import com.trickshotmlg.friendnet.core_api.interfaces.LocaleManager;
import com.trickshotmlg.friendnet.core_api.models.LocaleKey;
import com.trickshotmlg.friendnet.core_api.models.PlayerData;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class SpigotLocaleManager implements LocaleManager {

    private final FriendNetPlugin plugin;

    private Map<String, Map<LocaleKey, AbstractConfig>> configs = new HashMap<>();

    public SpigotLocaleManager(FriendNetPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void setPlayerLocale(UUID playerId, LocaleKey locale) {
        PlayerData playerData = plugin.getPlayerService().getPlayerData(playerId);
        if (playerData == null) {
            return;
        }
        playerData.setLocale(locale);
        if (plugin.getPlayerDataSaveQueue() != null) {
            plugin.getPlayerDataSaveQueue().markDirty(playerId);
        }
    }

    @Override
    public LocaleKey getPlayerLocale(UUID playerId) {
        try {
            PlayerData playerData = plugin.getPlayerService().getPlayerData(playerId);
            return playerData.getLocale();
        } catch (Exception e) {
           return LocaleKey.getDefaultLocale();
        }
    }

    private List<String> getBundledLocaleResourceNames() {
        List<String> resourceNames = new ArrayList<>();

        try {
            URL jarUrl = getClass().getProtectionDomain().getCodeSource().getLocation();
            File codeSource = new File(jarUrl.toURI());

            if (codeSource.isDirectory()) {
                File localesDir = new File(codeSource, "Locales");
                if (localesDir.isDirectory()) {
                    try (var paths = Files.walk(localesDir.toPath())) {
                        paths
                                .filter(Files::isRegularFile)
                                .filter(path -> path.getFileName().toString().endsWith(".yml"))
                                .forEach(path -> resourceNames.add(
                                        "Locales/" + localesDir.toPath().relativize(path).toString().replace("\\", "/")
                                ));
                    }
                }
            } else {
                try (JarFile jar = new JarFile(codeSource)) {
                    Enumeration<JarEntry> entries = jar.entries();

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
            Logger.warn("Could not discover bundled locale files: " + e.getMessage());
        }

        return resourceNames;
    }

    @Override
    public void loadLocales() {
        // clear registry and re-create it from supported locals of config
        LocaleKey.clearRegistry();
        for (String code : plugin.getConfig().getStringList("SupportedLocales")) {
            LocaleKey.of(code);
        }

        // set default locale
        LocaleKey defLoc = new LocaleKey(plugin.getConfig().getString("DefaultLocale"));
        if (LocaleKey.exists(defLoc)) {
            LocaleKey.setDefaultLocale(defLoc);
        } else {
            // set default locale to first found locale
            LocaleKey.setDefaultLocale(LocaleKey.values().stream().toList().get(0));
        }


        configs.clear();


        File localesDir = new File(plugin.getDataFolder(), "Locales");
        if (!localesDir.exists() || !localesDir.isDirectory()) {
            localesDir.mkdirs();
        }

        for (String resourceName : getBundledLocaleResourceNames()) {
            File localeFile = new File(plugin.getDataFolder(), resourceName);
            if (!localeFile.exists()) {
                plugin.saveResource(resourceName, false);
            }
        }

        File[] files = localesDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;

        for (File file : files) {
            String filename = file.getName(); // e.g., messages_en.yml or gui_de_DE.yml
            int lastDot = filename.lastIndexOf('.');
            if (lastDot == -1) continue; // skip files without extension

            String nameWithoutExtension = filename.substring(0, lastDot);

            Optional<LocaleKey.LocaleFileName> localeFileName = LocaleKey.parseLocaleFileName(nameWithoutExtension);
            if (localeFileName.isEmpty()) {
                continue;
            }

            String baseName = localeFileName.get().baseName();
            LocaleKey locale = localeFileName.get().locale();
            if (!isSupportedLocaleFileLocale(locale)) {
                // skip this file, since its locale code is not in the supported locales config value
                continue;
            }

            // Create AbstractConfig instance for this file
            String relativeFilePath = plugin.getDataFolder().toPath()
                    .relativize(file.toPath())
                    .toString()
                    .replace("\\", "/");
            AbstractConfig config = new SpigotConfig(plugin, relativeFilePath); // your concrete implementation

            try {
                config.initDefaults();
            } catch (Exception e) {
                //throw new RuntimeException(e);
            }

            // Store config by base name and locale
            configs.computeIfAbsent(baseName.toLowerCase(), t -> new HashMap<>())
                    .put(locale, config);
        }
    }

    @Override
    public String getMessage(UUID playerId, String type, String path) {
        LocaleKey locale = getPlayerLocale(playerId); // uses default if none
        return getMessage(locale, type, path);
    }

    public String getMessage(UUID playerId, String type, String path, Map<String, Object> placeholders) {
        String message = getMessage(playerId, type, path);

        for (Map.Entry<String, Object> entry : placeholders.entrySet()) {
            message = message.replace("%" + entry.getKey() + "%", entry.getValue().toString());
        }
        return message;
    }

    public Optional<String> getLocaleString(LocaleKey locale, String type, String path) {
        Map<LocaleKey, AbstractConfig> localeConfigs = configs.get(type);
        if (localeConfigs == null || localeConfigs.isEmpty()) {
            return Optional.empty();
        }

        AbstractConfig configExact = locale != null ? localeConfigs.get(locale) : null;
        AbstractConfig configRoot = null;

        if (locale != null) {
            configRoot = locale.fallbackChain().stream()
                    .skip(1)
                    .map(localeConfigs::get)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
        }

        String value = tryGetString(configExact, path);
        if (value == null) value = tryGetString(configRoot, path);

        if (value == null || value.isBlank()) {
            return Optional.empty();
        }

        return Optional.of(value);
    }

    /**
     * Returns the Color Code Formatted message in a specific locale from a key in a message file
     * @param locale
     * @param type
     * @param path
     * @return
     */
    private String getMessage(LocaleKey locale, String type, String path) {
        Map<LocaleKey, AbstractConfig> localeConfigs = configs.get(type);
        if (localeConfigs == null || localeConfigs.isEmpty()) {
            return path; // No configs for this type at all
        }

        LocaleKey defaultLocale = LocaleKey.getDefaultLocale();
        AbstractConfig configExact = locale != null ? localeConfigs.get(locale) : null;
        AbstractConfig configRoot = null;
        AbstractConfig configDefault = defaultLocale != null ? localeConfigs.get(defaultLocale) : null;

        // Try parent locale configs, e.g. sr_Latn_RS -> sr_Latn -> sr.
        if (locale != null) {
            configRoot = locale.fallbackChain().stream()
                    .skip(1)
                    .map(localeConfigs::get)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
        }

        // Try reading from most specific to most general
        String message = tryGetString(configExact, path);
        if (message == null) message = tryGetString(configRoot, path);
        if (message == null) message = tryGetString(configDefault, path);

        // Fallback to showing the path itself
        if (message == null || message.isEmpty()) {
            return path;
        }

        return org.bukkit.ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * Safely tries to get a string or string-list from a config.
     */
    private String tryGetString(AbstractConfig config, String path) {
        if (config == null) return null;
        Optional<String> msgOpt = config.getString(path);
        if (msgOpt.isPresent() && !msgOpt.get().isEmpty()) {
            return msgOpt.get();
        }

        Object value = config.get(path);
        if (value instanceof List<?> list && !list.isEmpty()) {
            List<String> lines = new ArrayList<>();
            for (Object item : list) {
                if (item != null) {
                    lines.add(item.toString());
                }
            }
            return String.join("\n", lines);
        }

        return null;
    }

    private boolean isSupportedLocaleFileLocale(LocaleKey fileLocale) {
        if (LocaleKey.exists(fileLocale)) {
            return true;
        }

        for (LocaleKey supportedLocale : LocaleKey.values()) {
            if (supportedLocale.fallbackChain().contains(fileLocale)
                    || fileLocale.fallbackChain().contains(supportedLocale)) {
                return true;
            }
        }

        return false;
    }
}
