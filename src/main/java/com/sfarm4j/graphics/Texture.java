package com.sfarm4j.graphics;

import org.lwjgl.system.MemoryStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.stb.STBImage.*;

public class Texture {
    private static final Logger log = LoggerFactory.getLogger(Texture.class);
    private final int id;
    private final int width;
    private final int height;

    public Texture(String filePath) {
        stbi_set_flip_vertically_on_load(true);
        int textureWidth;
        int textureHeight;
        ByteBuffer imageBuffer;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            IntBuffer pChannels = stack.mallocInt(1);

            imageBuffer = stbi_load(filePath, pWidth, pHeight, pChannels, 4);
            if (imageBuffer == null) {
                throw new RuntimeException("Failed to load texture file " +
                        "[" + filePath + "]: " + stbi_failure_reason());
            }

            textureWidth = pWidth.get(0);
            textureHeight = pHeight.get(0);
        }

        this.width = textureWidth;
        this.height = textureHeight;

        this.id = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, this.id);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        glTexImage2D(
                GL_TEXTURE_2D,
                0,
                GL_RGBA,
                width,
                height,
                0,
                GL_RGBA,
                GL_UNSIGNED_BYTE,
                imageBuffer
        );

        stbi_image_free(imageBuffer);
        glBindTexture(GL_TEXTURE_2D, 0); // Unbind

        log.info("Texture loaded successfully [{}] ({}x{} px, ID: {})",
                filePath, width, height, id);
    }

    public void bind() {
        glBindTexture(GL_TEXTURE_2D, id);
    }

    public void unbind() {
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public void cleanup() {
        glDeleteTextures(id);
        log.info("Texture resource deleted (ID: {})", id);
    }

    public int getId() {
        return id;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}