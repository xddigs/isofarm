package com.isofarm.gui;

import com.isofarm.input.Keyboard;
import com.isofarm.input.Mouse;
import org.lwjgl.glfw.GLFW;

public class UIManager {
    private final UIPanel root;
    private final UITooltip tooltip;
    private UIElement focusedElement;

    public UIManager(float width, float height) {
        root = new UIPanel(0.0f, 0.0f, width, height);
        tooltip = new UITooltip();
        tooltip.setInteractable(false);
        tooltip.setLayer(Integer.MAX_VALUE);
        root.addChild(tooltip);
    }

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

        for (int button = GLFW.GLFW_MOUSE_BUTTON_1;
             button <= GLFW.GLFW_MOUSE_BUTTON_LAST;
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
            for (int key = GLFW.GLFW_KEY_SPACE;
                 key <= GLFW.GLFW_KEY_LAST;
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

    public void render() {
        GUI.render(root);
    }

    public UIPanel getRoot() {
        return root;
    }

    public UITooltip getTooltip() {
        return tooltip;
    }

    public UIElement getFocusedElement() {
        return focusedElement;
    }

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

    public void clearFocus() {
        setFocusedElement(null);
    }

    public void resize(float width, float height) {
        root.setSize(width, height);
        GUI.resize((int) width, (int) height);
    }
}