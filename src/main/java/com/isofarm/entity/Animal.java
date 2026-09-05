package com.isofarm.entity;

import com.isofarm.data.DataClass;

/**
 * Encapsulates the state and operations required by animal within the game runtime.
 */
@DataClass
public abstract class Animal extends Entity {

    /**
     * Creates a new {@code Animal} instance.
     * @param name the {@link String} supplied as {@code name}
     */
    public Animal(String name) {
        super(name);
    }

    /**
     * Creates a new {@code Animal} instance.
     */
    public Animal() {
        this(Animal.class.getSimpleName());
    }
}
