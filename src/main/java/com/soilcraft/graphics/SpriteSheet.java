package com.soilcraft.graphics;

public class SpriteSheet {
    private final Texture texture;
    private final int totalFrames;

    public SpriteSheet(String path, int totalFrames) {
        this.texture = new Texture(path);
        this.totalFrames = totalFrames;
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

    public float getWidth() {
        return (float) texture.getWidth();
    }

    public float getHeight() {
        return (float) texture.getHeight();
    }

    public int getTotalFrames() {
        return totalFrames;
    }

    public void dispose() {
        texture.dispose();
    }
}