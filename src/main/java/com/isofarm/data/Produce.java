package com.isofarm.data;

import java.util.Objects;

@DataClass
public class Produce extends Item {
    private final CropType type;

    public Produce(CropType type) {
        super(type.getId(), type.getName(), type.getValue());
        this.type = type;
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
