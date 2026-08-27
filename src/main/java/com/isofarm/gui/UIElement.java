package com.isofarm.gui;

import com.isofarm.data.DataClass;
import com.isofarm.graphics.SpriteSheet;
import com.isofarm.graphics.Texture;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings("unused")
@DataClass
public abstract class UIElement {
    private final List<UIElement> children = new ArrayList<>();
    private final Vector4f tint = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
    private final Vector2f spriteOffset = new Vector2f();
    private UIElement parent;
    private float x;
    private float y;
    private float width;
    private float height;
    private float scaleX = 1.0f;
    private float scaleY = 1.0f;
    private float rotation;
    private float pivotX = 0.5f;
    private float pivotY = 0.5f;
    private float opacity = 1.0f;
    private int zIndex;
    private boolean visible = true;
    private boolean enabled = true;
    private boolean interactable = true;
    private boolean focusable;
    private boolean hovered;
    private boolean pressed;
    private boolean focused;
    private boolean mouseInside;
    private boolean clipChildren;
    private Anchor anchor = Anchor.TOP_LEFT;
    private Texture sprite;
    private SpriteSheet spriteSheet;
    private String tooltipText;
    private int spriteCol;
    private int spriteRow;
    private int spriteFrameCount = 1;
    private boolean spriteAnimated;
    private boolean spriteLoop = true;
    private float spriteFrameDuration = 0.1f;
    private float spriteAnimationTimer;
    private boolean spriteFlipX;
    private boolean spriteFlipY;
    private float spriteScaleX = 1.0f;
    private float spriteScaleY = 1.0f;
    private Consumer<UIElement> clickListener;
    private Consumer<UIElement> mouseEnterListener;
    private Consumer<UIElement> mouseExitListener;
    private Consumer<UIElement> mousePressListener;
    private Consumer<UIElement> mouseReleaseListener;
    private Consumer<UIElement> focusListener;
    private Consumer<UIElement> blurListener;

