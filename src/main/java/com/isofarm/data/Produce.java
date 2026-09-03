package com.isofarm.data;

import com.isofarm.item.Item;

import java.util.Objects;

/**
 * Provides produce behavior.
 */
@DataClass
public class Produce implements Item {
    private final byte id;
    private final String name;
    private final String displayName;
    private final int value;
    private final CropType type;

    /**
     * Creates a new {@code Produce} instance.
     * @param type the type value
     */
    public Produce(CropType type) {
        this.type = type;
        this.id = type.getId();
        this.name = type.getName();
        this.displayName = type.getDisplayName();
        this.value = type.getValue();
    }

    /**
     * Returns the id.
     * @return the id
     */
    @Override
    public byte getId() {
        return id;
    }

    /**
     * Returns the name.
     * @return the name
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Returns the display name.
     * @return the display name
     */
    @Override
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the value.
     * @return the value
     */
    @Override
    public int getValue() {
        return value;
    }

    /**
     * Returns the type.
     * @return the type
     */
    public CropType getType() {
        return type;
    }

    /**
     * Performs the equals operation.
     * @param o the o value
     * @return the equals result
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Produce produce)) return false;
        return type == produce.type;
    }

    /**
     * Checks whether hash code.
     * @return the hash code result
     */
    @Override
    public int hashCode() {
        return Objects.hash(type);
    }

    /**
     * Performs the copy operation.
     * @return the copy result
     */
    @Override
    public Item copy() {
        return new Produce(type);
    }
}
