package com.isofarm.gui;

import com.isofarm.data.CompletionProvider;
import com.isofarm.data.GodObject;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * Provides uitext field behavior.
 */
@SuppressWarnings("all")
@GodObject
public class UITextField extends UIElement {
    private final Vector4f backgroundColor = new Vector4f(0.08f, 0.08f, 0.08f, 1.0f);
    private final Vector4f focusedColor = new Vector4f(0.12f, 0.12f, 0.12f, 1.0f);
    private final Vector4f textColor = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
    private final Vector4f cursorColor = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
    private final Vector4f selectionColor = new Vector4f(0.2f, 0.4f, 0.8f, 0.8f);
    private final StringBuilder text = new StringBuilder();
    private UIFont font = GUI.getNormalFont();

    private int cursorPosition;
    private int selectionAnchor;
    private int maxLength = 256;

    private float cursorTimer;
    private boolean cursorVisible = true;
    private float scrollOffset;

    private CompletionProvider completionProvider;
    private List<String> completions = List.of();
    private int completionIndex = -1;
    private String completionInput = "";
    private int completionCursor = -1;
    private int completionTokenStart = -1;

    /**
     * Creates a new {@code UITextField} instance.
     * @param x the x value
     * @param y the y value
     * @param width the width value
     * @param height the height value
     */
    public UITextField(float x, float y, float width, float height) {
        super(x, y, width, height);
        setFocusable(true);
        setLayer(1000);
        hide();
    }

    /**
     * Updates the current state.
     * @param delta the delta value
     */
    @Override
    public void update(float delta) {
        super.update(delta);

        if (!isFocused()) {
            cursorTimer = 0.0f;
            cursorVisible = true;
            return;
        }

        cursorTimer += delta;

        if (cursorTimer >= 0.5f) {
            cursorTimer -= 0.5f;
            cursorVisible = !cursorVisible;
        }

        updateScroll();
    }

    /**
     * Renders render.
     */
    @Override
    public void render() {
        Vector4f background = isFocused() ? focusedColor : backgroundColor;
        GUI.drawRect(getAbsoluteX(), getAbsoluteY(), getAbsoluteWidth(), getAbsoluteHeight(), new Vector4f(background.x, background.y, background.z, background.w * getWorldOpacity()));

        float textX = getAbsoluteX() + 8.0f - scrollOffset;
        float textY = GUI.getCenteredTextY(text.toString(), font, getAbsoluteY(), getAbsoluteHeight());

        int selectionStart = getSelectionStart();
        int selectionEnd = getSelectionEnd();

        if (selectionStart != selectionEnd) {
            float selectionX = textX + getTextWidth(text.substring(0, selectionStart));
            float selectionWidth = getTextWidth(text.substring(selectionStart, selectionEnd));
            GUI.drawRect(selectionX, getAbsoluteY() + 5.0f, selectionWidth, getAbsoluteHeight() - 10.0f, new Vector4f(selectionColor.x, selectionColor.y, selectionColor.z, selectionColor.w * getWorldOpacity()));
        }

        GUI.pushScissor(getAbsoluteX() + 8.0f, getAbsoluteY(), getAbsoluteWidth() - 16.0f, getAbsoluteHeight());
        GUI.drawString(text.toString(), textX, textY, font, new Vector4f(textColor.x, textColor.y, textColor.z, textColor.w * getWorldOpacity()));

        if (isFocused() && cursorVisible) {
            String beforeCursor = text.substring(0, cursorPosition);
            float cursorX = textX + getTextWidth(beforeCursor);

            GUI.drawRect(cursorX, getAbsoluteY() + 6.0f, 1.0f, getAbsoluteHeight() - 12.0f,
                    new Vector4f(cursorColor.x, cursorColor.y, cursorColor.z, cursorColor.w * getWorldOpacity()));
        }

        GUI.popScissor();
        renderChildren();
    }

