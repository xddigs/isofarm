package com.isofarm.entity;

import com.isofarm.data.DataClass;

/**
 * Provides animal behavior.
 */
@DataClass
public abstract class Animal extends Entity {

    /**
     * Creates a new {@code Animal} instance.
     * @param name the name value
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
