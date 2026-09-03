package com.isofarm.gui;

import com.isofarm.input.Keyboard;
import com.isofarm.input.Mouse;

/**
 * Provides uimanager behavior.
 */
public class UIManager {
    private final UIPanel root;
    private final UITooltip tooltip;
    private UIElement focusedElement;

    /**
     * Creates a new {@code UIManager} instance.
     * @param width the width value
     * @param height the height value
     */
    public UIManager(float width, float height) {
        root = new UIPanel(0.0f, 0.0f, width, height);
        tooltip = new UITooltip();
        tooltip.setInteractable(false);
        tooltip.setLayer(Integer.MAX_VALUE);
        root.addChild(tooltip);
    }

    /**
     * Updates the current state.
     * @param delta the delta value
     */
    public void update(float delta) {
        root.update(delta);
        root.mouseMoved(Mouse.getX(), Mouse.getY());

        UIElement hovered = root.findElementAt(Mouse.getX(), Mouse.getY());
        if (hovered != null
                && hovered != tooltip
                && hovered.getTooltipText() != null
                && !hovered.getTooltipText().isBlank()) {
            tooltip.text(hovered.getTooltipText());
            tooltip.updatePosition(
                    Mouse.getX(),
                    Mouse.getY(),
                    root.getWidth(),
                    root.getHeight()
            );
            tooltip.show();
        } else {
            tooltip.hide();
        }

        for (int button = Mouse.BUTTON_1;
             button <= Mouse.BUTTON_LAST;
             button++) {

            if (Mouse.isButtonPressed(button)) {
                UIElement element = root.findElementAt(Mouse.getX(), Mouse.getY());

                if (element != null && element.isFocusable()) {
                    setFocusedElement(element);
                } else {
                    clearFocus();
                }

                root.mousePressed(Mouse.getX(), Mouse.getY(), button);
            }

            if (Mouse.isButtonReleased(button)) {
                root.mouseReleased(Mouse.getX(), Mouse.getY(), button);
            }
        }

        if (Mouse.getScrollY() != 0.0f) {
            root.mouseScrolled(
                    Mouse.getX(),
                    Mouse.getY(),
                    0.0f,
                    Mouse.getScrollY()
            );
        }

        if (focusedElement != null) {
            for (int key = Keyboard.KEY_SPACE;
                 key <= Keyboard.KEY_LAST;
                 key++) {

                if (Keyboard.isKeyPressed(key)) {
                    focusedElement.keyPressed(key, 0, Keyboard.getModifiers());
                }

                if (Keyboard.isKeyReleased(key)) {
                    focusedElement.keyReleased(key, 0, 0);
                }
            }

            String typedCharacters = Keyboard.getTypedCharacters();

            for (int i = 0; i < typedCharacters.length();) {
                int codePoint = typedCharacters.codePointAt(i);

                if (focusedElement.charTyped(codePoint)) {
                    break;
                }

                i += Character.charCount(codePoint);
            }

            if (!focusedElement.isFocused()) {
                clearFocus();
            }
        } else {
            Keyboard.getTypedCharacters();
        }
    }

    /**
     * Renders render.
     */
    public void render() {
        GUI.render(root);
    }

    /**
     * Returns the root.
     * @return the root
     */
    public UIPanel getRoot() {
        return root;
    }

    /**
     * Returns the tooltip.
     * @return the tooltip
     */
    public UITooltip getTooltip() {
        return tooltip;
    }

    /**
     * Performs the show tooltip operation.
     * @param text the text value
     * @param mouseX the mouse x value
     * @param mouseY the mouse y value
     */
    public void showTooltip(String text, float mouseX, float mouseY) {
        if (text == null || text.isBlank()) {
            tooltip.hide();
            return;
        }

        tooltip.text(text);
        tooltip.updatePosition(mouseX, mouseY, root.getWidth(), root.getHeight());
        tooltip.show();
    }

    /**
     * Performs the hide tooltip operation.
     */
    public void hideTooltip() {
        tooltip.hide();
    }

    /**
     * Returns the focused element.
     * @return the focused element
     */
    public UIElement getFocusedElement() {
        return focusedElement;
    }

    /**
     * Sets the focused element.
     * @param element the element value
     */
    public void setFocusedElement(UIElement element) {
        if (focusedElement == element) {
            return;
        }

        if (focusedElement != null) {
            focusedElement.setFocused(false);
        }

        focusedElement = null;

        if (element != null
                && element.isFocusable()
                && element.isActuallyVisible()
                && element.isActuallyEnabled()) {

            focusedElement = element;
            focusedElement.setFocused(true);
        }
    }

    /**
     * Clears the focus.
     */
    public void clearFocus() {
        setFocusedElement(null);
    }

    /**
     * Performs the resize operation.
     * @param width the width value
     * @param height the height value
     */
    public void resize(float width, float height) {
        root.setSize(width, height);
        GUI.resize((int) width, (int) height);
    }
}
