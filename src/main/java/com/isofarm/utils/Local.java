package com.isofarm.utils;

import com.isofarm.data.Languages;
import com.isofarm.data.Singleton;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Properties;

/**
 * Provides local behavior.
 */
@Utils
@Singleton
public class Local {
    private static final Languages[] LANGS = Languages.values();
    public static final Local lang = new Local();
    private final Properties properties = new Properties();
    private Languages currentLanguage;

    /**
     * Creates a new {@code Local} instance.
     */
    private Local() {
        String sysLang = Locale.getDefault().getLanguage();
        Languages initialLang = Languages.EN;
        for (Languages l : LANGS) {
            if (l.getCode().startsWith(sysLang)) {
                initialLang = l;
                break;
            }
        }
        setLanguage(initialLang);
    }

    /**
     * Sets the language.
     * @param language the language value
     */
    public void setLanguage(Languages language) {
        if (language == null) return;
        String file = "/lang/lang_" + language.getCode() + ".properties";
        try (InputStream input = Local.class.getResourceAsStream(file)) {
            if (input == null) {
                throw new FileNotFoundException("Localization file not found: " + file);
            }

            properties.clear();
            properties.load(new InputStreamReader(input, StandardCharsets.UTF_8));
            this.currentLanguage = language;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Performs the next language operation.
     * @return the next language result
     */
    public Languages nextLanguage() {
        int nextIndex = (currentLanguage.ordinal() + 1) % LANGS.length;
        Languages nextLang = LANGS[nextIndex];
        setLanguage(nextLang);
        return nextLang;
    }

    /**
     * Returns the current language.
     * @return the current language
     */
    public Languages getCurrentLanguage() {
        return currentLanguage;
    }

    /**
     * Performs the t operation.
     * @param key the key value
     * @return the t result
     */
    public String t(String key) {
        return properties.getProperty(key, key);
    }

    /**
     * Performs the f operation.
     * @param s the s value
     * @param args the args value
     * @return the f result
     */
    public String f(String s, Object... args) {
        return MessageFormat.format(t(s), args);
    }

    /**
     * Performs the item operation.
     * @param itemKey the item key value
     * @param tierKey the tier key value
     * @return the item result
     */
    public String item(String itemKey, String tierKey) {
        String itemName = t(itemKey);
        if (tierKey == null || tierKey.isBlank()) {
            return itemName;
        }
        String tierName = t(tierKey);
        return f("item.format.tiered", tierName, itemName);
    }

    /**
     * Formats an enchanted item name according to the active language's
     * grammatical ordering.
     *
     * @param itemKey localization key of the base item
     * @param enchantmentKey common enchantment key without the
     *                       {@code .adjective} suffix
     * @return the localized enchanted item name
     */
    public static String enchanted(String itemKey, String enchantmentKey) {
        String itemName = lang.t(itemKey);
        String adjective = lang.t(enchantmentKey + ".adjective");
        return lang.f("item.enchanted.format", adjective, itemName);
    }
}
