package com.isofarm.gui;

import com.isofarm.graphics.Mesh;
import com.isofarm.graphics.Shader;
import com.isofarm.graphics.SpriteSheet;
import com.isofarm.graphics.Texture;
import com.isofarm.utils.K;
import com.isofarm.utils.Utils;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.stb.STBTTBakedChar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11C.*;

@Utils
public class GUI {
    private static final Shader shader = new Shader(K.Paths.UI_VERTEX_SHADER, K.Paths.UI_FRAG_SHADER);
    private static final Mesh mesh = Mesh.createQuad();
    private static final Matrix4f projection = new Matrix4f();
    private static final Matrix4f model = new Matrix4f();
    private static final Logger log = LoggerFactory.getLogger(GUI.class);

    private static final UIFont small = new UIFont(K.Paths.FONT, 16.0f);
    private static final UIFont normal = new UIFont(K.Paths.FONT, 24.0f);
    private static final UIFont big = new UIFont(K.Paths.FONT, 32.0f);
    private static final UIFont large = new UIFont(K.Paths.FONT, 48.0f);

    private static final UIFont smallBold = new UIFont(K.Paths.FONT_BOLD, 16.0f);
    private static final UIFont normalBold = new UIFont(K.Paths.FONT_BOLD, 24.0f);
    private static final UIFont bigBold = new UIFont(K.Paths.FONT_BOLD, 32.0f);
    private static final UIFont largeBold = new UIFont(K.Paths.FONT_BOLD, 48.0f);

    private static int screenWidth;
    private static int screenHeight;

    private GUI() {}

    public static void begin(float screenWidth, float screenHeight) {
        shader.bind();
        GUI.screenWidth = (int) screenWidth;
        GUI.screenHeight = (int) screenHeight;
        updateProjection();
        shader.setUniform("uProjection", projection);
        shader.setUniform("uTexture", 0);
        shader.setUniform("uUseFont", false);
    }

    public static void end() {
        shader.unbind();
    }

    public static void drawLine(float x1, float y1, float x2, float y2,
                                float thickness, Vector4f color) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float length = (float) Math.sqrt(dx * dx + dy * dy);

        if (length <= 0.0f || thickness <= 0.0f) {
            return;
        }

        float angle = (float) Math.atan2(dy, dx);
        model.identity()
                .translate(x1, y1, 0.0f)
                .rotateZ(angle)
                .translate(0.0f, -thickness * 0.5f, 0.0f)
                .scale(length, thickness, 1.0f);

        shader.setUniform("uModel", model);
        shader.setUniform("uColor", color);
        shader.setUniform("uUseTexture", false);
        shader.setUniform("uUseFont", false);

