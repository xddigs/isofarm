package com.tilled.entity;

import com.tilled.wrld.GameMaster;
import org.joml.Vector3f;

public class Sun extends Entity {
    private final Vector3f direction;
    private final Vector3f color;
    private float intensity;

    public Sun(String name) {
        super(name);
        this.direction = new Vector3f();
        this.color = new Vector3f(1.0f, 0.95f, 0.85f);
        this.intensity = 0.0f;
    }

    public Vector3f getDirection() {
        return direction;
    }

    public Vector3f getColor() {
        return color;
    }

    public float getIntensity() {
        return intensity;
    }

    @Override
    public void update(float timeOfDay) {
        float angle = ((timeOfDay - 6.0f) / 24.0f) * ((float) Math.PI * 2.0f);
        float x = (float) Math.cos(angle);
        float y = (float) Math.sin(angle);
        direction.set(x, -y, 0.25f).normalize();

        if (timeOfDay >= 5.0f && timeOfDay < 7.0f) {
            float t = (timeOfDay - 5.0f) / 2.0f;
            intensity = 0.15f + (t * 0.85f);
        } else if (timeOfDay >= 7.0f && timeOfDay < 19.0f) {
            intensity = 1.0f;
        } else if (timeOfDay >= 19.0f && timeOfDay < 22.0f) {
            float t = (timeOfDay - 19.0f) / 3.0f;
            intensity = 1.0f - (t * 0.9f);
        } else {
            intensity = 0.0f;
        }
    }

    @Override
    public void render(GameMaster gameMaster) {

    }
}