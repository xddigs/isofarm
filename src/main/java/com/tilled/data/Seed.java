package com.tilled.data;

import java.util.Objects;

@DataClass
public class Seed extends Item {
    private final CropType type;
    private final String description;

    public Seed(CropType type) {
        super(type.getId(), "Seed of " + type.getName(), 1, 1);
        this.type = type;
        this.description = type.getDescription();
    }

    public Seed() {
        this(CropType.WHEAT);
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
}