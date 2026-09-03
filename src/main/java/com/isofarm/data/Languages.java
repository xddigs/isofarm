package com.isofarm.data;

import com.isofarm.utils.Local;

/**
 * Enumerates the supported languages values.
 */
@DataClass
public enum Languages {
    EN("en_US", "engine.lang.english-US"),
    ES("es_ES", "engine.lang.spanish-ES");

    private final String code;
    private final String name;

    /**
     * Creates a new {@code Languages} instance.
     * @param code the code value
     * @param name the name value
     */
    Languages(String code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * Returns the code.
     * @return the code
     */
    public String getCode() {
        return code;
    }

    /**
     * Returns the name.
     * @return the name
     */
    public String getName() {
        return Local.lang.t(name);
    }
}
