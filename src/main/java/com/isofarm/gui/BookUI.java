package com.isofarm.gui;

import com.isofarm.data.BookLine;
import com.isofarm.data.Singleton;
import com.isofarm.graphics.SpriteSheet;
import com.isofarm.input.Mouse;
import com.isofarm.item.Book;
import com.isofarm.item.Page;
import com.isofarm.utils.K;
import com.isofarm.wrld.GameMaster;
import org.joml.Vector4f;
import org.lwjgl.stb.STBTTBakedChar;

/**
 * Provides book ui behavior.
 */
@Singleton
public class BookUI extends UIElement {
    private static final float ANIMATION_DURATION = 0.35f;
    private static final float PAGE_FLIP_DURATION = 0.4f;
    private static final int TOTAL_ANIM_FRAMES = 16;

    public static BookUI bui;
    private float animationProgress = 0.0f;

    private boolean isOpening = false;
    private boolean isClosing = false;

    private boolean isFlippingPage = false;
    private boolean isFlippingNext = true;

    private float pageFlipTimer = 0.0f;

    private BookLine hoveredBookLine;

    /**
     * Creates a new {@code BookUI} instance.
     * @param x the x value
     * @param y the y value
     * @param width the width value
     * @param height the height value
     */
    public BookUI(float x, float y, float width, float height) {
        super(x, y, width, height);
        hide();
    }

    /**
     * Initializes the component.
     * @param x the x value
     * @param y the y value
     * @param width the width value
     * @param height the height value
     */
    public static void init(float x, float y, float width, float height) {
        if (bui != null) return;
        bui = new BookUI(x, y, width, height);
    }

    /**
     * Performs the ease in out cubic operation.
     * @param t the t value
     * @return the ease in out cubic result
     */
    private static float easeInOutCubic(float t) {
        if (t < 0.5f) {
            return 4.0f * t * t * t;
        }

        return 1.0f - (float) Math.pow(-2.0f * t + 2.0f, 3.0f) / 2.0f;
    }

    /**
     * Performs the lerp operation.
     * @param start the start value
     * @param end the end value
     * @param t the t value
     * @return the lerp result
     */
    private static float lerp(float start, float end, float t) {
        return start + (end - start) * t;
    }

    /**
     * Renders render.
     */
    @Override
    public void render() {
    }

    /**
     * Performs the open operation.
     */
    public void open() {
        isClosing = false;
        isOpening = true;
    }

    /**
     * Performs the close operation.
     */
    public void close() {
        if (isClosing) return;

        isClosing = true;
        isOpening = false;
    }

    /**
     * Checks whether the closed condition is met.
     * @return {@code true} if closed; otherwise {@code false}
     */
    public boolean isClosed() {
        return !isOpening && !isClosing && animationProgress <= 0.0f;
    }

    /**
     * Checks whether the animating condition is met.
     * @return {@code true} if animating; otherwise {@code false}
     */
    public boolean isAnimating() {
        return isOpening || isClosing;
    }

    /**
     * Checks whether the open condition is met.
     * @return {@code true} if open; otherwise {@code false}
     */
    public boolean isOpen() {
        return !isOpening && !isClosing && animationProgress >= 1.0f;
    }

    /**
     * Updates the current state.
     * @param book the book value
     * @param animSheet the anim sheet value
     */
    public void update(Book book, SpriteSheet animSheet) {
        if (book == null || !isOpen() || book.getPages().isEmpty() || animSheet == null) {
            hoveredBookLine = null;
            GameMaster.game.getGameUIService()
                    .getUIManager()
                    .hideTooltip();
            return;
        }

        updateBookLine(animSheet, book);
        if (Mouse.isButtonPressed(Mouse.BUTTON_LEFT)) {
            click();
        }
    }