    protected UIElement(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public abstract void render();

    public void update(float delta) {
        if (!visible) {
            return;
        }

        updateSpriteAnimation(delta);

        for (UIElement child : getSortedChildren()) {
            child.update(delta);
        }
    }

    protected void updateSpriteAnimation(float delta) {
        if (!spriteAnimated || spriteSheet == null || spriteFrameCount <= 1) {
            return;
        }

        spriteAnimationTimer += delta;

        while (spriteAnimationTimer >= spriteFrameDuration) {
            spriteAnimationTimer -= spriteFrameDuration;
            spriteCol++;
            if (spriteCol >= spriteFrameCount) {
                if (spriteLoop) {
                    spriteCol = 0;
                } else {
                    spriteCol = spriteFrameCount - 1;
                    spriteAnimated = false;
                }
            }
        }
    }

    public void renderChildren() {
        for (UIElement child : getSortedChildren()) {
            if (child.isVisible()) {
                child.render();
            }
        }
    }

    public void addChild(UIElement child) {
        if (child == null || child == this) {
            return;
        }

        if (child.parent != null) {
            child.parent.removeChild(child);
        }

        child.parent = this;
        children.add(child);
        sortChildren();
    }

    public void removeChild(UIElement child) {
        if (child == null) {
            return;
        }

        if (children.remove(child)) {
            child.parent = null;
        }
    }

    public void removeAllChildren() {
        for (UIElement child : children) {
            child.parent = null;
        }

        children.clear();
    }

    public List<UIElement> getChildren() {
        return List.copyOf(children);
    }

    private void sortChildren() {
        children.sort(Comparator.comparingInt(UIElement::getZIndex));
    }

    public UIElement getParent() {
        return parent;
    }

    public void setParent(UIElement parent) {
        if (this.parent == parent) {
            return;
        }

        if (this.parent != null) {
            this.parent.removeChild(this);
        }

        if (parent != null) {
            parent.addChild(this);
        }
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public UIElement setPosition(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = Math.max(0.0f, width);
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = Math.max(0.0f, height);
    }

    public UIElement setSize(float width, float height) {
        this.width = Math.max(0.0f, width);
        this.height = Math.max(0.0f, height);
        return this;
    }

    public float getScaleX() {
        return scaleX;
    }

    public void setScaleX(float scaleX) {
        this.scaleX = scaleX;
    }

    public float getScaleY() {
        return scaleY;
    }

    public void setScaleY(float scaleY) {
        this.scaleY = scaleY;
    }

    public UIElement setScale(float scale) {
        this.scaleX = scale;
        this.scaleY = scale;
        return this;
    }

    public UIElement setScale(float scaleX, float scaleY) {
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        return this;
    }

    public float getRotation() {
        return rotation;
    }

    public UIElement setRotation(float rotation) {
        this.rotation = rotation;
        return this;
    }

    public float getPivotX() {
        return pivotX;
    }

    public void setPivotX(float pivotX) {
        this.pivotX = pivotX;
    }

    public float getPivotY() {
        return pivotY;
    }

    public void setPivotY(float pivotY) {
        this.pivotY = pivotY;
    }

    public UIElement setPivot(float pivotX, float pivotY) {
        this.pivotX = pivotX;
        this.pivotY = pivotY;
        return this;
    }

    public float getOpacity() {
        return opacity;
    }

    public void setOpacity(float opacity) {
        this.opacity = Math.clamp(opacity, 0.0f, 1.0f);
    }

    public UIElement setOpacityValue(float opacity) {
        setOpacity(opacity);
        return this;
    }

    public int getZIndex() {
        return zIndex;
    }

    public void setZIndex(int zIndex) {
        this.zIndex = zIndex;

        if (parent != null) {
            parent.sortChildren();
        }
    }

    public UIElement setLayer(int zIndex) {
        setZIndex(zIndex);
        return this;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;

        if (!visible) {
            setHovered(false);
            setPressed(false);
        }
    }

    public UIElement show() {
        setVisible(true);
        return this;
    }

    public UIElement hide() {
        setVisible(false);
        return this;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;

        if (!enabled) {
            setHovered(false);
            setPressed(false);
            setFocused(false);
        }
    }

    public UIElement enable() {
        setEnabled(true);
        return this;
    }

    public UIElement disable() {
        setEnabled(false);
        return this;
    }

    public String getTooltipText() {
        return tooltipText;
    }

    public void setTooltipText(String tooltipText) {
        this.tooltipText = tooltipText;
    }

    public UIElement tooltip(String tooltipText) {
        this.tooltipText = tooltipText;
        return this;
    }

    public boolean isInteractable() {
        return interactable;
    }

    public void setInteractable(boolean interactable) {
        this.interactable = interactable;
    }

    public boolean isFocusable() {
        return focusable;
    }

    public void setFocusable(boolean focusable) {
        this.focusable = focusable;
    }

    public boolean isHovered() {
        return hovered;
    }

    public void setHovered(boolean hovered) {
        if (this.hovered == hovered) {
            return;
        }

        this.hovered = hovered;

        if (hovered) {
            if (mouseEnterListener != null) {
                mouseEnterListener.accept(this);
            }
        } else {
            if (mouseExitListener != null) {
                mouseExitListener.accept(this);
            }
        }
    }

    public boolean isPressed() {
        return pressed;
    }

    public void setPressed(boolean pressed) {
        this.pressed = pressed;
    }

    public boolean isFocused() {
        return focused;
    }

    public void setFocused(boolean focused) {
        if (this.focused == focused) {
            return;
        }

        this.focused = focused;

        if (focused) {
            if (focusListener != null) {
                focusListener.accept(this);
            }
        } else {
            if (blurListener != null) {
                blurListener.accept(this);
            }
        }
    }

    public boolean isMouseInside() {
        return mouseInside;
    }

    public void setMouseInside(boolean mouseInside) {
        this.mouseInside = mouseInside;
    }

    public boolean isClipChildren() {
        return clipChildren;
    }

    public void setClipChildren(boolean clipChildren) {
        this.clipChildren = clipChildren;
    }

    public Anchor getAnchor() {
        return anchor;
    }

    public void setAnchor(Anchor anchor) {
        this.anchor = anchor == null ? Anchor.TOP_LEFT : anchor;
    }

    public UIElement anchor(Anchor anchor) {
        setAnchor(anchor);
        return this;
    }

    public Vector4f getTint() {
        return new Vector4f(tint);
    }

    public void setTint(Vector4f tint) {
        if (tint == null) {
            this.tint.set(1.0f, 1.0f, 1.0f, 1.0f);
            return;
        }

        this.tint.set(tint);
    }

    public void setTint(float r, float g, float b, float a) {
        tint.set(r, g, b, a);
    }

    public UIElement tint(float r, float g, float b, float a) {
        setTint(r, g, b, a);
        return this;
    }

    public UIElement tint(float r, float g, float b) {
        setTint(r, g, b, 1.0f);
        return this;
    }

    public Texture getSprite() {
        return sprite;
    }

    public void setSprite(Texture sprite) {
        this.sprite = sprite;
        this.spriteSheet = null;
        this.spriteCol = 0;
        this.spriteFrameCount = 1;
        this.spriteAnimated = false;
    }

    public UIElement sprite(Texture sprite) {
        setSprite(sprite);
        return this;
    }

    public SpriteSheet getSpriteSheet() {
        return spriteSheet;
    }

    public void setSpriteSheet(SpriteSheet spriteSheet) {
        this.spriteSheet = spriteSheet;
        this.sprite = null;
        this.spriteCol = 0;
        this.spriteFrameCount = spriteSheet != null ? spriteSheet.getTotalFrames() : 1;
        this.spriteAnimationTimer = 0.0f;
    }

    public UIElement spriteSheet(SpriteSheet spriteSheet) {
        setSpriteSheet(spriteSheet);
        return this;
    }

    public boolean hasSprite() {
        return sprite != null || spriteSheet != null;
    }

    public boolean hasStaticSprite() {
        return sprite != null;
    }

    public boolean hasSpriteSheet() {
        return spriteSheet != null;
    }

    public int getSpriteCol() {
        return spriteCol;
    }

    public void setSpriteColumn(int spriteFrame) {
        if (spriteSheet == null) {
            this.spriteCol = 0;
            return;
        }

        this.spriteCol = Math.clamp(spriteFrame, 0, spriteFrameCount - 1);
    }

    public int getSpriteRow() {
        return spriteRow;
    }

    public void setSpriteRow(int spriteRow) {
        if (spriteSheet == null) {
            this.spriteRow = 0;
            return;
        }

        this.spriteRow = Math.clamp(spriteRow, 0, spriteFrameCount - 1);
    }

    public UIElement col(int frame) {
        setSpriteColumn(frame);
        return this;
    }

    public UIElement row(int row) {
        setSpriteRow(row);
        return this;
    }

    public int getSpriteFrameCount() {
        return spriteFrameCount;
    }

    public void setSpriteFrameCount(int spriteFrameCount) {
        this.spriteFrameCount = Math.max(1, spriteFrameCount);

        if (spriteCol >= this.spriteFrameCount) {
            spriteCol = this.spriteFrameCount - 1;
        }
    }

    public float getSpriteFrameDuration() {
        return spriteFrameDuration;
    }

    public void setSpriteFrameDuration(float spriteFrameDuration) {
        this.spriteFrameDuration = Math.max(0.001f, spriteFrameDuration);
    }

    public UIElement frameDuration(float duration) {
        setSpriteFrameDuration(duration);
        return this;
    }

    public boolean isSpriteAnimated() {
        return spriteAnimated;
    }

    public void setSpriteAnimated(boolean spriteAnimated) {
        this.spriteAnimated = spriteAnimated && spriteSheet != null && spriteFrameCount > 1;
    }

    public UIElement animateSprite(boolean animate) {
        setSpriteAnimated(animate);
        return this;
    }

    public boolean isSpriteLooping() {
        return spriteLoop;
    }

    public void setSpriteLooping(boolean spriteLoop) {
        this.spriteLoop = spriteLoop;
    }

    public UIElement loopSprite(boolean loop) {
        setSpriteLooping(loop);
        return this;
    }

    public float getSpriteAnimationTimer() {
        return spriteAnimationTimer;
    }

    public void resetSpriteAnimation() {
        spriteCol = 0;
        spriteAnimationTimer = 0.0f;
    }

    public boolean isSpriteFlipX() {
        return spriteFlipX;
    }

    public void setSpriteFlipX(boolean spriteFlipX) {
        this.spriteFlipX = spriteFlipX;
    }

    public UIElement flipX(boolean flip) {
        setSpriteFlipX(flip);
        return this;
    }

    public boolean isSpriteFlipY() {
        return spriteFlipY;
    }

    public void setSpriteFlipY(boolean spriteFlipY) {
        this.spriteFlipY = spriteFlipY;
    }

    public UIElement flipY(boolean flip) {
        setSpriteFlipY(flip);
        return this;
    }

    public float getSpriteScaleX() {
        return spriteScaleX;
    }

    public void setSpriteScaleX(float spriteScaleX) {
        this.spriteScaleX = spriteScaleX;
    }

    public float getSpriteScaleY() {
        return spriteScaleY;
    }

    public void setSpriteScaleY(float spriteScaleY) {
        this.spriteScaleY = spriteScaleY;
    }

    public UIElement setSpriteScale(float scale) {
        this.spriteScaleX = scale;
        this.spriteScaleY = scale;
        return this;
    }

    public Vector2f getSpriteOffset() {
        return new Vector2f(spriteOffset);
    }

    public void setSpriteOffset(float x, float y) {
        spriteOffset.set(x, y);
    }

    public UIElement spriteOffset(float x, float y) {
        setSpriteOffset(x, y);
        return this;
    }

    public float getAbsoluteX() {
        if (parent == null) {
            return x;
        }

        return parent.getContentX() + x;
    }

    public float getAbsoluteY() {
        if (parent == null) {
            return y;
        }

        return parent.getContentY() + y;
    }

    public float getContentX() {
        return getAbsoluteX();
    }

    public float getContentY() {
        return getAbsoluteY();
    }

    public float getAbsoluteWidth() {
        return width * scaleX;
    }

    public float getAbsoluteHeight() {
        return height * scaleY;
    }

    public Vector2f getAbsolutePosition() {
        return new Vector2f(getAbsoluteX(), getAbsoluteY());
    }

    public Vector2f getAbsoluteSize() {
        return new Vector2f(getAbsoluteWidth(), getAbsoluteHeight());
    }

    public boolean contains(float mouseX, float mouseY) {
        if (!visible || !enabled || !interactable) {
            return false;
        }

        float absoluteX = getAbsoluteX();
        float absoluteY = getAbsoluteY();
        float absoluteWidth = getAbsoluteWidth();
        float absoluteHeight = getAbsoluteHeight();

        return mouseX >= absoluteX &&
                mouseX <= absoluteX + absoluteWidth &&
                mouseY >= absoluteY &&
                mouseY <= absoluteY + absoluteHeight;
    }

    public UIElement findElementAt(float mouseX, float mouseY) {
        if (!visible || !enabled || !interactable) {
            return null;
        }

        List<UIElement> sorted = new ArrayList<>(children);
        sorted.sort(Comparator.comparingInt(UIElement::getZIndex).reversed());

        for (UIElement child : sorted) {
            UIElement found = child.findElementAt(mouseX, mouseY);

            if (found != null) {
                return found;
            }
        }

        return contains(mouseX, mouseY) ? this : null;
    }

    public void mouseMoved(float mouseX, float mouseY) {
        if (!visible || !enabled) {
            return;
        }

        boolean inside = contains(mouseX, mouseY);
        setMouseInside(inside);
        setHovered(inside);

        for (UIElement child : getSortedChildren()) {
            child.mouseMoved(mouseX, mouseY);
        }
    }

    public boolean mousePressed(float mouseX, float mouseY, int button) {
        if (!visible || !enabled || !interactable) {
            return false;
        }

        List<UIElement> sorted = new ArrayList<>(children);
        sorted.sort(Comparator.comparingInt(UIElement::getZIndex).reversed());

        for (UIElement child : sorted) {
            if (child.mousePressed(mouseX, mouseY, button)) {
                return true;
            }
        }

        if (!contains(mouseX, mouseY)) {
            return false;
        }

        pressed = true;

        if (mousePressListener != null) {
            mousePressListener.accept(this);
        }

        return true;
    }

    public boolean mouseReleased(float mouseX, float mouseY, int button) {
        if (!visible || !enabled || !interactable) {
            return false;
        }

        boolean wasPressed = pressed;
        pressed = false;

        List<UIElement> sorted = new ArrayList<>(children);
        sorted.sort(Comparator.comparingInt(UIElement::getZIndex).reversed());

        for (UIElement child : sorted) {
            if (child.mouseReleased(mouseX, mouseY, button)) {
                return true;
            }
        }

        if (!wasPressed) {
            return false;
        }

        if (contains(mouseX, mouseY)) {
            if (mouseReleaseListener != null) {
                mouseReleaseListener.accept(this);
            }

            if (clickListener != null) {
                clickListener.accept(this);
            }

            return true;
        }

        return false;
    }

    public boolean mouseScrolled(float mouseX, float mouseY, float scrollX, float scrollY) {
        if (!visible || !enabled || !interactable) {
            return false;
        }

        List<UIElement> sorted = new ArrayList<>(children);
        sorted.sort(Comparator.comparingInt(UIElement::getZIndex).reversed());

        for (UIElement child : sorted) {
            if (child.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
                return true;
            }
        }

        return contains(mouseX, mouseY);
    }

    public boolean keyPressed(int key, int scancode, int modifiers) {
        if (!visible || !enabled) {
            return false;
        }

        List<UIElement> sorted = new ArrayList<>(children);
        sorted.sort(Comparator.comparingInt(UIElement::getZIndex).reversed());

        for (UIElement child : sorted) {
            if (child.keyPressed(key, scancode, modifiers)) {
                return true;
            }
        }

        return false;
    }

    public boolean keyReleased(int key, int scancode, int modifiers) {
        if (!visible || !enabled) {
            return false;
        }

        List<UIElement> sorted = new ArrayList<>(children);
        sorted.sort(Comparator.comparingInt(UIElement::getZIndex).reversed());

        for (UIElement child : sorted) {
            if (child.keyReleased(key, scancode, modifiers)) {
                return true;
            }
        }

        return false;
    }

    public boolean charTyped(int codepoint) {
        if (!visible || !enabled) {
            return false;
        }

        List<UIElement> sorted = new ArrayList<>(children);
        sorted.sort(Comparator.comparingInt(UIElement::getZIndex).reversed());

        for (UIElement child : sorted) {
            if (child.charTyped(codepoint)) {
                return true;
            }
        }

        return false;
    }

    public void onClick(Consumer<UIElement> listener) {
        this.clickListener = listener;
    }

    public void onMouseEnter(Consumer<UIElement> listener) {
        this.mouseEnterListener = listener;
    }

    public void onMouseExit(Consumer<UIElement> listener) {
        this.mouseExitListener = listener;
    }

    public void onMousePress(Consumer<UIElement> listener) {
        this.mousePressListener = listener;
    }

    public void onMouseRelease(Consumer<UIElement> listener) {
        this.mouseReleaseListener = listener;
    }

    public void onFocus(Consumer<UIElement> listener) {
        this.focusListener = listener;
    }

    public void onBlur(Consumer<UIElement> listener) {
        this.blurListener = listener;
    }

    public List<UIElement> getSortedChildren() {
        List<UIElement> sorted = new ArrayList<>(children);
        sorted.sort(Comparator.comparingInt(UIElement::getZIndex));
        return sorted;
    }

    public float getWorldOpacity() {
        if (parent == null) {
            return opacity;
        }

        return parent.getWorldOpacity() * opacity;
    }

    public boolean isActuallyVisible() {
        if (!visible) {
            return false;
        }

        return parent == null || parent.isActuallyVisible();
    }

    public boolean isActuallyEnabled() {
        if (!enabled) {
            return false;
        }

        return parent == null || parent.isActuallyEnabled();
    }

    public void dispose() {
        for (UIElement child : children) {
            child.dispose();
        }

        children.clear();
        parent = null;
    }

    @Override
    public String toString() {
        return "UIElement {" +
                "name=" + getClass().getSimpleName() +
                ", children=" + children.size() +
                ", parent=" + parent.getClass().getSimpleName() +
                ", x=" + x +
                ", y=" + y +
                ", width=" + width +
                ", height=" + height +
               '}';
    }

    public enum Anchor {
        TOP_LEFT,
        TOP_CENTER,
        TOP_RIGHT,
        CENTER_LEFT,
        CENTER,
        CENTER_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_CENTER,
        BOTTOM_RIGHT
    }
}