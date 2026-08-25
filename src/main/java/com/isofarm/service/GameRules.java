package com.isofarm.service;

import com.isofarm.utils.Settings;

import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings("all")
public final class GameRules {
    private static final Map<String, Object> RULES = new LinkedHashMap<>();

    static {
        RULES.put("doKeepInventory", Settings.doKeepInventory());
        RULES.put("doEnableMotions", Settings.doEnableMotions());
        RULES.put("isOrthographic", Settings.isOrthographic());
        RULES.put("renderDistance", Settings.getRenderDistance());
        RULES.put("fov", Settings.getFov());
        RULES.put("unloadMargin", Settings.getUnloadMargin());
        RULES.put("doEnableShadows", Settings.doEnableShadows());
        RULES.put("maxInteractionDistance", Settings.getMaxInteractionDistance());
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
            case "doKeepInventory" -> {
                Settings.setDoKeepInventory((Boolean) value);
            }

            case "fov" -> {
                Settings.setFov((Float) value);
            }

            case "doEnableMotions" -> {
                Settings.setDoEnableMotions((Boolean) value);
            }

            case "isOrthographic" -> {
                Settings.setOrthographic((Boolean) value);
            }

            case "renderDistance" -> {
                Settings.setRenderDistance((Integer) value);
            }

            case "unloadMargin" -> {
                Settings.setUnloadMargin((Integer) value);
            }

            case "doEnableShadows" -> {
                Settings.setDoEnableShadows((Boolean) value);
            }

            case "maxInteractionDistance" -> {
                Settings.setMaxInteractionDistance((Float) value);
            }
        }
    }
}