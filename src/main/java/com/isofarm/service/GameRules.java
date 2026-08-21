package com.isofarm.service;

import com.isofarm.utils.Settings;

import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings("all")
public final class GameRules {
    private static final Map<String, Object> RULES = new LinkedHashMap<>();

    static {
        RULES.put("doEnableMotions", Settings.doEnableMotions);
        RULES.put("isOrthographic", Settings.isOrthographic);
        RULES.put("renderDistance", Settings.renderDistance);
        RULES.put("unloadMargin", Settings.unloadMargin);
        RULES.put("maxInteractionDistance", Settings.maxInteractionDistance);
    }

    private GameRules() {}

    public static boolean exists(String rule) {
        return RULES.containsKey(rule);
    }

    public static Object get(String rule) {
        return RULES.get(rule);
    }

    public static Map<String, Object> getRules() {
        return Map.copyOf(RULES);
    }

    public static boolean getBoolean(String rule) {
        Object value = RULES.get(rule);

        if (!(value instanceof Boolean booleanValue)) {
            throw new IllegalArgumentException(
                    "GameRule is not boolean: " + rule
            );
        }

        return booleanValue;
    }

    public static int getInt(String rule) {
        Object value = RULES.get(rule);

        if (!(value instanceof Integer intValue)) {
            throw new IllegalArgumentException(
                    "GameRule is not integer: " + rule
            );
        }

        return intValue;
    }

    public static float getFloat(String rule) {
        Object value = RULES.get(rule);

        if (!(value instanceof Float floatValue)) {
            throw new IllegalArgumentException(
                    "GameRule is not float: " + rule
            );
        }

        return floatValue;
    }

    public static void set(String rule, Object value) {
        if (!RULES.containsKey(rule)) {
            throw new IllegalArgumentException(
                    "Unknown gamerule: " + rule
            );
        }

        Object current = RULES.get(rule);

        if (!current.getClass().equals(value.getClass())) {
            throw new IllegalArgumentException(
                    "Invalid value type for gamerule: " + rule
            );
        }

        RULES.put(rule, value);
        apply(rule, value);
    }

    private static void apply(String rule, Object value) {
        switch (rule) {
            case "doEnableMotions" -> {
                Settings.doEnableMotions = (Boolean) value;
            }

            case "isOrthographic" -> {
                Settings.isOrthographic = (Boolean) value;
            }

            case "renderDistance" -> {
                Settings.renderDistance = (Integer) value;
            }

            case "unloadMargin" -> {
                Settings.unloadMargin = (Integer) value;
            }

            case "maxInteractionDistance" -> {
                Settings.maxInteractionDistance = (Float) value;
            }
        }
    }
}