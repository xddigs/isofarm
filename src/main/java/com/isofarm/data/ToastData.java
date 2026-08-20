package com.isofarm.data;

@DataClass
public enum ToastData {
    SUCCESS((byte) 0, "Success"),
    INFO((byte) 1, "Info"),
    WARNING((byte) 2, "Warning"),
    ERROR((byte) 3, "Error"),
    REWARD((byte) 4, "Reward"),
    PURCHASE((byte) 5, "Purchase"),
    SELL((byte) 6, "Sell");

    private final byte id;
    private final String title;

    ToastData(byte id, String title) {
        this.id = id;
        this.title = title;
    }

    public byte getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public static ToastData fromId(byte id) {
        for (ToastData data : values()) {
            if (data.getId() == id) {
                return data;
            }
        }
        return null;
    }
}
