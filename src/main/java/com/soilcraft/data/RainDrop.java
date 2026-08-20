package com.soilcraft.data;

@DataClass
public class RainDrop {
    private final float velocity;
    private final float length;
    private float x, y, z;

    public RainDrop(float x, float y, float z,
                    float velocity, float length) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.velocity = velocity;
        this.length = length;
    }

    public void update(float delta) {
        y -= velocity * delta;
    }

    public boolean isDead(float groundY) {
        return y + length < groundY;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public float getLength() {
        return length;
    }
}