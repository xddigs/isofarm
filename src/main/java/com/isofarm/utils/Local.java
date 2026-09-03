package com.isofarm.utils;

import com.isofarm.data.Languages;
import com.isofarm.data.Singleton;
import com.isofarm.data.Tier;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Properties;

@Utils
@Singleton
public class Local {
    private static final Languages[] LANGS = Languages.values();
    public static final Local lang = new Local();
    private final Properties properties = new Properties();
    private Languages currentLanguage;

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

    public void setLanguage(Languages language) {
        if (language == null) return;

        String file = "/lang/lang_" + language.getCode() + ".properties";
        try (InputStream input = Local.class.getResourceAsStream(file)) {
            if (input == null) {
                throw new FileNotFoundException("Localization file not found: " + file);
            }
            properties.clear();
            properties.load(input);
            this.currentLanguage = language;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load localization: " + file, e);
        }
    }

    public Languages nextLanguage() {
        int nextIndex = (currentLanguage.ordinal() + 1) % LANGS.length;
        Languages nextLang = LANGS[nextIndex];
        setLanguage(nextLang);
        return nextLang;
    }

    public Languages getCurrentLanguage() {
        return currentLanguage;
    }

    public String t(String key) {
        return properties.getProperty(key, key);
    }

    public String f(String s, Object... args) {
        return MessageFormat.format(t(s), args);
    }

    public String item(String itemKey, String tierKey) {
        String itemName = t(itemKey);
        if (tierKey == null || tierKey.isBlank()) {
            return itemName;
        }
        String tierName = t(tierKey);
        return f("item.format.tiered", tierName, itemName);
    }
}