    /**
     * Performs the key pressed operation.
     * @param key the key value
     * @param scancode the scancode value
     * @param modifiers the modifiers value
     * @return the key pressed result
     */
    @Override
    public boolean keyPressed(int key, int scancode, int modifiers) {
        if (!isActuallyVisible() || !isActuallyEnabled()) {
            return false;
        }

        if (!isFocused()) {
            return super.keyPressed(key, scancode, modifiers);
        }

        boolean shift = (modifiers & GLFW.GLFW_MOD_SHIFT) != 0;
        boolean control = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0;

        if (key == GLFW.GLFW_KEY_TAB) {
            if (!shift) {
                completeForward();
            } else {
                completeBackward();
            }
            return true;
        }

        switch (key) {
            case GLFW.GLFW_KEY_BACKSPACE -> {
                if (hasSelection()) {
                    deleteSelection();
                } else if (cursorPosition > 0) {
                    int previous = previousCodePoint(cursorPosition);
                    text.delete(previous, cursorPosition);
                    cursorPosition = previous;
                    selectionAnchor = cursorPosition;
                }

                resetCompletion();
                resetCursorBlink();
                return true;
            }

            case GLFW.GLFW_KEY_DELETE -> {
                if (hasSelection()) {
                    deleteSelection();
                } else if (cursorPosition < text.length()) {
                    int next = nextCodePoint(cursorPosition);
                    text.delete(cursorPosition, next);
                }

                resetCompletion();
                resetCursorBlink();
                return true;
            }

            case GLFW.GLFW_KEY_LEFT -> {
                if (control) {
                    moveCursorWordLeft(shift);
                } else {
                    moveCursorLeft(shift);
                }

                resetCursorBlink();
                return true;
            }

            case GLFW.GLFW_KEY_RIGHT -> {
                if (control) {
                    moveCursorWordRight(shift);
                } else {
                    moveCursorRight(shift);
                }

                resetCursorBlink();
                return true;
            }

            case GLFW.GLFW_KEY_HOME -> {
                cursorPosition = 0;
                if (!shift) {
                    selectionAnchor = cursorPosition;
                }

                resetCursorBlink();
                return true;
            }

            case GLFW.GLFW_KEY_END -> {
                cursorPosition = text.length();
                if (!shift) {
                    selectionAnchor = cursorPosition;
                }

                resetCursorBlink();
                return true;
            }

            case GLFW.GLFW_KEY_A -> {
                if (control) {
                    selectionAnchor = 0;
                    cursorPosition = text.length();
                    resetCursorBlink();
                    return true;
                }
            }

            case GLFW.GLFW_KEY_C -> {
                if (control && hasSelection()) {
                    copySelection();
                    return true;
                }
            }

            case GLFW.GLFW_KEY_X -> {
                if (control && hasSelection()) {
                    copySelection();
                    deleteSelection();
                    resetCursorBlink();
                    return true;
                }
            }

            case GLFW.GLFW_KEY_V -> {
                if (control) {
                    pasteClipboard();
                    resetCursorBlink();
                    return true;
                }
            }

            case GLFW.GLFW_KEY_ENTER -> {
                return true;
            }
        }

        return true;
    }

    /**
     * Performs the char typed operation.
     * @param codepoint the codepoint value
     * @return the char typed result
     */
    @Override
    public boolean charTyped(int codepoint) {
        if (!isActuallyVisible() || !isActuallyEnabled()) {
            return false;
        }

        if (!isFocused()) {
            return super.charTyped(codepoint);
        }

        if (codepoint == '\n' || codepoint == '\r') {
            return true;
        }

        if (Character.isISOControl(codepoint)) {
            return true;
        }

        int charCount = Character.charCount(codepoint);

        if (hasSelection()) {
            int selectedLength = getSelectionEnd() - getSelectionStart();
            int newLength = text.length() - selectedLength + charCount;

            if (newLength > maxLength) {
                return true;
            }

            deleteSelection();
        } else if (text.length() + charCount > maxLength) {
            return true;
        }

        char[] chars = Character.toChars(codepoint);

        text.insert(cursorPosition, chars);
        cursorPosition += chars.length;
        selectionAnchor = cursorPosition;

        resetCompletion();
        resetCursorBlink();

        return true;
    }

    /**
     * Sets the completion provider.
     * @param completionProvider the completion provider value
     */
    public void setCompletionProvider(CompletionProvider completionProvider) {
        this.completionProvider = completionProvider;
        resetCompletion();
    }

    /**
     * Performs the reset completion operation.
     */
    private void resetCompletion() {
        completions = List.of();
        completionIndex = -1;
        completionInput = "";
        completionCursor = -1;
        completionTokenStart = -1;
    }

    /**
     * Performs the complete forward operation.
     */
    private void completeForward() {
        if (completionProvider == null) {
            return;
        }
        prepareCompletions();
        if (completions.isEmpty()) {
            return;
        }
        completionIndex++;
        if (completionIndex >= completions.size()) {
            completionIndex = 0;
        }
        applyCompletion(completions.get(completionIndex));
    }

