package com.isofarm.data;

import com.isofarm.item.Item;
import com.isofarm.utils.Local;

import java.util.Objects;

@DataClass
public class Seed implements Item {
    private final CropType type;
    private final byte id;
    private final String displayName;
    private final String name;
    private final int value;

    public Seed(CropType type) {
        this.type = type;
        this.id = type.getId();
        this.displayName = getDisplayName(type.getName());
        this.name = type.getName() + "_seed";
        this.value = type.getValue();
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

    private String getDisplayName(String name) {
        return Local.lang.t("crop." + name + ".seed");
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