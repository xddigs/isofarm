package com.sfarm4j.graphics;

import org.lwjgl.system.MemoryStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.stb.STBImage.*;

public class Texture {
    private static final Logger log = LoggerFactory.getLogger(Texture.class);
    private final int id;
    private final int width;
    private final int height;

    public Texture(String resourcePath) {
        stbi_set_flip_vertically_on_load(true);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            IntBuffer pChannels = stack.mallocInt(1);

            byte[] rawData;
            try (InputStream in = Texture.class.getClassLoader()
                    .getResourceAsStream(resourcePath)) {
                if (in == null) {
                    throw new IllegalArgumentException("Resource not found" +
                            " on classpath: " + resourcePath);
                }
                rawData = in.readAllBytes();
            } catch (Exception e) {
                throw new RuntimeException("Failed to read texture file " +
                        "[" + resourcePath + "]", e);
            }

            ByteBuffer rawBuffer = stack.malloc(rawData.length);
            rawBuffer.put(rawData).flip();

            ByteBuffer imageBuffer = stbi_load_from_memory(rawBuffer,
                    pWidth, pHeight, pChannels, 4);
            if (imageBuffer == null) {
                throw new RuntimeException("Failed to decode texture " +
                        "[" + resourcePath + "]: " + stbi_failure_reason());
            }

            this.width = pWidth.get(0);
            this.height = pHeight.get(0);

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
            glBindTexture(GL_TEXTURE_2D, 0);
        }

        log.info("Texture loaded successfully [{}] ({}x{} px, ID: {})",
                resourcePath, width, height, id);
    }

    public void bind() {
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, id);
    }

    public void unbind() {
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public void dispose() {
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