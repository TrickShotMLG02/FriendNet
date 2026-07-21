package com.trickshotmlg.friendnet.core_api.models;

import junit.framework.TestCase;

import java.util.List;

public class LocaleKeyTest extends TestCase {

    public void testCanonicalizesSupportedBcp47StyleCodes() {
        assertLocaleCode("en", "en");
        assertLocaleCode("de", "de");
        assertLocaleCode("fr", "fr");
        assertLocaleCode("es", "es");
        assertLocaleCode("pt_BR", "pt_BR");
        assertLocaleCode("de_CH", "de_CH");
        assertLocaleCode("zh_CN", "zh_CN");
        assertLocaleCode("zh_TW", "zh_TW");
        assertLocaleCode("gsw_CH", "gsw_CH");
        assertLocaleCode("sr_Latn", "sr_Latn");
        assertLocaleCode("sr_Latn_RS", "sr_Latn_RS");
        assertLocaleCode("en_US_POSIX", "en_US_POSIX");
    }

    public void testHyphenSeparatedTagsNormalizeToUnderscoreCodes() {
        assertLocaleCode("pt-BR", "pt_BR");
        assertLocaleCode("sr-Latn-RS", "sr_Latn_RS");
        assertLocaleCode("en-US-POSIX", "en_US_POSIX");
    }

    public void testFallbackChainKeepsScriptBeforeLanguage() {
        LocaleKey locale = new LocaleKey("sr_Latn_RS");

        assertEquals(
                List.of(new LocaleKey("sr_Latn_RS"), new LocaleKey("sr_Latn"), new LocaleKey("sr")),
                locale.fallbackChain()
        );
    }

    public void testParsesLocaleFileNames() {
        LocaleKey.LocaleFileName fileName = LocaleKey.parseLocaleFileName("messages_en_US_POSIX").orElseThrow();

        assertEquals("messages", fileName.baseName());
        assertEquals(new LocaleKey("en_US_POSIX"), fileName.locale());
    }

    private void assertLocaleCode(String input, String expected) {
        assertEquals(expected, new LocaleKey(input).getCode());
    }
}