    /**
     * Updates the book line.
     * @param animSheet the anim sheet value
     * @param book the book value
     */
    private void updateBookLine(SpriteSheet animSheet, Book book) {
        float screenWidth = GUI.getScreenWidth();
        float screenHeight = GUI.getScreenHeight();

        float scale = 2.0f;

        float bookWidth = animSheet.getFrameWidth() * scale;
        float bookHeight = animSheet.getFrameHeight() * scale;

        float centerX = (screenWidth - bookWidth) * 0.5f;
        float centerY = (screenHeight - bookHeight) * 0.5f;

        float easedProgress = easeInOutCubic(animationProgress);
        float y = lerp(screenHeight, centerY, easedProgress);

        float paddingX = K.UI.UI_BOOK_PADDING_X;
        float paddingTop = K.UI.UI_BOOK_PADDING_TOP;
        float lineHeight = GUI.getNormalFont().getSize();

        setTooltipText(null);
        GameMaster.game.getGameUIService()
                .getUIManager()
                .hideTooltip();

        hoveredBookLine = null;
        int leftPageIndex = book.getCurrentPage();

        if (leftPageIndex < book.getPages().size()) {
            float textX = centerX + paddingX;
            checkHover(book.getPage(leftPageIndex), textX, y + paddingTop, lineHeight);
        }

        int rightPageIndex = leftPageIndex + 1;
        if (hoveredBookLine == null && rightPageIndex < book.getPages().size()) {
            float textX = centerX + (bookWidth / 2.0f) + paddingX;
            checkHover(book.getPage(rightPageIndex), textX, y + paddingTop, lineHeight);
        }
    }

    /**
     * Performs the check hover operation.
     * @param page the page value
     * @param textX the text x value
     * @param startY the start y value
     * @param lineHeight the line height value
     */
    private void checkHover(Page page, float textX, float startY, float lineHeight) {
        float textY = startY;
        for (BookLine bookLine : page.getLines()) {
            if (bookLine.isInteractive() && isMouseHovering(textX, textY, bookLine.getText())) {
                hoveredBookLine = bookLine;
                GameMaster.game.getGameUIService().getUIManager().showTooltip(bookLine.getTooltipText(),
                        Mouse.getX(), Mouse.getY());
                break;
            }
            textY += lineHeight;
        }
    }

    /**
     * Renders render.
     * @param book the book value
     * @param delta the delta value
     * @param animSheet the anim sheet value
     */
    public void render(Book book, float delta, SpriteSheet animSheet) {
        if (book == null || animSheet == null) return;
        float screenWidth = GUI.getScreenWidth();
        float screenHeight = GUI.getScreenHeight();

        float scale = 2.0f;
        float bookWidth = animSheet.getFrameWidth() * scale;
        float bookHeight = animSheet.getFrameHeight() * scale;

        float centerX = (screenWidth - bookWidth) * 0.5f;
        float centerY = (screenHeight - bookHeight) * 0.5f;

        updateAnimation(delta);
        float alpha = easeInOutCubic(animationProgress);
        float y = lerp(screenHeight, centerY, alpha);

        if (isFlippingPage) {
            pageFlipTimer += delta;
            float progress = Math.min(1.0f, pageFlipTimer / PAGE_FLIP_DURATION);
            float animFrameProgress = isFlippingNext ? progress : (1.0f - progress);
            int currentFrame = (int) (animFrameProgress * (TOTAL_ANIM_FRAMES - 1));
            GUI.drawSprite(animSheet, currentFrame, centerX, y, bookWidth, bookHeight, new Vector4f(1.0f));

            if (progress >= 1.0f) {
                isFlippingPage = false;
            }

        } else {
            GUI.drawSprite(animSheet, 0, centerX, y, bookWidth, bookHeight, new Vector4f(1.0f));
            renderSpread(book, centerX, y, animSheet, scale, alpha);
        }
        renderChildren();
    }

