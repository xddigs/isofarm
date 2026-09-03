package com.isofarm.data;

import com.isofarm.utils.Local;

@DataClass
public enum Languages {
    EN("en_US", "engine.lang.english-US"),
    ES("es_ES", "engine.lang.spanish-ES");

    private final String code;
    private final String name;

    Languages(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return Local.lang.t(name);
    }
}