    /**
     * Performs the complete backward operation.
     */
    private void completeBackward() {
        if (completionProvider == null) {
            return;
        }
        prepareCompletions();
        if (completions.isEmpty()) {
            return;
        }
        completionIndex--;

        if (completionIndex < 0) {
            completionIndex = completions.size() - 1;
        }
        applyCompletion(completions.get(completionIndex));
    }

    /**
     * Performs the apply completion operation.
     * @param completion the completion value
     */
    private void applyCompletion(String completion) {
        if (completion == null || completion.isEmpty()) {
            return;
        }

        int tokenStart = findCurrentTokenStart();
        int tokenEnd = findCurrentTokenEnd();
        text.replace(tokenStart, tokenEnd, completion);

        cursorPosition = tokenStart + completion.length();
        selectionAnchor = cursorPosition;
        completionCursor = cursorPosition;
        resetCursorBlink();
    }

    /**
     * Performs the prepare completions operation.
     */
    private void prepareCompletions() {
        if (!completions.isEmpty()
                && completionCursor == cursorPosition
                && completionTokenStart >= 0) {
            return;
        }

        completionInput = text.toString();
        completionCursor = cursorPosition;
        completionTokenStart = findCurrentTokenStart();

        completions = completionProvider.complete(completionInput, completionCursor);
        completionIndex = -1;
    }

    /**
     * Finds and returns the current token start.
     * @return the located current token start
     */
    private int findCurrentTokenStart() {
        int position = cursorPosition;
        while (position > 0) {
            char c = text.charAt(position - 1);
            if (Character.isWhitespace(c)) {
                break;
            }
            position--;
        }

        return position;
    }

    /**
     * Finds and returns the current token end.
     * @return the located current token end
     */
    private int findCurrentTokenEnd() {
        int position = cursorPosition;
        while (position < text.length()) {
            char c = text.charAt(position);
            if (Character.isWhitespace(c)) {
                break;
            }
            position++;
        }
        return position;
    }

    /**
     * Performs the move cursor left operation.
     * @param shift the shift value
     */
    private void moveCursorLeft(boolean shift) {
        resetCompletion();
        if (hasSelection() && !shift) {
            cursorPosition = getSelectionStart();
            selectionAnchor = cursorPosition;
            return;
        }

        if (cursorPosition > 0) {
            cursorPosition = previousCodePoint(cursorPosition);
        }

        if (!shift) {
            selectionAnchor = cursorPosition;
        }
    }

    /**
     * Performs the move cursor right operation.
     * @param shift the shift value
     */
    private void moveCursorRight(boolean shift) {
        resetCompletion();
        if (hasSelection() && !shift) {
            cursorPosition = getSelectionEnd();
            selectionAnchor = cursorPosition;
            return;
        }

        if (cursorPosition < text.length()) {
            cursorPosition = nextCodePoint(cursorPosition);
        }

        if (!shift) {
            selectionAnchor = cursorPosition;
        }
    }

    /**
     * Performs the move cursor word left operation.
     * @param shift the shift value
     */
    private void moveCursorWordLeft(boolean shift) {
        if (hasSelection() && !shift) {
            cursorPosition = getSelectionStart();
            selectionAnchor = cursorPosition;
            return;
        }

        while (cursorPosition > 0) {
            int previous = previousCodePoint(cursorPosition);
            int codePoint = text.codePointAt(previous);

            if (!Character.isWhitespace(codePoint)) {
                cursorPosition = previous;
                break;
            }

            cursorPosition = previous;
        }

        while (cursorPosition > 0) {
            int previous = previousCodePoint(cursorPosition);
            int codePoint = text.codePointAt(previous);

            if (Character.isWhitespace(codePoint)) {
                break;
            }

            cursorPosition = previous;
        }

        if (!shift) {
            selectionAnchor = cursorPosition;
        }
    }

    /**
     * Performs the move cursor word right operation.
     * @param shift the shift value
     */
    private void moveCursorWordRight(boolean shift) {
        if (hasSelection() && !shift) {
            cursorPosition = getSelectionEnd();
            selectionAnchor = cursorPosition;
            return;
        }

        while (cursorPosition < text.length()) {
            int next = nextCodePoint(cursorPosition);
            int codePoint = text.codePointAt(cursorPosition);

            cursorPosition = next;

            if (!Character.isWhitespace(codePoint)) {
                break;
            }
        }

        while (cursorPosition < text.length()) {
            int codePoint = text.codePointAt(cursorPosition);

            if (Character.isWhitespace(codePoint)) {
                break;
            }

            cursorPosition = nextCodePoint(cursorPosition);
        }

        if (!shift) {
            selectionAnchor = cursorPosition;
        }
    }

