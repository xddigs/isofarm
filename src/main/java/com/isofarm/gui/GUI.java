package com.isofarm.gui;

import com.isofarm.data.GodObject;
import com.isofarm.graphics.*;
import com.isofarm.input.Mouse;
import com.isofarm.item.Item;
import com.isofarm.utils.K;
import com.isofarm.utils.Settings;
import com.isofarm.utils.Utils;
import com.isofarm.wrld.GameMaster;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.stb.STBTTBakedChar;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

/**
 * Provides gui behavior.
 */
@Utils
@GodObject
public class GUI {
    private static final Shader shader = new Shader(K.Paths.UI_VERTEX_SHADER, K.Paths.UI_FRAG_SHADER);
    private static final Mesh mesh = Mesh.createQuad();
    private static final Matrix4f projection = new Matrix4f();
    private static final Matrix4f model = new Matrix4f();

    private static final float FONT_SMALL = 16.0f;
    private static final float FONT_NORMAL = 24.0f;
    private static final float FONT_BIG = 32.0f;

    private static final UIFont small = new UIFont(K.Paths.FONT, FONT_SMALL);
    private static final UIFont normal = new UIFont(K.Paths.FONT, FONT_NORMAL);
    private static final UIFont big = new UIFont(K.Paths.FONT, FONT_BIG);

    private static final UIFont smallBold = new UIFont(K.Paths.FONT_BOLD, FONT_SMALL);
    private static final UIFont normalBold = new UIFont(K.Paths.FONT_BOLD, FONT_NORMAL);
    private static final UIFont bigBold = new UIFont(K.Paths.FONT_BOLD, FONT_BIG);
    private static final float CURSOR_ICON_OFFSET = 36.0f;

    private static int screenWidth;
    private static int screenHeight;
    private static boolean wasCursorIconDrawn = false;

    /**
     * Creates a new {@code GUI} instance.
     */
    private GUI() {
    }

