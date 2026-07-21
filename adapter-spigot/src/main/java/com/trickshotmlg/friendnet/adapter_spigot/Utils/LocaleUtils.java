package com.trickshotmlg.friendnet.adapter_spigot.Utils;

import com.trickshotmlg.friendnet.core_api.models.LocaleKey;

import java.util.Locale;

public class LocaleUtils {

    /**
     * Get the language name of a source locale, localized to the target locale.
     *
     * @param targetCode Locale code for the display language (e.g., "de_CH")
     * @param sourceCode Locale code to get the name of (e.g., "en_US")
     * @return Localized language name (e.g., "Englisch" if target="de_CH" and source="en_US")
     */
    public static String getLocalizedLanguageName(String targetCode, String sourceCode) {
        Locale targetLocale = parseLocale(targetCode);
        Locale sourceLocale = parseLocale(sourceCode);
        return sourceLocale.getDisplayName(targetLocale);
    }

    public static String getLocalizedLanguageName(LocaleKey targetCode, LocaleKey sourceCode) {
        return getLocalizedLanguageName(targetCode.getCode(), sourceCode.getCode());
    }

    public static Locale parseLocale(String code) {
        if (code == null || code.isBlank()) {
            return Locale.getDefault();
        }

        return Locale.forLanguageTag(new LocaleKey(code).toLanguageTag());
    }
}
