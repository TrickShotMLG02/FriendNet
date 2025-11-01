package com.trickshotmlg.friendnet.core_api.interfaces;

import com.trickshotmlg.friendnet.core_api.models.LocaleKey;

import java.util.UUID;

public interface LocaleManager {

    /**
     * Sets the preferred locale for a player.
     */
    void setPlayerLocale(UUID playerId, LocaleKey locale);

    /**
     * Gets the preferred locale for a player.
     * Falls back to the default locale if none is set.
     */
    LocaleKey getPlayerLocale(UUID playerId);

    /**
     * Loads all available locale files from the locales directory.
     * Each file must be named like "*_en.yml" or "*_de.yml".
     */
    public void loadLocales();

    /**
     * Gets a translated message for the given locale and path.
     * Falls back to the default locale if not found.
     */
    public String getMessage(UUID playerId, String type, String path);
}