    /**
     * Performs the begin operation.
     * @param screenWidth the screen width value
     * @param screenHeight the screen height value
     */
    public static void begin(float screenWidth, float screenHeight) {
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_CULL_FACE);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDepthMask(false);
        glActiveTexture(GL_TEXTURE0);
        shader.bind();
        GUI.screenWidth = (int) screenWidth;
        GUI.screenHeight = (int) screenHeight;
        updateProjection();
        shader.setUniform("uProjection", projection);
        shader.setUniform("uTexture", 0);
        shader.setUniform("uUseFont", false);
        shader.setUniform("uUseSilhouette", false);
        shader.setUniform("uUsePageTransform", false);
    }

    /**
     * Performs the end operation.
     */
    public static void end() {
        shader.unbind();
        glDepthMask(true);
        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
    }

    /**
     * Draws the line.
     * @param x1 the x1 value
     * @param y1 the y1 value
     * @param x2 the x2 value
     * @param y2 the y2 value
     * @param thickness the thickness value
     * @param color the color value
     */
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

    /**
     * Draws the border.
     * @param x the x value
     * @param y the y value
     * @param width the width value
     * @param height the height value
     * @param color the color value
     * @param borderWidth the border width value
     */
    public static void drawBorder(float x, float y, float width, float height,
                                  Vector4f color, float borderWidth) {
        drawBorder(x, y, width, height, color, borderWidth, 0.0f);
    }

    /**
     * Draws the border.
     * @param x the x value
     * @param y the y value
     * @param width the width value
     * @param height the height value
     * @param color the color value
     * @param borderWidth the border width value
     * @param arc the arc value
     */
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

    /**
     * Draws the rect.
     * @param x the x value
     * @param y the y value
     * @param width the width value
     * @param height the height value
     * @param color the color value
     */
    public static void drawRect(float x, float y, float width, float height,
                                Vector4f color) {
        drawRect(x, y, width, height, color, 0.0f, null, 0.0f);
    }

    /**
     * Draws the rect.
     * @param x the x value
     * @param y the y value
     * @param width the width value
     * @param height the height value
     * @param color the color value
     * @param borderColor the border color value
     * @param borderWidth the border width value
     */
    public static void drawRect(float x, float y, float width, float height,
                                Vector4f color, Vector4f borderColor, float borderWidth) {
        drawRect(x, y, width, height, color, 0.0f, borderColor, borderWidth);
    }

    /**
     * Draws the rect.
     * @param x the x value
     * @param y the y value
     * @param width the width value
     * @param height the height value
     * @param color the color value
     * @param arc the arc value
     */
    public static void drawRect(float x, float y, float width, float height,
                                Vector4f color, float arc) {
        drawRect(x, y, width, height, color, arc, null, 0.0f);
    }

    /**
     * Draws the rect.
     * @param x the x value
     * @param y the y value
     * @param width the width value
     * @param height the height value
     * @param color the color value
     * @param arc the arc value
     * @param borderColor the border color value
     * @param borderWidth the border width value
     */
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

    /**
     * Draws the texture.
     * @param texture the texture value
     * @param x the x value
     * @param y the y value
     * @param width the width value
     * @param height the height value
     * @param tint the tint value
     */
    public static void drawTexture(Texture texture, float x, float y,
                                   float width, float height, Vector4f tint) {
        if (texture == null) return;
        texture.bind();

        model.identity()
                .translate(x, y, 0.0f)
                .scale(width, height, 1.0f);

        shader.setUniform("uModel", model);
        shader.setUniform("uColor", tint);
        shader.setUniform("uUseTexture", true);
        shader.setUniform("uUseFont", false);
        shader.setUniform("uUVBounds", new Vector4f(0.0f, 1.0f, 1.0f, 0.0f));

        mesh.render();
        texture.unbind();
    }

    /**
     * Draws the sprite.
     * @param spriteSheet the sprite sheet value
     * @param column the column value
     * @param row the row value
     * @param x the x value
     * @param y the y value
     * @param width the width value
     * @param height the height value
     * @param tint the tint value
     */
    public static void drawSprite(SpriteSheet spriteSheet, int column, int row,
                                  float x, float y, float width, float height,
                                  Vector4f tint) {
        if (spriteSheet == null) return;
        int cols = spriteSheet.getCols();
        int rows = spriteSheet.getRows();

        column = Math.clamp(column, 0, cols - 1);
        row = Math.clamp(row, 0, rows - 1);

        int frameIndex = row * cols + column;

        Vector4f uv = spriteSheet.getUVBounds(frameIndex);
        Vector4f uvBounds = new Vector4f(uv.x, uv.w, uv.z, uv.y);

        spriteSheet.bind();
        model.identity().translate(x, y, 0.0f).scale(width, height, 1.0f);
        shader.setUniform("uModel", model);
        shader.setUniform("uColor", tint);
        shader.setUniform("uUseTexture", true);
        shader.setUniform("uUseFont", false);
        shader.setUniform("uUVBounds", uvBounds);

        mesh.render();
        spriteSheet.unbind();
    }

    /**
     * Draws the sprite.
     * @param spriteSheet the sprite sheet value
     * @param frame the frame value
     * @param x the x value
     * @param y the y value
     * @param width the width value
     * @param height the height value
     * @param tint the tint value
     */
    public static void drawSprite(SpriteSheet spriteSheet, int frame,
                                  float x, float y, float width, float height, Vector4f tint) {
        drawSprite(spriteSheet, frame % spriteSheet.getCols(),
                frame / spriteSheet.getCols(), x, y, width, height, tint);
    }

    /** Draws a sprite as a solid color while preserving its texture alpha. */
    public static void drawSpriteSilhouette(SpriteSheet spriteSheet, int frame,
                                            float x, float y, float width, float height,
                                            Vector4f color) {
        shader.setUniform("uUseSilhouette", true);
        drawSprite(spriteSheet, frame, x, y, width, height, color);
        shader.setUniform("uUseSilhouette", false);
    }

    /**
     * Applies a temporary horizontal fold and curve around a book spine.
     * Every UI primitive rendered until {@link #endPageTransform()} receives
     * the same deformation.
     */
    public static void beginPageTransform(float pivotX, float scaleX,
                                          float pageWidth, float curve) {
        shader.setUniform("uPagePivotX", pivotX);
        shader.setUniform("uPageScaleX", Math.max(0.0f, scaleX));
        shader.setUniform("uPageWidth", Math.max(1.0f, pageWidth));
        shader.setUniform("uPageCurve", curve);
        shader.setUniform("uUsePageTransform", true);
    }

    /** Restores normal UI rendering after a page fold. */
    public static void endPageTransform() {
        shader.setUniform("uUsePageTransform", false);
    }

    /**
     * Draws the string.
     * @param text the text value
     * @param x the x value
     * @param y the y value
     * @param font the font value
     * @param color the color value
     */
    public static void drawString(String text, float x, float y,
                                  UIFont font, Vector4f color) {
        drawString(text, x, y, font, color, 1.0f);
    }

    /**
     * Draws the string.
     * @param text the text value
     * @param x the x value
     * @param y the y value
     * @param font the font value
     * @param color the color value
     * @param textScale the text scale value
     */
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

    /**
     * Draws the small string.
     * @param text the text value
     * @param x the x value
     * @param y the y value
     * @param color the color value
     */
    public static void drawSmallString(String text, float x, float y, Vector4f color) {
        drawString(text, x, y, small, color);
    }

    /**
     * Draws the normal string.
     * @param text the text value
     * @param x the x value
     * @param y the y value
     * @param color the color value
     */
    public static void drawNormalString(String text, float x, float y, Vector4f color) {
        drawString(text, x, y, normal, color);
    }

    /**
     * Draws the bold string.
     * @param line the line value
     * @param textX the text x value
     * @param textY the text y value
     * @param uiBookTextColor the ui book text color value
     */
    public static void drawBoldString(String line, float textX, float textY, Vector4f uiBookTextColor) {
        drawString(line, textX, textY, normalBold, uiBookTextColor);
    }

    /**
     * Draws the big string.
     * @param text the text value
     * @param x the x value
     * @param y the y value
     * @param color the color value
     */
    public static void drawBigString(String text, float x, float y, Vector4f color) {
        drawString(text, x, y, big, color);
    }

    /**
     * Returns the string width.
     * @param text the text value
     * @param font the font value
     * @return the string width
     */
    public static float getStringWidth(String text, UIFont font) {
        return getStringWidth(text, font, 1.0f);
    }

    /**
     * Returns the string width.
     * @param text the text value
     * @param font the font value
     * @param textScale the text scale value
     * @return the string width
     */
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

    /**
     * Returns the string height.
     * @param text the text value
     * @param normalFont the normal font value
     * @return the string height
     */
    public static float getStringHeight(String text, UIFont normalFont) {
        return normalFont.getSize() * text.length();
    }

    /**
     * Returns the centered text y.
     * @param text the text value
     * @param font the font value
     * @param boxY the box y value
     * @param boxHeight the box height value
     * @return the centered text y
     */
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

    /**
     * Performs the wrap text operation.
     * @param text the text value
     * @param maxWidth the max width value
     * @param font the font value
     * @return the wrap text result
     */
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

    /**
     * Performs the push scissor operation.
     * @param x the x value
     * @param y the y value
     * @param width the width value
     * @param height the height value
     */
    public static void pushScissor(float x, float y, float width, float height) {
        int windowHeight = (int) getScreenHeight();
        glEnable(GL_SCISSOR_TEST);
        glScissor((int) x, windowHeight - (int) (y + height),
                (int) width, (int) height);
    }

    /**
     * Performs the pop scissor operation.
     */
    public static void popScissor() {
        glDisable(GL_SCISSOR_TEST);
    }

    /**
     * Renders render.
     * @param element the element value
     */
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
                    element.getSpriteCol(),
                    element.getSpriteRow(),
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

    /**
     * Performs the resize operation.
     * @param width the width value
     * @param height the height value
     */
    public static void resize(int width, int height) {
        screenWidth = width;
        screenHeight = height;
        updateProjection();
    }

    /**
     * Updates the projection.
     */
    private static void updateProjection() {
        projection.identity().ortho2D(
                0.0f,
                screenWidth,
                screenHeight,
                0.0f
        );
    }

    /**
     * Draws the cursor.
     * @param gameMaster the game master value
     */
    public static void drawCursor(GameMaster gameMaster) {
        if (gameMaster == null) return;
        if (wasCursorIconDrawn) return;

        var hotbarUI = gameMaster.getGameUIService().getHotbarUI();
        if (hotbarUI == null) return;

        Item selectedItem = Settings.selectedItem;
        if (selectedItem == null) return;

        SpriteSheet spriteSheet = ResourceManager.getItemSpriteSheet(selectedItem);
        if (spriteSheet == null) return;

        int frameIndex = ResourceManager.getItemFrame(selectedItem);
        float iconSize = 32.0f;

        float renderX = Mouse.getX() + CURSOR_ICON_OFFSET;
        float renderY = Mouse.getY() + CURSOR_ICON_OFFSET;
        drawSprite(spriteSheet, frameIndex, renderX, renderY, iconSize, iconSize, new Vector4f(1.0f));
        wasCursorIconDrawn = true;
    }

    /**
     * Checks if the cursor was drawn
     * @return {@code true} if the cursor was drawn, {@code false} otherwise
     */
    public static boolean wasCursorIconDrawn() {
        return wasCursorIconDrawn;
    }

    /**
     * Sets the value of the {@code wasCursorIconDrawn} field.
     * @param wasCursorIconDrawn the new value of the {@code wasCursorIconDrawn} field
     */
    public static void setWasCursorIconDrawn(boolean wasCursorIconDrawn) {
        GUI.wasCursorIconDrawn = wasCursorIconDrawn;
    }

    /**
     * Returns the screen width.
     * @return the screen width
     */
    public static int getScreenWidth() {
        return screenWidth;
    }

    /**
     * Returns the screen height.
     * @return the screen height
     */
    public static int getScreenHeight() {
        return screenHeight;
    }

    /**
     * Returns the shader.
     * @return the shader
     */
    public static Shader getShader() {
        return shader;
    }

    /**
     * Returns the small font.
     * @return the small font
     */
    public static UIFont getSmallFont() {
        return small;
    }

    /**
     * Returns the normal font.
     * @return the normal font
     */
    public static UIFont getNormalFont() {
        return normal;
    }

    /**
     * Returns the big font.
     * @return the big font
     */
    public static UIFont getBigFont() {
        return big;
    }

    /**
     * Returns the small bold font.
     * @return the small bold font
     */
    public static UIFont getSmallBoldFont() {
        return smallBold;
    }

    /**
     * Returns the normal bold font.
     * @return the normal bold font
     */
    public static UIFont getNormalBoldFont() {
        return normalBold;
    }

    /**
     * Returns the big bold font.
     * @return the big bold font
     */
    public static UIFont getBigBoldFont() {
        return bigBold;
    }

    /**
     * Performs the dispose operation.
     */
    public static void dispose() {
        mesh.dispose();
        shader.dispose();
    }
}
