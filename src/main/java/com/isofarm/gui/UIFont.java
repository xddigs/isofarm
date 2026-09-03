package com.isofarm.gui;

import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.stb.STBTruetype;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13C.glActiveTexture;
import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.system.MemoryUtil.memFree;

/**
 * Provides uifont behavior.
 */
@SuppressWarnings("all")
public class UIFont {
    private static final int FIRST_CHAR = 32;
    private static final int LAST_CHAR = 255;
    private static final int CHAR_COUNT = LAST_CHAR - FIRST_CHAR + 1;

    private final String path;
    private final float size;
    private final int atlasWidth;
    private final int atlasHeight;
    private final int textureId;
    private final STBTTBakedChar.Buffer glyphs;

    /**
     * Creates a new {@code UIFont} instance.
     * @param path the path value
     * @param size the size value
     */
    public UIFont(String path, float size) {
        this(path, size, 1024, 1024);
    }

    /**
     * Creates a new {@code UIFont} instance.
     * @param path the path value
     * @param size the size value
     * @param atlasWidth the atlas width value
     * @param atlasHeight the atlas height value
     */
    public UIFont(String path, float size, int atlasWidth, int atlasHeight) {
        this.path = path;
        this.size = size;
        this.atlasWidth = atlasWidth;
        this.atlasHeight = atlasHeight;

        ByteBuffer fontData = loadFont(path);
        glyphs = STBTTBakedChar.malloc(CHAR_COUNT);
        ByteBuffer bitmap = memAlloc(atlasWidth * atlasHeight);
        STBTruetype.stbtt_BakeFontBitmap(fontData, size, bitmap, atlasWidth,
                atlasHeight, FIRST_CHAR, glyphs);

        textureId = glGenTextures();

        glBindTexture(GL_TEXTURE_2D, textureId);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RED, atlasWidth, atlasHeight,
                0, GL_RED, GL_UNSIGNED_BYTE, bitmap);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        glBindTexture(GL_TEXTURE_2D, 0);

        memFree(bitmap);
        memFree(fontData);
    }

    /**
     * Loads the font.
     * @param path the path value
     * @return the load font result
     */
    private ByteBuffer loadFont(String path) {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(path)) {
            if (input == null) {
                throw new IllegalArgumentException("Font not found: " + path);
            }

            byte[] data = input.readAllBytes();

            ByteBuffer buffer = memAlloc(data.length);
            buffer.put(data);
            buffer.flip();

            return buffer;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load font: " + path, e);
        }
    }

    /**
     * Performs the bind operation.
     */
    public void bind() {
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textureId);
    }

    /**
     * Performs the unbind operation.
     */
    public void unbind() {
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    /**
     * Returns the texture id.
     * @return the texture id
     */
    public int getTextureId() {
        return textureId;
    }

    /**
     * Returns the size.
     * @return the size
     */
    public float getSize() {
        return size;
    }

    /**
     * Returns the atlas width.
     * @return the atlas width
     */
    public int getAtlasWidth() {
        return atlasWidth;
    }

    /**
     * Returns the atlas height.
     * @return the atlas height
     */
    public int getAtlasHeight() {
        return atlasHeight;
    }

    /**
     * Returns the glyph.
     * @param character the character value
     * @return the glyph
     */
    public STBTTBakedChar getGlyph(int character) {
        if (character < FIRST_CHAR || character > LAST_CHAR) {
            return null;
        }

        return glyphs.get(character - FIRST_CHAR);
    }

    /**
     * Returns the first char.
     * @return the first char
     */
    public int getFirstChar() {
        return FIRST_CHAR;
    }

    /**
     * Returns the last char.
     * @return the last char
     */
    public int getLastChar() {
        return LAST_CHAR;
    }

    /**
     * Performs the dispose operation.
     */
    public void dispose() {
        glDeleteTextures(textureId);
        glyphs.free();
    }
}