        mesh.render();
    }

    public static void drawBorder(float x, float y, float width, float height,
                                  Vector4f color, float borderWidth) {
        drawBorder(x, y, width, height, color, borderWidth, 0.0f);
    }

    public static void drawBorder(float x, float y, float width, float height,
                                  Vector4f color, float borderWidth, float arc) {
        if (width <= 0.0f || height <= 0.0f || borderWidth <= 0.0f) {
            return;
        }

        float radius = Math.clamp(arc, 0.0f, Math.min(width, height) * 0.5f);
        model.identity().translate(x, y, 0.0f).scale(width, height, 1.0f);
        shader.setUniform("uModel", model);
        shader.setUniform("uColor", color);
        shader.setUniform("uBorderColor", color);
        shader.setUniform("uBorderWidth", borderWidth);
        shader.setUniform("uUseTexture", false);
        shader.setUniform("uUseFont", false);
        shader.setUniform("uUseRoundedRect", true);
        shader.setUniform("uRectSize", width, height);
        shader.setUniform("uCornerRadius", radius);
        shader.setUniform("uBorderOnly", true);
        mesh.render();
        shader.setUniform("uUseRoundedRect", false);
        shader.setUniform("uBorderOnly", false);
    }

    public static void drawRect(float x, float y, float width, float height,
                                Vector4f color) {
        drawRect(x, y, width, height, color, 0.0f, null, 0.0f);
    }

    public static void drawRect(float x, float y, float width, float height,
                                Vector4f color, Vector4f borderColor, float borderWidth) {
        drawRect(x, y, width, height, color, 0.0f, borderColor, borderWidth);
    }

    public static void drawRect(float x, float y, float width, float height,
                                Vector4f color, float arc) {
        drawRect(x, y, width, height, color, arc, null, 0.0f);
    }

    public static void drawRect(float x, float y, float width, float height,
                                Vector4f color, float arc,
                                Vector4f borderColor, float borderWidth) {
        if (width <= 0.0f || height <= 0.0f) {
            return;
        }

        float radius = Math.clamp(arc, 0.0f, Math.min(width, height) * 0.5f);

        float border = Math.max(0.0f, borderWidth);
        boolean rounded = radius > 0.0f || border > 0.0f;
        model.identity().translate(x, y, 0.0f).scale(width, height, 1.0f);
        shader.setUniform("uModel", model);
        shader.setUniform("uColor", color);
        shader.setUniform("uBorderColor", borderColor != null ? borderColor : color);
        shader.setUniform("uBorderWidth", border);
        shader.setUniform("uUseTexture", false);
        shader.setUniform("uUseFont", false);
        shader.setUniform("uUseRoundedRect", rounded);
        shader.setUniform("uRectSize", width, height);
        shader.setUniform("uCornerRadius", radius);
        shader.setUniform("uBorderOnly", false);
        mesh.render();
        shader.setUniform("uUseRoundedRect", false);
    }

    public static void drawTexture(Texture texture, float x, float y,
                                   float width, float height, Vector4f tint) {
        if (texture == null) {
            return;
        }

        texture.bind();

        model.identity()
                .translate(x + width * 0.5f, y + height * 0.5f, 0.0f)
                .scale(width, height, 1.0f);

        shader.setUniform("uModel", model);
        shader.setUniform("uColor", tint);
        shader.setUniform("uUseTexture", true);
        shader.setUniform("uFrameIndex", 0);
        shader.setUniform("uTotalFrames", 1);

        mesh.render();

        texture.unbind();
    }

    public static void drawSprite(SpriteSheet spriteSheet, int frame, float x, float y,
                                  float width, float height, Vector4f tint) {
        if (spriteSheet == null) {
            return;
        }

        spriteSheet.bind();
        model.identity()
                .translate(x, y, 0.0f)
                .scale(width, height, 1.0f);

        shader.setUniform("uModel", model);
        shader.setUniform("uColor", tint);
        shader.setUniform("uUseTexture", true);
        shader.setUniform("uFrameIndex", frame);
        shader.setUniform("uTotalFrames", spriteSheet.getTotalFrames());

        mesh.render();
        spriteSheet.unbind();
    }

    public static void drawString(String text, float x, float y,
                                  UIFont font, Vector4f color) {
        drawString(text, x, y, font, color, 1.0f);
    }

    public static void drawString(String text, float x, float y,
                                  UIFont font, Vector4f color, float textScale) {
        if (text == null || text.isEmpty() || font == null) {
            return;
        }

        font.bind();
        shader.setUniform("uUseTexture", true);
        shader.setUniform("uUseFont", true);
        shader.setUniform("uFrameIndex", 0);
        shader.setUniform("uTotalFrames", 1);
        shader.setUniform("uColor", color);

        float cursorX = x;

        for (int i = 0; i < text.length(); ) {
            int codePoint = text.codePointAt(i);
            STBTTBakedChar glyph = font.getGlyph(codePoint);
            if (glyph == null) {
                cursorX += (font.getSize() * 0.5f) * textScale;
                i += Character.charCount(codePoint);
                continue;
            }

            float width = (glyph.x1() - glyph.x0()) * textScale;
            float height = (glyph.y1() - glyph.y0()) * textScale;

            if (width > 0.0f && height > 0.0f) {
                float glyphX = cursorX + (glyph.xoff() * textScale);
                float glyphY = y + (glyph.yoff() * textScale);

                float u0 = (float) glyph.x0() / font.getAtlasWidth();
                float v0 = (float) glyph.y0() / font.getAtlasHeight();
                float u1 = (float) glyph.x1() / font.getAtlasWidth();
                float v1 = (float) glyph.y1() / font.getAtlasHeight();

                model.identity()
                        .translate(glyphX, glyphY, 0.0f)
                        .scale(width, height, 1.0f);

                shader.setUniform("uModel", model);
                shader.setUniform("uGlyphUV", new Vector4f(u0, v0, u1, v1));

                mesh.render();
            }

            cursorX += glyph.xadvance() * textScale;
            i += Character.charCount(codePoint);
        }

        font.unbind();
        shader.setUniform("uUseFont", false);
    }

    public static void drawSmallString(String text, float x, float y, Vector4f color) {
        drawString(text, x, y, small, color);
    }

    public static void drawNormalString(String text, float x, float y, Vector4f color) {
        drawString(text, x, y, normal, color);
    }

    public static void drawBigString(String text, float x, float y, Vector4f color) {
        drawString(text, x, y, big, color);
    }

    public static float getStringWidth(String text, UIFont font) {
        return getStringWidth(text, font, 1.0f);
    }

    public static float getStringWidth(String text, UIFont font, float textScale) {
        if (text == null || text.isEmpty() || font == null) {
            return 0.0f;
        }

        float width = 0.0f;
        for (int i = 0; i < text.length(); i++) {
            char character = text.charAt(i);
            STBTTBakedChar glyph = font.getGlyph(character);
            if (glyph == null) {
                width += (font.getSize() * 0.5f) * textScale;
                continue;
            }
            width += glyph.xadvance() * textScale;
        }
        return width;
    }

    public static float getStringHeight(String text, UIFont normalFont) {
        return normalFont.getSize() * text.length();
    }

    public static float getCenteredTextY(String text, UIFont font,
                                         float boxY, float boxHeight) {
        if (text == null || text.isEmpty() || font == null) {
            return boxY;
        }

        float minY = Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;

        for (int i = 0; i < text.length(); ) {
            int codePoint = text.codePointAt(i);
            STBTTBakedChar glyph = font.getGlyph(codePoint);

            if (glyph != null) {
                float top = glyph.yoff();
                float bottom = glyph.yoff() + (glyph.y1() - glyph.y0());

                minY = Math.min(minY, top);
                maxY = Math.max(maxY, bottom);
            }

            i += Character.charCount(codePoint);
        }

        if (minY == Float.MAX_VALUE) {
            return boxY + font.getSize();
        }

        float textHeight = maxY - minY;
        float centeredTop = boxY + (boxHeight - textHeight) * 0.5f;
        return centeredTop - minY;
    }

    public static String[] wrapText(String text, float maxWidth, UIFont font) {
        if (text == null || text.isEmpty() || maxWidth <= 0.0f) {
            return new String[0];
        }

        List<String> lines = new ArrayList<>();
        String[] words = text.split("\\s+");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            if (word.isEmpty()) continue;
            String testLine = currentLine.isEmpty() ? word : currentLine + " " + word;
            if (getStringWidth(testLine, font) <= maxWidth) {
                if (!currentLine.isEmpty()) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
            } else {
                if (!currentLine.isEmpty()) {
                    lines.add(currentLine.toString());
                    currentLine.setLength(0);
                }
                currentLine.append(word);
            }
        }

        if (!currentLine.isEmpty()) {
            lines.add(currentLine.toString());
        }

        return lines.toArray(new String[0]);
    }

    public static void pushScissor(float x, float y, float width, float height) {
        int windowHeight = (int) getScreenHeight();
        glEnable(GL_SCISSOR_TEST);
        glScissor((int) x, windowHeight - (int) (y + height),
                (int) width, (int) height);
    }

    public static void popScissor() {
        glDisable(GL_SCISSOR_TEST);
    }

    public static void render(UIElement element) {
        if (element == null || !element.isActuallyVisible()) {
            return;
        }

        if (element.hasStaticSprite()) {
            drawTexture(
                    element.getSprite(),
                    element.getAbsoluteX(),
                    element.getAbsoluteY(),
                    element.getAbsoluteWidth(),
                    element.getAbsoluteHeight(),
                    element.getTint()
            );
        }

        if (element.hasSpriteSheet()) {
            drawSprite(
                    element.getSpriteSheet(),
                    element.getSpriteFrame(),
                    element.getAbsoluteX(),
                    element.getAbsoluteY(),
                    element.getAbsoluteWidth(),
                    element.getAbsoluteHeight(),
                    element.getTint()
            );
        }

        element.render();
        element.renderChildren();
    }

    public static void resize(int width, int height) {
        screenWidth = width;
        screenHeight = height;
        updateProjection();
    }

    private static void updateProjection() {
        projection.identity().ortho2D(
                0.0f,
                screenWidth,
                screenHeight,
                0.0f
        );
    }

    public static int getScreenWidth() {
        return screenWidth;
    }

    public static int getScreenHeight() {
        return screenHeight;
    }

    public static Shader getShader() {
        return shader;
    }

    public static UIFont getSmallFont() {
        return small;
    }

    public static UIFont getNormalFont() {
        return normal;
    }

    public static UIFont getBigFont() {
        return big;
    }

    public static UIFont getLargeFont() {
        return large;
    }

    public static UIFont getSmallBoldFont() {
        return smallBold;
    }

    public static UIFont getNormalBoldFont() {
        return normalBold;
    }

    public static UIFont getBigBoldFont() {
        return bigBold;
    }

    public static UIFont getLargeBoldFont() {
        return largeBold;
    }

    public static void dispose() {
        mesh.dispose();
        shader.dispose();
    }
}