package com.isofarm.data;

@DataClass
public enum MaterialID {
    STICK((byte) 0, "Stick", ToolType.values(), 1);

    private final byte id;
    private final String name;
    private final Object[] canBeCraftedInto;
    private final int value;

    MaterialID(byte id, String name, Object[] canBeCraftedInto, int value) {
        this.id = id;
        this.name = name;
        this.canBeCraftedInto = canBeCraftedInto;
        this.value = value;
    }

    public byte getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Object[] getCanBeCraftedInto() {
        return canBeCraftedInto;
    }

    public int getValue() {
        return value;
    }
}
