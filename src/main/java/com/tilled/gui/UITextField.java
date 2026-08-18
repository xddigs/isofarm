package com.tilled.gui;

import com.tilled.data.UIElement;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;

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

    public UITextField(float x, float y, float width, float height) {
        super(x, y, width, height);
        setFocusable(true);
    }

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

    @Override
    public void render() {
        Vector4f background = isFocused() ? focusedColor : backgroundColor;
        GUI.drawRect(getAbsoluteX(), getAbsoluteY(), getAbsoluteWidth(),
                getAbsoluteHeight(), new Vector4f(background.x, background.y,
                        background.z, background.w * getWorldOpacity()));

        float textX = getAbsoluteX() + 8.0f - scrollOffset;
        float textY = getAbsoluteY() + (getAbsoluteHeight() - font.getSize()) * 0.5f + font.getSize();

        int selectionStart = getSelectionStart();
        int selectionEnd = getSelectionEnd();

        if (selectionStart != selectionEnd) {
            float selectionX = textX + getTextWidth(text.substring(0, selectionStart));
            float selectionWidth = getTextWidth(text.substring(selectionStart, selectionEnd));
            GUI.drawRect(selectionX, getAbsoluteY() + 5.0f, selectionWidth,
                    getAbsoluteHeight() - 10.0f, new Vector4f(selectionColor.x,
                            selectionColor.y, selectionColor.z, selectionColor.w * getWorldOpacity()));
        }

        GUI.pushScissor(getAbsoluteX() + 8.0f, getAbsoluteY(), getAbsoluteWidth() - 16.0f, getAbsoluteHeight());
        GUI.drawString(text.toString(), textX, textY, font, new Vector4f(textColor.x,
                textColor.y, textColor.z, textColor.w * getWorldOpacity()));

        if (isFocused() && cursorVisible) {
            String beforeCursor = text.substring(0, cursorPosition);
            float cursorX = textX + getTextWidth(beforeCursor);

            GUI.drawRect(cursorX, getAbsoluteY() + 6.0f, 1.0f,
                    getAbsoluteHeight() - 12.0f, new Vector4f(cursorColor.x,
                            cursorColor.y, cursorColor.z, cursorColor.w * getWorldOpacity()));
        }

        GUI.popScissor();
        renderChildren();
    }

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

        resetCursorBlink();

        return true;
    }

    private void moveCursorLeft(boolean shift) {
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

    private void moveCursorRight(boolean shift) {
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

    private int previousCodePoint(int position) {
        return Character.offsetByCodePoints(text, position, -1);
    }

    private int nextCodePoint(int position) {
        return Character.offsetByCodePoints(text, position, 1);
    }

    private boolean hasSelection() {
        return cursorPosition != selectionAnchor;
    }

    private int getSelectionStart() {
        return Math.min(cursorPosition, selectionAnchor);
    }

    private int getSelectionEnd() {
        return Math.max(cursorPosition, selectionAnchor);
    }

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

    private void copySelection() {
        if (!hasSelection()) {
            return;
        }

        String selected = text.substring(
                getSelectionStart(),
                getSelectionEnd()
        );

        GLFW.glfwSetClipboardString(
                GLFW.glfwGetCurrentContext(),
                selected
        );
    }

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

    private void resetCursorBlink() {
        cursorTimer = 0.0f;
        cursorVisible = true;
        updateScroll();
    }

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

    public String getText() {
        return text.toString();
    }

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

    public void clear() {
        text.setLength(0);
        cursorPosition = 0;
        selectionAnchor = 0;
        scrollOffset = 0.0f;
        resetCursorBlink();
    }

    public int getCursorPosition() {
        return cursorPosition;
    }

    public void setCursorPosition(int position) {
        cursorPosition = Math.clamp(position, 0, text.length());
        selectionAnchor = cursorPosition;
        resetCursorBlink();
    }

    public int getMaxLength() {
        return maxLength;
    }

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

    public UIFont getFont() {
        return font;
    }

    public void setFont(UIFont font) {
        if (font != null) {
            this.font = font;
            updateScroll();
        }
    }

    public Vector4f getBackgroundColor() {
        return new Vector4f(backgroundColor);
    }

    public void setBackgroundColor(float r, float g, float b, float a) {
        backgroundColor.set(r, g, b, a);
    }

    public Vector4f getFocusedColor() {
        return new Vector4f(focusedColor);
    }

    public void setFocusedColor(float r, float g, float b, float a) {
        focusedColor.set(r, g, b, a);
    }

    public Vector4f getTextColor() {
        return new Vector4f(textColor);
    }

    public void setTextColor(float r, float g, float b, float a) {
        textColor.set(r, g, b, a);
    }

    public Vector4f getCursorColor() {
        return new Vector4f(cursorColor);
    }

    public void setCursorColor(float r, float g, float b, float a) {
        cursorColor.set(r, g, b, a);
    }

    public Vector4f getSelectionColor() {
        return new Vector4f(selectionColor);
    }

    public void setSelectionColor(float r, float g, float b, float a) {
        selectionColor.set(r, g, b, a);
    }

    public boolean hasSelectionText() {
        return hasSelection();
    }

    public String getSelectedText() {
        if (!hasSelection()) {
            return "";
        }

        return text.substring(getSelectionStart(), getSelectionEnd());
    }
}