    /**
     * Performs the previous code point operation.
     * @param position the position value
     * @return the previous code point result
     */
    private int previousCodePoint(int position) {
        return Character.offsetByCodePoints(text, position, -1);
    }

    /**
     * Performs the next code point operation.
     * @param position the position value
     * @return the next code point result
     */
    private int nextCodePoint(int position) {
        return Character.offsetByCodePoints(text, position, 1);
    }

    /**
     * Checks whether the selection condition is met.
     * @return {@code true} if selection; otherwise {@code false}
     */
    private boolean hasSelection() {
        return cursorPosition != selectionAnchor;
    }

    /**
     * Returns the selection start.
     * @return the selection start
     */
    private int getSelectionStart() {
        return Math.min(cursorPosition, selectionAnchor);
    }

    /**
     * Returns the selection end.
     * @return the selection end
     */
    private int getSelectionEnd() {
        return Math.max(cursorPosition, selectionAnchor);
    }

    /**
     * Performs the delete selection operation.
     */
    private void deleteSelection() {
        if (!hasSelection()) {
            return;
        }

        int start = getSelectionStart();
        int end = getSelectionEnd();

        text.delete(start, end);
        cursorPosition = start;
        selectionAnchor = start;
    }

    /**
     * Performs the copy selection operation.
     */
    private void copySelection() {
        if (!hasSelection()) {
            return;
        }

        String selected = text.substring(getSelectionStart(), getSelectionEnd());

        GLFW.glfwSetClipboardString(GLFW.glfwGetCurrentContext(), selected);
    }

    /**
     * Performs the paste clipboard operation.
     */
    private void pasteClipboard() {
        long window = GLFW.glfwGetCurrentContext();

        String clipboard = GLFW.glfwGetClipboardString(window);

        if (clipboard == null || clipboard.isEmpty()) {
            return;
        }

        clipboard = clipboard.replace("\r", "").replace("\n", "");

        if (clipboard.isEmpty()) {
            return;
        }

        int selectionLength = getSelectionEnd() - getSelectionStart();
        int available = maxLength - (text.length() - selectionLength);

        if (available <= 0) {
            return;
        }

        int codePointCount = clipboard.codePointCount(0, clipboard.length());

        if (codePointCount > available) {
            int end = clipboard.offsetByCodePoints(0, available);
            clipboard = clipboard.substring(0, end);
        }

        if (hasSelection()) {
            deleteSelection();
        }

        text.insert(cursorPosition, clipboard);
        cursorPosition += clipboard.length();
        selectionAnchor = cursorPosition;
    }

    /**
     * Performs the reset cursor blink operation.
     */
    private void resetCursorBlink() {
        cursorTimer = 0.0f;
        cursorVisible = true;
        updateScroll();
    }

    /**
     * Updates the scroll.
     */
    private void updateScroll() {
        float availableWidth = Math.max(1.0f, getAbsoluteWidth() - 16.0f);
        float cursorX = getTextWidth(text.substring(0, cursorPosition));
        if (cursorX - scrollOffset > availableWidth) {
            scrollOffset = cursorX - availableWidth;
        }

        if (cursorX - scrollOffset < 0.0f) {
            scrollOffset = cursorX;
        }

        float textWidth = getTextWidth(text.toString());
        float maxScroll = Math.max(0.0f, textWidth - availableWidth);
        scrollOffset = Math.clamp(scrollOffset, 0.0f, maxScroll);
    }

    /**
     * Returns the text width.
     * @param value the value value
     * @return the text width
     */
    private float getTextWidth(String value) {
        float width = 0.0f;

        for (int i = 0; i < value.length(); ) {
            int codePoint = value.codePointAt(i);
            var glyph = font.getGlyph(codePoint);

            if (glyph != null) {
                width += glyph.xadvance();
            }

            i += Character.charCount(codePoint);
        }

        return width;
    }

