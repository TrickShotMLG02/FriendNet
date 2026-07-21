package com.trickshotmlg.friendnet.core_api.models;

import java.util.*;

public class LocaleKey {
    private static final Map<String, LocaleKey> REGISTRY = new HashMap<>();
    private static LocaleKey DEFAULT_LOCALE = null;

    private final String language;
    private final String script;
    private final String region;
    private final String variant;
    private final String code;

    public LocaleKey(String code) {
        String normalized = normalize(code);
        Locale locale = Locale.forLanguageTag(toLanguageTag(normalized));

        this.language = locale.getLanguage().toLowerCase(Locale.ROOT);
        this.script = toTitleCase(locale.getScript());
        this.region = locale.getCountry().toUpperCase(Locale.ROOT);
        this.variant = normalizeVariant(locale.getVariant());
        this.code = buildCode(language, script, region, variant);
    }

    public String getLanguage() { return language; }
    public String getScript() { return script; }
    public String getCountry() { return region; }
    public String getRegion() { return region; }
    public String getVariant() { return variant; }
    public String getCode() { return code; }
    public String toLanguageTag() { return code.replace('_', '-'); }
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
        return Optional.ofNullable(REGISTRY.get(new LocaleKey(code).getCode()));
    }

    public static LocaleKey getOrFallback(String code) {
        if (code == null || code.isBlank()) {
            return getDefaultLocale();
        }

        LocaleKey requested = new LocaleKey(code);
        for (LocaleKey candidate : requested.fallbackChain()) {
            LocaleKey key = REGISTRY.get(candidate.getCode());
            if (key != null) {
                return key;
            }
        }

        return getDefaultLocale();
    }

    public static boolean exists(LocaleKey key) { return REGISTRY.containsKey(key.getCode()); }

    public static void clearRegistry() {
        REGISTRY.clear();
    }

    public static Collection<LocaleKey> values() {
        return REGISTRY.values();
    }

    public static boolean isValidCode(String code) {
        if (code == null || code.isBlank()) {
            return false;
        }

        LocaleKey key = new LocaleKey(code);
        return !key.getLanguage().isBlank() && key.getCode().equals(normalize(code));
    }

    public List<LocaleKey> fallbackChain() {
        List<LocaleKey> chain = new ArrayList<>();
        chain.add(this);

        if (!variant.isBlank() && !region.isBlank()) {
            chain.add(new LocaleKey(buildCode(language, script, region, "")));
        }

        if (!region.isBlank()) {
            chain.add(new LocaleKey(buildCode(language, script, "", "")));
        }

        if (!script.isBlank()) {
            chain.add(new LocaleKey(language));
        }

        return chain.stream().distinct().toList();
    }

    public static Optional<LocaleFileName> parseLocaleFileName(String nameWithoutExtension) {
        if (nameWithoutExtension == null || nameWithoutExtension.isBlank()) {
            return Optional.empty();
        }

        int underscoreIndex = nameWithoutExtension.indexOf('_');
        while (underscoreIndex >= 0 && underscoreIndex < nameWithoutExtension.length() - 1) {
            String baseName = nameWithoutExtension.substring(0, underscoreIndex);
            String localeCode = nameWithoutExtension.substring(underscoreIndex + 1);
            if (!baseName.isBlank() && isValidCode(localeCode)) {
                return Optional.of(new LocaleFileName(baseName, new LocaleKey(localeCode)));
            }
            underscoreIndex = nameWithoutExtension.indexOf('_', underscoreIndex + 1);
        }

        return Optional.empty();
    }

    private static String normalize(String code) {
        if (code == null || code.isBlank()) {
            Locale defaultLocale = Locale.getDefault();
            return buildCode(
                    defaultLocale.getLanguage().toLowerCase(Locale.ROOT),
                    toTitleCase(defaultLocale.getScript()),
                    defaultLocale.getCountry().toUpperCase(Locale.ROOT),
                    normalizeVariant(defaultLocale.getVariant())
            );
        }

        String languageTag = toLanguageTag(code.trim());
        Locale locale = Locale.forLanguageTag(languageTag);
        return buildCode(
                locale.getLanguage().toLowerCase(Locale.ROOT),
                toTitleCase(locale.getScript()),
                locale.getCountry().toUpperCase(Locale.ROOT),
                normalizeVariant(locale.getVariant())
        );
    }

    private static String toLanguageTag(String code) {
        return code.replace('_', '-');
    }

    private static String buildCode(String language, String script, String region, String variant) {
        StringJoiner joiner = new StringJoiner("_");
        if (language != null && !language.isBlank()) {
            joiner.add(language);
        }
        if (script != null && !script.isBlank()) {
            joiner.add(script);
        }
        if (region != null && !region.isBlank()) {
            joiner.add(region);
        }
        if (variant != null && !variant.isBlank()) {
            joiner.add(variant);
        }
        return joiner.toString();
    }

    private static String toTitleCase(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        String lower = value.toLowerCase(Locale.ROOT);
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    private static String normalizeVariant(String value) {
        return value == null ? "" : value.replace('-', '_');
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof LocaleKey other && this.code.equals(other.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    public String toString() {
        return code;
    }

    public record LocaleFileName(String baseName, LocaleKey locale) {
    }
}
