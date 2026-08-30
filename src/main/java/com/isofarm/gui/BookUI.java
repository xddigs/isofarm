package com.isofarm.gui;

import com.isofarm.graphics.Texture;
import com.isofarm.item.Book;
import com.isofarm.item.Page;
import com.isofarm.utils.K;
import org.joml.Vector4f;

public class BookUI {
    private static final Texture page = new Texture(K.Paths.DEFAULT_BOOK_UI);
    private static final float ANIMATION_DURATION = 0.35f;
    private static float animationProgress = 0.0f;

    private static boolean isOpening = false;
    private static boolean isClosing = false;

    public static void open() {
        isClosing = false;
        isOpening = true;
    }

    public static void close() {
        if (isClosing) {
            return;
        }

        isClosing = true;
        isOpening = false;
    }

    public static boolean isClosed() {
        return !isOpening && !isClosing && animationProgress <= 0.0f;
    }

    public static boolean isAnimating() {
        return isOpening || isClosing;
    }

    public static boolean isOpen() {
        return !isOpening && !isClosing && animationProgress >= 1.0f;
    }

    public static void render(Book book, float delta) {
        if (book == null) {
            return;
        }

        float screenWidth = GUI.getScreenWidth();
        float screenHeight = GUI.getScreenHeight();

        float scale = 2.0f;

        float bookWidth = page.getWidth() * scale;
        float bookHeight = page.getHeight() * scale;

        float centerX = (screenWidth - bookWidth) * 0.5f;
        float centerY = (screenHeight - bookHeight) * 0.5f;

        updateAnimation(delta);

        float easedProgress = easeInOutCubic(animationProgress);
        float startY = screenHeight;
        float y = lerp(startY, centerY, easedProgress);

        GUI.drawTexture(page, centerX, y, bookWidth, bookHeight, new Vector4f(1.0f));

        renderPage(book, centerX, y);
    }

    private static void updateAnimation(float delta) {
        float amount = delta / ANIMATION_DURATION;

        if (isOpening) {
            animationProgress += amount;

            if (animationProgress >= 1.0f) {
                animationProgress = 1.0f;
                isOpening = false;
            }
        }

        if (isClosing) {
            animationProgress -= amount;

            if (animationProgress <= 0.0f) {
                animationProgress = 0.0f;
                isClosing = false;
            }
        }
    }

    private static float easeInOutCubic(float t) {
        if (t < 0.5f) {
            return 4.0f * t * t * t;
        }
        return 1.0f - (float) Math.pow(-2.0f * t + 2.0f, 3.0f) / 2.0f;
    }

    private static float lerp(float start, float end, float t) {
        return start + (end - start) * t;
    }

    private static void renderPage(Book book, float x, float y) {
        if (book.getPages().isEmpty()) {
            return;
        }

        Page page = book.getPage(book.getCurrentPage());

        float paddingX = K.UI.UI_BOOK_PADDING_X;
        float paddingTop = K.UI.UI_BOOK_PADDING_TOP;
        float lineHeight = GUI.getNormalFont().getSize();

        float textX = x + paddingX;
        float textY = y + paddingTop;

        for (String line : page.getLines()) {
            if (line.startsWith("-")) {
                line = line.replace("-", "");
            }

            if (line.startsWith("**")) {
                line = line.replace("**", "");
                GUI.drawBoldString(line, textX, textY, K.UI.UI_BOOK_TEXT_COLOR);
            } else {
                GUI.drawNormalString(line, textX, textY, K.UI.UI_BOOK_TEXT_COLOR);
            }

            textY += lineHeight;
        }
    }
}