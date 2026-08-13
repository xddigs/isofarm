package com.sfarm4j.graphics;

import org.joml.Vector3f;

public class Sunlight {
    private final Vector3f direction;
    private final Vector3f color;
    private float intensity;

    public Sunlight(Vector3f direction, Vector3f color, float intensity) {
        this.direction = new Vector3f(direction).normalize();
        this.color = color;
        this.intensity = intensity;
    }

    public Sunlight(Vector3f direction) {
        this(direction, new Vector3f(1.0f, 1.0f, 1.0f), 1.0f);
    }

    public Vector3f getDirection() { return direction; }
    public Vector3f getColor() { return color; }
    public float getIntensity() { return intensity; }

    public void setDirection(float x, float y, float z) {
        this.direction.set(x, y, z).normalize();
    }
}