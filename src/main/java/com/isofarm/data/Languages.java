package com.isofarm.data;

@DataClass
public enum Languages {
    EN("en_US"),
    ES("es_ES");

    private final String code;

    Languages(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
