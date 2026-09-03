package com.isofarm.service;

import com.isofarm.utils.Settings;
import com.isofarm.utils.ToastFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Provides game rules behavior.
 */
@SuppressWarnings("all")
public final class GameRules {
    private static final Map<String, Object> RULES = new LinkedHashMap<>();
    private static final Logger log = LoggerFactory.getLogger(GameRules.class);

    static {
        RULES.put("doKeepInventory", Settings.doKeepInventory());
        RULES.put("doEnableMotions", Settings.doEnableMotions());
        RULES.put("renderDistance", Settings.getRenderDistance());
        RULES.put("fov", Settings.getFov());
        RULES.put("unloadMargin", Settings.getUnloadMargin());
        RULES.put("doEnableShadows", Settings.doEnableShadows());
        RULES.put("maxInteractionDistance", Settings.getMaxInteractionDistance());
    }

    /**
     * Creates a new {@code GameRules} instance.
     */
    private GameRules() {}

    /**
     * Performs the exists operation.
     * @param rule the rule value
     * @return the exists result
     */
    public static boolean exists(String rule) {
        return RULES.containsKey(rule);
    }

    /**
     * Returns get.
     * @param rule the rule value
     * @return the get result
     */
    public static Object get(String rule) {
        return RULES.get(rule);
    }

    /**
     * Returns the rules.
     * @return the rules
     */
    public static Map<String, Object> getRules() {
        return Map.copyOf(RULES);
    }

    /**
     * Returns the boolean.
     * @param rule the rule value
     * @return the boolean
     */
    public static boolean getBoolean(String rule) {
        Object value = RULES.get(rule);

        if (!(value instanceof Boolean booleanValue)) {
            throw new IllegalArgumentException(
                    "GameRule is not boolean: " + rule
            );
        }

        return booleanValue;
    }

    /**
     * Returns the int.
     * @param rule the rule value
     * @return the int
     */
    public static int getInt(String rule) {
        Object value = RULES.get(rule);

        if (!(value instanceof Integer intValue)) {
            throw new IllegalArgumentException(
                    "GameRule is not integer: " + rule
            );
        }

        return intValue;
    }

    /**
     * Returns the float.
     * @param rule the rule value
     * @return the float
     */
    public static float getFloat(String rule) {
        Object value = RULES.get(rule);

        if (!(value instanceof Float floatValue)) {
            throw new IllegalArgumentException(
                    "GameRule is not float: " + rule
            );
        }

        return floatValue;
    }

    /**
     * Sets set.
     * @param rule the rule value
     * @param value the value value
     */
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

    /**
     * Performs the apply operation.
     * @param rule the rule value
     * @param value the value value
     */
    private static void apply(String rule, Object value) {
        try {
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
        } catch (ClassCastException e) {
            ToastFactory.error(String.format("Invalid value for %s: %s", rule, value));
        }
    }
}