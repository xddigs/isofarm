package com.isofarm.graphics;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * Provides framebuffer behavior.
 */
public class Framebuffer {
    private final int fboId;
    private final int textureId;
    private final int width;
    private final int height;
    private final int depthBufferId;

    /**
     * Creates a new {@code Framebuffer} instance.
     * @param width the width value
     * @param height the height value
     */
    public Framebuffer(int width, int height) {
        this.width = width;
        this.height = height;

        fboId = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, fboId);

        textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureId);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, 0);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, textureId, 0);

        depthBufferId = glGenRenderbuffers();
        glBindRenderbuffer(GL_RENDERBUFFER, depthBufferId);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, width, height);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, depthBufferId);

        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw new IllegalStateException("Framebuffer is not complete");
        }

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    /**
     * Performs the bind operation.
     */
    public void bind() {
        glBindFramebuffer(GL_FRAMEBUFFER, fboId);
        glViewport(0, 0, width, height);
    }

    /**
     * Performs the unbind operation.
     * @param windowWidth the window width value
     * @param windowHeight the window height value
     */
    public void unbind(int windowWidth, int windowHeight) {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0, 0, windowWidth, windowHeight);
    }

    /**
     * Returns the texture id.
     * @return the texture id
     */
    public int getTextureId() {
        return textureId;
    }

    /**
     * Performs the dispose operation.
     */
    public void dispose() {
        glDeleteTextures(textureId);
        glDeleteRenderbuffers(depthBufferId);
        glDeleteFramebuffers(fboId);
    }
}