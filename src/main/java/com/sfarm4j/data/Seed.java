package com.sfarm4j.data;

import java.util.Objects;

@DataClass
public class Seed extends Item {
    private final CropType type;

    public Seed(CropType type) {
        super(type.getId(), type.getName() + " Seed", 1, 1);
        this.type = type;
    }

    public Seed() {
        this(CropType.WHEAT);
    }

    public CropType getType() {
        return type;
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