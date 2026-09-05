package com.isofarm.graphics;

import org.joml.Vector4f;

/**
 * Encapsulates the state and operations required by sprite sheet within the game runtime.
 */
public class SpriteSheet {
    private final Texture texture;
    private final int totalFrames;
    private final int cols;
    private final int rows;

    /**
     * Creates a new {@code SpriteSheet} instance.
     * @param path the {@link String} supplied as {@code path}
     * @param cols the {@code int} supplied as {@code cols}
     * @param rows the {@code int} supplied as {@code rows}
     */
    public SpriteSheet(String path, int cols, int rows) {
        if (cols <= 0) {
            throw new IllegalArgumentException("cols must be greater than 0");
        }

        if (rows <= 0) {
            throw new IllegalArgumentException("rows must be greater than 0");
        }

        this.texture = new Texture(path);
        this.cols = cols;
        this.rows = rows;
        this.totalFrames = cols * rows;
    }

    /**
     * Returns the texture id.
     * @return {@code int}; the texture id
     */
    public int getTextureId() {
        return texture.getId();
    }

    /**
     * Binds this object to the active runtime context.
     */
    public void bind() {
        texture.bind();
    }

    /**
     * Unbinds this object from the active runtime context.
     */
    public void unbind() {
        texture.unbind();
    }

    /**
     * Returns the cols.
     * @return {@code int}; the cols
     */
    public int getCols() {
        return cols;
    }

    /**
     * Returns the rows.
     * @return {@code int}; the rows
     */
    public int getRows() {
        return rows;
    }

    /**
     * Returns the frames per row.
     * @return {@code int}; the frames per row
     */
    public int getFramesPerRow() {
        return cols;
    }

    /**
     * Returns the width.
     * @return {@code float}; the width
     */
    public float getWidth() {
        return texture.getWidth();
    }

    /**
     * Returns the height.
     * @return {@code float}; the height
     */
    public float getHeight() {
        return texture.getHeight();
    }

    /**
     * Returns the frame width.
     * @return {@code int}; the frame width
     */
    public int getFrameWidth() {
        return texture.getWidth() / cols;
    }

    /**
     * Returns the frame height.
     * @return {@code int}; the frame height
     */
    public int getFrameHeight() {
        return texture.getHeight() / rows;
    }

    /**
     * Returns the total frames.
     * @return {@code int}; the total frames
     */
    public int getTotalFrames() {
        return totalFrames;
    }

    /**
     * Returns the uvbounds.
     * @param frameIndex the {@code int} supplied as {@code frameIndex}
     * @return the {@link Vector4f} representing the uvbounds
     */
    public Vector4f getUVBounds(int frameIndex) {
        frameIndex = Math.clamp(frameIndex, 0, totalFrames - 1);
        int column = frameIndex % cols;
        int row = frameIndex / cols;

        float frameWidth = 1.0f / cols;
        float frameHeight = 1.0f / rows;

        float uMin = column * frameWidth;
        float uMax = uMin + frameWidth;

        float vMax = 1.0f - row * frameHeight;
        float vMin = vMax - frameHeight;
        return new Vector4f(uMin, vMin, uMax, vMax);
    }

    /**
     * Releases the resources associated with this object.
     */
    public void dispose() {
        texture.dispose();
    }
}