package com.isofarm.utils;

import com.isofarm.data.Singleton;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Properties;

@Utils
@Singleton
public class Local {
    public static final Local lang = new Local();
    private final Properties properties = new Properties();

    private Local() {
        load(Locale.getDefault().getLanguage());
    }

    private void load(String language) {
        String file = "lang/lang-" + language + ".properties";
        try (InputStream input = Local.class.getResourceAsStream(file)) {
            if (input == null) {
                return;
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load localization: " + file, e);
        }
    }

    public String t(String key) {
        return properties.getProperty(key, key);
    }

    public String f(String s, Object... args) {
        return MessageFormat.format(Local.lang.t(s), args);
    }
}