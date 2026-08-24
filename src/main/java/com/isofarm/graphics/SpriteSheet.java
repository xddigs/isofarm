package com.isofarm.graphics;

public class SpriteSheet {
    private final Texture texture;
    private final int totalFrames;
    private final int cols;
    private final int rows;

    public SpriteSheet(String path, int cols, int rows) {
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
        return (float) texture.getWidth();
    }

    public float getHeight() {
        return (float) texture.getHeight();
    }

    public int getFrameWidth() {
        return texture.getWidth() / totalFrames;
    }

    public int getFrameHeight() {
        return texture.getHeight() / totalFrames;
    }

    public int getTotalFrames() {
        return totalFrames;
    }

    public void dispose() {
        texture.dispose();
    }
}