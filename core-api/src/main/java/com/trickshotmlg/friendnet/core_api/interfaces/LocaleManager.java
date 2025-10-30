package com.trickshotmlg.friendnet.core_api.interfaces;

import com.trickshotmlg.friendnet.core_api.enums.Locale;

import java.util.UUID;

public interface LocaleManager {

    /**
     *
     * @param locale
     */
    void setDefaultLocale(Locale locale);

    /**
     *
     * @return
     */
    Locale getDefaultLocale();

    /**
     * Sets the preferred locale for a player.
     */
    void setPlayerLocale(UUID playerId, Locale locale);

    /**
     * Gets the preferred locale for a player.
     * Falls back to the default locale if none is set.
     */
    Locale getPlayerLocale(UUID playerId);

    /**
     * Loads all available locale files from the locales directory.
     * Each file must be named like "en_US.yml" or "de_DE.yml".
     */
    public void loadLocales();

    /**
     * Gets a translated message for the given locale and path.
     * Falls back to the default locale if not found.
     */
    public String getMessage(UUID playerId, String type, String path);
}
