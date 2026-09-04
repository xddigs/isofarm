package com.isofarm.data;

import com.isofarm.item.Item;
import com.isofarm.utils.Local;

import java.util.Locale;
import java.util.Objects;

/**
 * Provides seed behavior.
 */
@DataClass
public class Seed implements Item, Plantable {
    private final CropType type;
    private final byte id;
    private final String displayName;
    private final String name;
    private final int value;

    /**
     * Creates a new {@code Seed} instance.
     * @param type the type value
     */
    public Seed(CropType type) {
        this.type = type;
        this.id = type.getId();
        this.displayName = getDisplayName(type.getName().toLowerCase(Locale.ROOT));
        this.name = type.getName() + "_seed";
        this.value = type.getValue();
    }

    /**
     * Creates a new {@code Seed} instance.
     */
    public Seed() {
        this(CropType.WHEAT);
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
    @Override
    public CropType getType() {
        return type;
    }

    /**
     * Returns the display name.
     * @param name the name value
     * @return the display name
     */
    private String getDisplayName(String name) {
        return Local.lang.t("crop." + name + ".seed");
    }

    /**
     * Performs the equals operation.
     * @param o the o value
     * @return the equals result
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Seed seed)) return false;
        return type == seed.type;
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
        return new Seed(type);
    }
}
