package com.soilcraft.data;

import com.soilcraft.graphics.SpriteSheet;
import org.joml.Vector2f;

@DataClass
public class Particle {
    private float x, y, z;
    private float vx, vy, vz;
    private float life;
    private final float maxLife;
    private final float size;

    private final Vector2f uvOffset;
    private final Vector2f uvScale;
    private final SpriteSheet texture;

    public Particle(float x, float y, float z, float vx, float vy, float vz,
                    float size, float maxLife, Vector2f uvOffset, Vector2f uvScale,
                    SpriteSheet texture) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.vx = vx;
        this.vy = vy;
        this.vz = vz;
        this.size = size;
        this.maxLife = maxLife;
        this.uvOffset = uvOffset;
        this.uvScale = uvScale;
        this.texture = texture;
        this.life = 0;
    }

    public SpriteSheet getTexture() {
        return texture;
    }

    public void update(float delta) {
        life += delta;

        vy -= 9.81f * delta;
        vx *= 0.98f;
        vz *= 0.98f;

        x += vx * delta;
        y += vy * delta;
        z += vz * delta;
    }

    public boolean isDead() {
        return life >= maxLife;
    }

    public float getAlpha() {
        return 1.0f - (life / maxLife);
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

    public float getVx() {
        return vx;
    }

    public float getVy() {
        return vy;
    }

    public float getVz() {
        return vz;
    }

    public float getLife() {
        return life;
    }

    public float getMaxLife() {
        return maxLife;
    }

    public float getSize() {
        return size;
    }

    public Vector2f getUvOffset() {
        return uvOffset;
    }

    public Vector2f getUvScale() {
        return uvScale;
    }
}