    /**
     * Updates the animation.
     * @param delta the delta value
     */
    private void updateAnimation(float delta) {
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

    /**
     * Performs the next page operation.
     */
    public void nextPage() {
        if (isFlippingPage) return;

        isFlippingPage = true;
        isFlippingNext = true;
        pageFlipTimer = 0.0f;
    }

    /**
     * Performs the previous page operation.
     */
    public void previousPage() {
        if (isFlippingPage) return;

        isFlippingPage = true;
        isFlippingNext = false;
        pageFlipTimer = 0.0f;
    }

    /**
     * Renders the spread.
     * @param book the book value
     * @param x the x value
     * @param y the y value
     * @param animSheet the anim sheet value
     * @param scale the scale value
     * @param alpha the alpha value
     */
    private void renderSpread(Book book, float x, float y, SpriteSheet animSheet, float scale, float alpha) {
        if (book.getPages().isEmpty() || alpha <= 0.0f) return;
        float bookWidth = animSheet.getFrameWidth() * scale;
        float leftPageX = x + K.UI.UI_BOOK_PADDING_X;
        float rightPageX = x + (bookWidth / 2.0f) + K.UI.UI_BOOK_PADDING_X;

        int leftPageIndex = book.getCurrentPage();
        if (leftPageIndex < book.getPages().size()) {
            renderPage(book.getPage(leftPageIndex), leftPageX, y + K.UI.UI_BOOK_PADDING_TOP, alpha);
        }

        int rightPageIndex = leftPageIndex + 1;
        if (rightPageIndex < book.getPages().size()) {
            renderPage(book.getPage(rightPageIndex), rightPageX, y + K.UI.UI_BOOK_PADDING_TOP, alpha);
        }
    }

    /**
     * Renders the page.
     * @param page the page value
     * @param textX the text x value
     * @param startY the start y value
     * @param alpha the alpha value
     */
    private void renderPage(Page page, float textX, float startY, float alpha) {
        float textY = startY;
        float lineHeight = GUI.getNormalFont().getSize();
        for (BookLine bookLine : page.getLines()) {
            String renderText = bookLine.getText();
            if (renderText.startsWith("-")) {
                renderText = renderText.replace("-", "");
            }

            boolean isHovered = bookLine == hoveredBookLine;
            Vector4f baseColor = isHovered ? new Vector4f(0.1f, 0.4f, 0.9f, 1.0f) : K.UI.UI_BOOK_TEXT_COLOR;
            Vector4f finalColor = new Vector4f(baseColor.x, baseColor.y, baseColor.z, baseColor.w * alpha);

            if (isHovered) {
                float cursorOffset = 24.0f;
                GUI.drawNormalString(">", textX - cursorOffset, textY, finalColor);
            }
            if (renderText.startsWith("**")) {
                renderText = renderText.replace("**", "");
            }
            GUI.drawNormalString(renderText, textX, textY, finalColor);
            textY += lineHeight;
        }
    }

    /**
     * Checks whether the mouse hovering condition is met.
     * @param x the x value
     * @param y the y value
     * @param text the text value
     * @return {@code true} if mouse hovering; otherwise {@code false}
     */
    public boolean isMouseHovering(float x, float y, String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        float mouseX = Mouse.getX();
        float mouseY = Mouse.getY();
        String cleanText = text.replace("**", "").replace("-", "").trim();

        if (cleanText.isEmpty()) {
            return false;
        }

        UIFont font = GUI.getNormalFont();
        float width = GUI.getStringWidth(cleanText, font);
        float minY = Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;

        for (int i = 0; i < cleanText.length(); ) {
            int codePoint = cleanText.codePointAt(i);
            STBTTBakedChar glyph = font.getGlyph(codePoint);
            if (glyph != null) {
                float glyphTop = y + glyph.yoff();
                float glyphBottom = glyphTop + (glyph.y1() - glyph.y0());
                minY = Math.min(minY, glyphTop);
                maxY = Math.max(maxY, glyphBottom);
            }
            i += Character.charCount(codePoint);
        }

        if (minY == Float.MAX_VALUE) {
            return false;
        }

        float horizontalPadding = 6.0f;
        float verticalPadding = 2.0f;
        return mouseX >= x - horizontalPadding && mouseX <= x + width + horizontalPadding
                && mouseY >= minY - verticalPadding && mouseY <= maxY + verticalPadding;
    }

    /**
     * Performs the click operation.
     */
    private void click() {
        if (hoveredBookLine == null || !hoveredBookLine.isInteractive()) {
            return;
        }

        hoveredBookLine.click();
    }

    /**
     * Performs the reload operation.
     * @param openedBook the opened book value
     */
    public void reload(Book openedBook) {
        openedBook.reload(GameMaster.game.getPlayer());
    }
}
