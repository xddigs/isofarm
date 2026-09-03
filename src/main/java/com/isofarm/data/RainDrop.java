package com.isofarm.data;

/**
 * Provides rain drop behavior.
 */
@DataClass
public class RainDrop {
    private final float velocity;
    private final float length;
    private float x, y, z;

    /**
     * Creates a new {@code RainDrop} instance.
     * @param x the x value
     * @param y the y value
     * @param z the z value
     * @param velocity the velocity value
     * @param length the length value
     */
    public RainDrop(float x, float y, float z,
                    float velocity, float length) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.velocity = velocity;
        this.length = length;
    }

    /**
     * Updates the current state.
     * @param delta the delta value
     */
    public void update(float delta) {
        y -= velocity * delta;
    }

    /**
     * Checks whether the dead condition is met.
     * @param groundY the ground y value
     * @return {@code true} if dead; otherwise {@code false}
     */
    public boolean isDead(float groundY) {
        return y + length < groundY;
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
     * Returns the length.
     * @return the length
     */
    public float getLength() {
        return length;
    }
}