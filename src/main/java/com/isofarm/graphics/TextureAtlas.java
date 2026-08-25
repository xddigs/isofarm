package com.isofarm.graphics;

import org.joml.Vector2f;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;

public class TextureAtlas {
    private final int textureId;
    private final Map<String, TextureRegion> regions = new HashMap<>();
    public record TextureRegion(Vector2f uvMin, Vector2f uvMax, Vector2f scale, Vector2f offset) {}

    public TextureAtlas(List<String> imagePaths, int tileWidth, int tileHeight) {
        int count = imagePaths.size();
        int cols = (int) Math.ceil(Math.sqrt(count));
        int rows = (int) Math.ceil((double) count / cols);

        int atlasWidth = cols * tileWidth;
        int atlasHeight = rows * tileHeight;

        ByteBuffer atlasBuffer = BufferUtils.createByteBuffer(atlasWidth * atlasHeight * 4);
        STBImage.stbi_set_flip_vertically_on_load(true);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer comp = stack.mallocInt(1);

            for (int i = 0; i < count; i++) {
                String path = imagePaths.get(i);
                ByteBuffer image = STBImage.stbi_load(path, w, h, comp, 4);
                if (image == null) {
                    throw new RuntimeException("Failed to load texture: " + path + " - " + STBImage.stbi_failure_reason());
                }

                int col = i % cols;
                int row = i / cols;
                int offsetX = col * tileWidth;
                int offsetY = row * tileHeight;

                for (int y = 0; y < tileHeight; y++) {
                    int srcPos = y * tileWidth * 4;
                    int destPos = ((offsetY + y) * atlasWidth + offsetX) * 4;
                    for (int x = 0; x < tileWidth * 4; x++) {
                        atlasBuffer.put(destPos + x, image.get(srcPos + x));
                    }
                }

                STBImage.stbi_image_free(image);
                float uMin = (float) offsetX / atlasWidth;
                float vMin = (float) offsetY / atlasHeight;
                float uMax = (float) (offsetX + tileWidth) / atlasWidth;
                float vMax = (float) (offsetY + tileHeight) / atlasHeight;

                regions.put(path, new TextureRegion(
                    new Vector2f(uMin, vMin),
                    new Vector2f(uMax, vMax),
                    new Vector2f((float) tileWidth / atlasWidth, (float) tileHeight / atlasHeight),
                    new Vector2f(uMin, vMin)
                ));
            }
        }

        textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureId);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, atlasWidth, atlasHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, atlasBuffer);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public void bind() {
        glBindTexture(GL_TEXTURE_2D, textureId);
    }

    public void unbind() {
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public TextureRegion getRegion(String path) {
        return regions.get(path);
    }

    public void dispose() {
        glDeleteTextures(textureId);
    }
}