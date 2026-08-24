package com.isofarm.graphics;

import org.joml.Vector4f;

public class SpriteSheet {

    private final Texture texture;
    private final int totalFrames;
    private final int cols;
    private final int rows;

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

    public int getTextureId() {
        return texture.getId();
    }

    public void bind() {
        texture.bind();
    }

    public void unbind() {
        texture.unbind();
    }

    public int getCols() {
        return cols;
    }

    public int getRows() {
        return rows;
    }

    public int getFramesPerRow() {
        return cols;
    }

    public float getWidth() {
        return texture.getWidth();
    }

    public float getHeight() {
        return texture.getHeight();
    }

    public int getFrameWidth() {
        return texture.getWidth() / cols;
    }

    public int getFrameHeight() {
        return texture.getHeight() / rows;
    }

    public int getTotalFrames() {
        return totalFrames;
    }

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

    public void dispose() {
        texture.dispose();
    }
}