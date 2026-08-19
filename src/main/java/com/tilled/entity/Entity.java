package com.tilled.entity;

import com.tilled.data.DataClass;
import com.tilled.wrld.GameMaster;

@DataClass
public abstract class Entity {
    private final byte id;
    private final String name;

    public Entity(String name) {
        this.id = (byte) (Math.floor((Math.random() * Math.random()) * 100));
        this.name = name;
    }

    public byte getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public abstract void update(float delta);
    public abstract void render(GameMaster gameMaster);
}