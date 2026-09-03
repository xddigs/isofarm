package com.isofarm.data;

import com.isofarm.item.Item;

import java.util.Objects;

@DataClass
public class Seed implements Item {
    private final CropType type;
    private final byte id;
    private final String name;
    private final int value;
    private final String description;

    public Seed(CropType type) {
        this.type = type;
        this.id = type.getId();
        this.name = type.getName() + " Seed";
        this.value = type.getValue();
        this.description = type.getDescription();
    }

    public Seed() {
        this(CropType.WHEAT);
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
    public int getValue() {
        return value;
    }

    public CropType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Seed seed)) return false;
        return type == seed.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }

    @Override
    public Item copy() {
        return new Seed(type);
    }
}