package com.trickshotmlg.friendnet.adapter_spigot.Configs;

import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
import com.trickshotmlg.friendnet.core_api.enums.Locale;
import com.trickshotmlg.friendnet.core_api.interfaces.AbstractConfig;
import com.trickshotmlg.friendnet.core_api.interfaces.LocaleManager;
import com.trickshotmlg.friendnet.core_api.models.PlayerData;

import java.io.File;
import java.util.*;

public class SpigotLocaleManager implements LocaleManager {

    private final FriendNetPlugin plugin;
    private Locale defaultLocale = Locale.EN;

    private Map<String, Map<Locale, AbstractConfig>> configs = new HashMap<>();

    public SpigotLocaleManager(FriendNetPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void setDefaultLocale(Locale locale) {
        defaultLocale = locale;
    }

    @Override
    public Locale getDefaultLocale() {
        return defaultLocale;
    }

    @Override
    public void setPlayerLocale(UUID playerId, Locale locale) {
        PlayerData playerData = plugin.getPlayerService().getPlayerData(playerId);
        if (playerData == null) {
            return;
        }
        playerData.setLocale(locale);
    }

    @Override
    public Locale getPlayerLocale(UUID playerId) {
        PlayerData playerData = plugin.getPlayerService().getPlayerData(playerId);
        if (playerData == null) return defaultLocale;
        return playerData.getLocale();
    }

    @Override
    public void loadLocales() {
        configs.clear();

        File localesDir = new File(plugin.getDataFolder(), "Locales");
        if (!localesDir.exists() || !localesDir.isDirectory()) {
            localesDir.mkdirs();
            return;
        }

        File[] files = localesDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;

        for (File file : files) {
            String filename = file.getName(); // e.g., messages_en.yml
            int underscore = filename.lastIndexOf('_');
            int dot = filename.lastIndexOf('.');

            if (underscore == -1 || dot == -1 || underscore >= dot) continue;

            String type = filename.substring(0, underscore).toLowerCase(); // messages, gui, etc
            String localeCode = filename.substring(underscore + 1, dot).toUpperCase();

            Locale locale;
            try {
                locale = Locale.valueOf(localeCode);
            } catch (IllegalArgumentException e) {
                // unknown locale, skip
                continue;
            }

            // Create AbstractConfig instance for this file (assuming a concrete implementation exists)
            String relativeFilePath = plugin.getDataFolder().toPath().relativize(file.toPath()).toString().replace("\\", "/");
            AbstractConfig config = new SpigotConfig(plugin, relativeFilePath); // your concrete implementation
            config.load();

            configs.computeIfAbsent(type, t -> new HashMap<>()).put(locale, config);
        }
    }

    @Override
    public String getMessage(UUID playerId, String type, String path) {
        Locale locale = getPlayerLocale(playerId); // uses default if none
        return getMessage(locale, type, path);
    }

    private String getMessage(Locale locale, String type, String path) {
        AbstractConfig config = configs.getOrDefault(type, Collections.emptyMap())
                .getOrDefault(locale, configs.get(type).get(defaultLocale));
        if(config == null) return path; // fallback to path string

        Optional<String> msgOpt = config.getString(path);
        if(!msgOpt.isPresent()) return path;
        String raw = msgOpt.get();

        return raw;
    }
}
