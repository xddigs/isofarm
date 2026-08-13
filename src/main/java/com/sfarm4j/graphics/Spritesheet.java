package com.sfarm4j.graphics;

public class Spritesheet {
    private final Texture texture;
    private final int totalFrames;

    public Spritesheet(String path, int totalFrames) {
        this.texture = new Texture(path);
        this.totalFrames = totalFrames;
    }

    public void bind() {
        texture.bind();
    }

    public void unbind() {
        texture.unbind();
    }

    public int getTotalFrames() {
        return totalFrames;
    }

    public void dispose() {
        texture.dispose();
    }
}