    /**
     * Sets the focused.
     * @param focused the focused value
     */
    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);

        if (!focused) {
            cursorTimer = 0.0f;
            cursorVisible = true;
            scrollOffset = 0.0f;
        } else {
            resetCursorBlink();
        }
    }

    /**
     * Returns the text.
     * @return the text
     */
    public String getText() {
        return text.toString();
    }

    /**
     * Sets the text.
     * @param text the text value
     */
    public void setText(String text) {
        this.text.setLength(0);

        if (text == null) {
            cursorPosition = 0;
            selectionAnchor = 0;
            scrollOffset = 0.0f;
            return;
        }

        if (text.length() > maxLength) {
            this.text.append(text, 0, maxLength);
        } else {
            this.text.append(text);
        }

        cursorPosition = this.text.length();
        selectionAnchor = cursorPosition;
        scrollOffset = 0.0f;
        updateScroll();
    }

    /**
     * Removes clear.
     */
    public void clear() {
        text.setLength(0);
        cursorPosition = 0;
        selectionAnchor = 0;
        scrollOffset = 0.0f;
        resetCursorBlink();
    }

    /**
     * Returns the cursor position.
     * @return the cursor position
     */
    public int getCursorPosition() {
        return cursorPosition;
    }

    /**
     * Sets the cursor position.
     * @param position the position value
     */
    public void setCursorPosition(int position) {
        cursorPosition = Math.clamp(position, 0, text.length());
        selectionAnchor = cursorPosition;
        resetCursorBlink();
    }

    /**
     * Returns the max length.
     * @return the max length
     */
    public int getMaxLength() {
        return maxLength;
    }

    /**
     * Sets the max length.
     * @param maxLength the max length value
     */
    public void setMaxLength(int maxLength) {
        this.maxLength = Math.max(1, maxLength);

        if (text.length() > this.maxLength) {
            int end = text.offsetByCodePoints(0, this.maxLength);
            text.setLength(end);
            cursorPosition = Math.min(cursorPosition, end);
            selectionAnchor = Math.min(selectionAnchor, end);
        }

        updateScroll();
    }

    /**
     * Returns the font.
     * @return the font
     */
    public UIFont getFont() {
        return font;
    }

    /**
     * Sets the font.
     * @param font the font value
     */
    public void setFont(UIFont font) {
        if (font != null) {
            this.font = font;
            updateScroll();
        }
    }

    /**
     * Returns the background color.
     * @return the background color
     */
    public Vector4f getBackgroundColor() {
        return new Vector4f(backgroundColor);
    }

    /**
     * Sets the background color.
     * @param r the r value
     * @param g the g value
     * @param b the b value
     * @param a the a value
     */
    public void setBackgroundColor(float r, float g, float b, float a) {
        backgroundColor.set(r, g, b, a);
    }

    /**
     * Returns the focused color.
     * @return the focused color
     */
    public Vector4f getFocusedColor() {
        return new Vector4f(focusedColor);
    }

    /**
     * Sets the focused color.
     * @param r the r value
     * @param g the g value
     * @param b the b value
     * @param a the a value
     */
    public void setFocusedColor(float r, float g, float b, float a) {
        focusedColor.set(r, g, b, a);
    }

    /**
     * Returns the text color.
     * @return the text color
     */
    public Vector4f getTextColor() {
        return new Vector4f(textColor);
    }

    /**
     * Sets the text color.
     * @param r the r value
     * @param g the g value
     * @param b the b value
     * @param a the a value
     */
    public void setTextColor(float r, float g, float b, float a) {
        textColor.set(r, g, b, a);
    }

    /**
     * Returns the cursor color.
     * @return the cursor color
     */
    public Vector4f getCursorColor() {
        return new Vector4f(cursorColor);
    }

    /**
     * Sets the cursor color.
     * @param r the r value
     * @param g the g value
     * @param b the b value
     * @param a the a value
     */
    public void setCursorColor(float r, float g, float b, float a) {
        cursorColor.set(r, g, b, a);
    }

    /**
     * Returns the selection color.
     * @return the selection color
     */
    public Vector4f getSelectionColor() {
        return new Vector4f(selectionColor);
    }

    /**
     * Sets the selection color.
     * @param r the r value
     * @param g the g value
     * @param b the b value
     * @param a the a value
     */
    public void setSelectionColor(float r, float g, float b, float a) {
        selectionColor.set(r, g, b, a);
    }

    /**
     * Checks whether the selection text condition is met.
     * @return {@code true} if selection text; otherwise {@code false}
     */
    public boolean hasSelectionText() {
        return hasSelection();
    }

    /**
     * Returns the selected text.
     * @return the selected text
     */
    public String getSelectedText() {
        if (!hasSelection()) {
            return "";
        }

        return text.substring(getSelectionStart(), getSelectionEnd());
    }
}