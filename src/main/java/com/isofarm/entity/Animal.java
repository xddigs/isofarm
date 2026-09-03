package com.isofarm.entity;

import com.isofarm.data.DataClass;

@DataClass
public abstract class Animal extends Entity {

    public Animal(String name) {
        super(name);
    }

    public Animal() {
        this(Animal.class.getSimpleName());
    }
}
