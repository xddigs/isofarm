package com.isofarm.data;

import com.isofarm.item.Item;

import java.util.Objects;

@DataClass
public class Produce implements Item {
    private final byte id;
    private final String name;
    private final String displayName;
    private final int value;
    private final CropType type;

    public Produce(CropType type) {
        this.type = type;
        this.id = type.getId();
        this.name = type.getName();
        this.displayName = type.getDisplayName();
        this.value = type.getValue();
    }

    @Override
    public byte getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public int getValue() {
        return value;
    }

    public CropType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Produce produce)) return false;
        return type == produce.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }

    @Override
    public Item copy() {
        return new Produce(type);
    }
}
