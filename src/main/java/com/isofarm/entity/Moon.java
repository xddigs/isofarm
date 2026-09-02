package com.isofarm.entity;

import com.isofarm.data.BlockPos;
import com.isofarm.data.RenderPass;
import com.isofarm.wrld.GameMaster;
import org.joml.Vector3f;

public class Moon extends Entity {
    private final Vector3f direction;
    private final Vector3f color;
    private float intensity;

    public Moon(String name) {
        super(name);
        this.direction = new Vector3f();
        this.color = new Vector3f(0.45f, 0.55f, 0.85f);
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
    public void update(BlockPos blockPos, float timeOfDay) {
        float angle = ((timeOfDay - 18.0f) / 24.0f) * ((float) Math.PI * 2.0f);
        float x = (float) Math.cos(angle);
        float y = (float) Math.sin(angle);
        direction.set(x, -y, -0.15f).normalize();
        if (timeOfDay >= 20.0f && timeOfDay < 22.0f) {
            float t = (timeOfDay - 20.0f) / 2.0f;
            intensity = t * 0.20f;
        } else if (timeOfDay >= 22.0f || timeOfDay < 5.0f) {
            intensity = 0.20f;
        } else if (timeOfDay >= 5.0f && timeOfDay < 7.0f) {
            float t = (7.0f - timeOfDay) / 2.0f;
            intensity = t * 0.20f;
        } else {
            intensity = 0.0f;
        }
    }

    @Override
    public void render(GameMaster gameMaster, RenderPass pass) {}
}