package com.isofarm.data;

/**
 * Enumerates the supported toast data values.
 */
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

    /**
     * Creates a new {@code ToastData} instance.
     * @param id the id value
     * @param title the title value
     */
    ToastData(byte id, String title) {
        this.id = id;
        this.title = title;
    }

    /**
     * Returns the id.
     * @return the id
     */
    public byte getId() {
        return id;
    }

    /**
     * Returns the title.
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Performs the from id operation.
     * @param id the id value
     * @return the from id result
     */
    public static ToastData fromId(byte id) {
        for (ToastData data : values()) {
            if (data.getId() == id) {
                return data;
            }
        }
        return null;
    }
}
