package com.trickshotmlg.friendnet.core_api.models;

import java.util.*;

public class LocaleKey {
    private static final Map<String, LocaleKey> REGISTRY = new HashMap<>();
    private static LocaleKey DEFAULT_LOCALE = null;

    private String language;
    private String country;

    public LocaleKey(String code) {
        String[] parts = code.split("_");
        String lang = parts[0];
        String ctry = (parts.length > 1) ? parts[1] : "";

        this.language = lang.toLowerCase();
        this.country = (ctry != null) ? ctry.toUpperCase() : "";
    }

    public String getLanguage() { return language; }
    public String getCountry() { return country; }
    public String getCode() { return language + (country.isEmpty() ? "" : "_" + country); }
    public static LocaleKey getDefaultLocale() { return DEFAULT_LOCALE; }
    public static void setDefaultLocale(LocaleKey defaultLocale) { DEFAULT_LOCALE = defaultLocale; }


    /**
     * Register or return an existing LocaleKey for a code
     * @param code the full code e.g. de, de_DE, en, en_US, en_GB
     * @return The LocaleKey registered or fetched
     */
    public static LocaleKey of(String code) {
        LocaleKey localeKey = new LocaleKey(code);
        return REGISTRY.computeIfAbsent(localeKey.getCode(), k -> localeKey);
    }

    /**
     * Tries to fetch the LocaleKey for a given code from Registry
     * @param code
     * @return
     */
    public static Optional<LocaleKey> fetch(String code) {
        return Optional.ofNullable(REGISTRY.getOrDefault(code, null));
    }

    public static LocaleKey getOrFallback(String code) {
        LocaleKey key = REGISTRY.get(code);
        if (key != null) return key;

        // try language-only
        String lang = code.split("_")[0];
        key = REGISTRY.get(lang);
        return (key != null) ? key : getDefaultLocale();
    }

    public static boolean exists(LocaleKey key) { return REGISTRY.containsKey(key.getCode()); }

    public static void clearRegistry() {
        REGISTRY.clear();
    }

    public static Collection<LocaleKey> values() {
        return REGISTRY.values();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof LocaleKey other && this.language.equals(other.language) && this.country.equals(other.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(language, country);
    }

    public String toString() {
        return country.isEmpty() ? language : language + "_" + country;
    }
}
