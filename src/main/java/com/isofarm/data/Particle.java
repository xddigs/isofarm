package com.isofarm.data;

import com.isofarm.graphics.SpriteSheet;
import org.joml.Vector2f;

/**
 * Provides particle behavior.
 */
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

    /**
     * Creates a new {@code Particle} instance.
     * @param x the x value
     * @param y the y value
     * @param z the z value
     * @param vx the vx value
     * @param vy the vy value
     * @param vz the vz value
     * @param size the size value
     * @param maxLife the max life value
     * @param uvOffset the uv offset value
     * @param uvScale the uv scale value
     * @param texture the texture value
     */
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

    /**
     * Returns the texture.
     * @return the texture
     */
    public SpriteSheet getTexture() {
        return texture;
    }

    /**
     * Updates the current state.
     * @param delta the delta value
     */
    public void update(float delta) {
        life += delta;

        vy -= 9.81f * delta;
        vx *= 0.98f;
        vz *= 0.98f;

        x += vx * delta;
        y += vy * delta;
        z += vz * delta;
    }

    /**
     * Checks whether the dead condition is met.
     * @return {@code true} if dead; otherwise {@code false}
     */
    public boolean isDead() {
        return life >= maxLife;
    }

    /**
     * Returns the alpha.
     * @return the alpha
     */
    public float getAlpha() {
        return 1.0f - (life / maxLife);
    }

    /**
     * Returns the x.
     * @return the x
     */
    public float getX() {
        return x;
    }

    /**
     * Returns the y.
     * @return the y
     */
    public float getY() {
        return y;
    }

    /**
     * Returns the z.
     * @return the z
     */
    public float getZ() {
        return z;
    }

    /**
     * Returns the vx.
     * @return the vx
     */
    public float getVx() {
        return vx;
    }

    /**
     * Returns the vy.
     * @return the vy
     */
    public float getVy() {
        return vy;
    }

    /**
     * Returns the vz.
     * @return the vz
     */
    public float getVz() {
        return vz;
    }

    /**
     * Returns the life.
     * @return the life
     */
    public float getLife() {
        return life;
    }

    /**
     * Returns the max life.
     * @return the max life
     */
    public float getMaxLife() {
        return maxLife;
    }

    /**
     * Returns the size.
     * @return the size
     */
    public float getSize() {
        return size;
    }

    /**
     * Returns the uv offset.
     * @return the uv offset
     */
    public Vector2f getUvOffset() {
        return uvOffset;
    }

    /**
     * Returns the uv scale.
     * @return the uv scale
     */
    public Vector2f getUvScale() {
        return uvScale;
    }
}