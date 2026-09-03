package com.isofarm.graphics;

import org.joml.Vector4f;

/**
 * Provides sprite sheet behavior.
 */
public class SpriteSheet {
    private final Texture texture;
    private final int totalFrames;
    private final int cols;
    private final int rows;

    /**
     * Creates a new {@code SpriteSheet} instance.
     * @param path the path value
     * @param cols the cols value
     * @param rows the rows value
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
     * @return the texture id
     */
    public int getTextureId() {
        return texture.getId();
    }

    /**
     * Performs the bind operation.
     */
    public void bind() {
        texture.bind();
    }

    /**
     * Performs the unbind operation.
     */
    public void unbind() {
        texture.unbind();
    }

    /**
     * Returns the cols.
     * @return the cols
     */
    public int getCols() {
        return cols;
    }

    /**
     * Returns the rows.
     * @return the rows
     */
    public int getRows() {
        return rows;
    }

    /**
     * Returns the frames per row.
     * @return the frames per row
     */
    public int getFramesPerRow() {
        return cols;
    }

    /**
     * Returns the width.
     * @return the width
     */
    public float getWidth() {
        return texture.getWidth();
    }

    /**
     * Returns the height.
     * @return the height
     */
    public float getHeight() {
        return texture.getHeight();
    }

    /**
     * Returns the frame width.
     * @return the frame width
     */
    public int getFrameWidth() {
        return texture.getWidth() / cols;
    }

    /**
     * Returns the frame height.
     * @return the frame height
     */
    public int getFrameHeight() {
        return texture.getHeight() / rows;
    }

    /**
     * Returns the total frames.
     * @return the total frames
     */
    public int getTotalFrames() {
        return totalFrames;
    }

    /**
     * Returns the uvbounds.
     * @param frameIndex the frame index value
     * @return the uvbounds
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
     * Performs the dispose operation.
     */
    public void dispose() {
        texture.dispose();
    }
}