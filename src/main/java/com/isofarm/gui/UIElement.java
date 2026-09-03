package com.isofarm.gui;

import com.isofarm.data.DataClass;
import com.isofarm.data.GodObject;
import com.isofarm.graphics.SpriteSheet;
import com.isofarm.graphics.Texture;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

/**
 * Provides uielement behavior.
 */
@SuppressWarnings("unused")
@GodObject
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

    /**
     * Creates a new {@code UIElement} instance.
     * @param x the x value
     * @param y the y value
     * @param width the width value
     * @param height the height value
     */
    protected UIElement(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Renders render.
     */
    public abstract void render();

    /**
     * Updates the current state.
     * @param delta the delta value
     */
    public void update(float delta) {
        if (!visible) {
            return;
        }

        updateSpriteAnimation(delta);

        for (UIElement child : getSortedChildren()) {
            child.update(delta);
        }
    }

    /**
     * Updates the sprite animation.
     * @param delta the delta value
     */
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

    /**
     * Renders the children.
     */
    public void renderChildren() {
        for (UIElement child : getSortedChildren()) {
            if (child.isVisible()) {
                child.render();
            }
        }
    }

    /**
     * Adds the child.
     * @param child the child value
     */
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

    /**
     * Removes the child.
     * @param child the child value
     */
    public void removeChild(UIElement child) {
        if (child == null) {
            return;
        }

        if (children.remove(child)) {
            child.parent = null;
        }
    }

    /**
     * Removes the all children.
     */
    public void removeAllChildren() {
        for (UIElement child : children) {
            child.parent = null;
        }

        children.clear();
    }

    /**
     * Returns the children.
     * @return the children
     */
    public List<UIElement> getChildren() {
        return List.copyOf(children);
    }

    /**
     * Performs the sort children operation.
     */
    private void sortChildren() {
        children.sort(Comparator.comparingInt(UIElement::getZIndex));
    }

    /**
     * Returns the parent.
     * @return the parent
     */
    public UIElement getParent() {
        return parent;
    }

    /**
     * Sets the parent.
     * @param parent the parent value
     */
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

    /**
     * Returns the x.
     * @return the x
     */
    public float getX() {
        return x;
    }

    /**
     * Sets the x.
     * @param x the x value
     */
    public void setX(float x) {
        this.x = x;
    }

    /**
     * Returns the y.
     * @return the y
     */
    public float getY() {
        return y;
    }

    /**
     * Sets the y.
     * @param y the y value
     */
    public void setY(float y) {
        this.y = y;
    }

    /**
     * Sets the position.
     * @param x the x value
     * @param y the y value
     * @return the set position result
     */
    public UIElement setPosition(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    /**
     * Returns the width.
     * @return the width
     */
    public float getWidth() {
        return width;
    }

    /**
     * Sets the width.
     * @param width the width value
     */
    public void setWidth(float width) {
        this.width = Math.max(0.0f, width);
    }

    /**
     * Returns the height.
     * @return the height
     */
    public float getHeight() {
        return height;
    }

    /**
     * Sets the height.
     * @param height the height value
     */
    public void setHeight(float height) {
        this.height = Math.max(0.0f, height);
    }

    /**
     * Sets the size.
     * @param width the width value
     * @param height the height value
     * @return the set size result
     */
    public UIElement setSize(float width, float height) {
        this.width = Math.max(0.0f, width);
        this.height = Math.max(0.0f, height);
        return this;
    }

    /**
     * Returns the scale x.
     * @return the scale x
     */
    public float getScaleX() {
        return scaleX;
    }

    /**
     * Sets the scale x.
     * @param scaleX the scale x value
     */
    public void setScaleX(float scaleX) {
        this.scaleX = scaleX;
    }

    /**
     * Returns the scale y.
     * @return the scale y
     */
    public float getScaleY() {
        return scaleY;
    }

    /**
     * Sets the scale y.
     * @param scaleY the scale y value
     */
    public void setScaleY(float scaleY) {
        this.scaleY = scaleY;
    }

    /**
     * Sets the scale.
     * @param scale the scale value
     * @return the set scale result
     */
    public UIElement setScale(float scale) {
        this.scaleX = scale;
        this.scaleY = scale;
        return this;
    }

    /**
     * Sets the scale.
     * @param scaleX the scale x value
     * @param scaleY the scale y value
     * @return the set scale result
     */
    public UIElement setScale(float scaleX, float scaleY) {
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        return this;
    }

    /**
     * Returns the rotation.
     * @return the rotation
     */
    public float getRotation() {
        return rotation;
    }

    /**
     * Sets the rotation.
     * @param rotation the rotation value
     * @return the set rotation result
     */
    public UIElement setRotation(float rotation) {
        this.rotation = rotation;
        return this;
    }

    /**
     * Returns the pivot x.
     * @return the pivot x
     */
    public float getPivotX() {
        return pivotX;
    }

    /**
     * Sets the pivot x.
     * @param pivotX the pivot x value
     */
    public void setPivotX(float pivotX) {
        this.pivotX = pivotX;
    }

    /**
     * Returns the pivot y.
     * @return the pivot y
     */
    public float getPivotY() {
        return pivotY;
    }

    /**
     * Sets the pivot y.
     * @param pivotY the pivot y value
     */
    public void setPivotY(float pivotY) {
        this.pivotY = pivotY;
    }

    /**
     * Sets the pivot.
     * @param pivotX the pivot x value
     * @param pivotY the pivot y value
     * @return the set pivot result
     */
    public UIElement setPivot(float pivotX, float pivotY) {
        this.pivotX = pivotX;
        this.pivotY = pivotY;
        return this;
    }

    /**
     * Returns the opacity.
     * @return the opacity
     */
    public float getOpacity() {
        return opacity;
    }

    /**
     * Sets the opacity.
     * @param opacity the opacity value
     */
    public void setOpacity(float opacity) {
        this.opacity = Math.clamp(opacity, 0.0f, 1.0f);
    }

    /**
     * Sets the opacity value.
     * @param opacity the opacity value
     * @return the set opacity value result
     */
    public UIElement setOpacityValue(float opacity) {
        setOpacity(opacity);
        return this;
    }

    /**
     * Returns the zindex.
     * @return the zindex
     */
    public int getZIndex() {
        return zIndex;
    }

    /**
     * Sets the zindex.
     * @param zIndex the z index value
     */
    public void setZIndex(int zIndex) {
        this.zIndex = zIndex;

        if (parent != null) {
            parent.sortChildren();
        }
    }

    /**
     * Sets the layer.
     * @param zIndex the z index value
     * @return the set layer result
     */
    public UIElement setLayer(int zIndex) {
        setZIndex(zIndex);
        return this;
    }

    /**
     * Checks whether the visible condition is met.
     * @return {@code true} if visible; otherwise {@code false}
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Sets the visible.
     * @param visible the visible value
     */
    public void setVisible(boolean visible) {
        this.visible = visible;

        if (!visible) {
            setHovered(false);
            setPressed(false);
        }
    }

    /**
     * Performs the show operation.
     * @return the show result
     */
    public UIElement show() {
        setVisible(true);
        return this;
    }

    /**
     * Performs the hide operation.
     * @return the hide result
     */
    public UIElement hide() {
        setVisible(false);
        return this;
    }

    /**
     * Checks whether the enabled condition is met.
     * @return {@code true} if enabled; otherwise {@code false}
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets the enabled.
     * @param enabled the enabled value
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;

        if (!enabled) {
            setHovered(false);
            setPressed(false);
            setFocused(false);
        }
    }

    /**
     * Performs the enable operation.
     * @return the enable result
     */
    public UIElement enable() {
        setEnabled(true);
        return this;
    }

    /**
     * Performs the disable operation.
     * @return the disable result
     */
    public UIElement disable() {
        setEnabled(false);
        return this;
    }

    /**
     * Returns the tooltip text.
     * @return the tooltip text
     */
    public String getTooltipText() {
        return tooltipText;
    }

    /**
     * Sets the tooltip text.
     * @param tooltipText the tooltip text value
     */
    public void setTooltipText(String tooltipText) {
        this.tooltipText = tooltipText;
    }

    /**
     * Performs the tooltip operation.
     * @param tooltipText the tooltip text value
     * @return the tooltip result
     */
    public UIElement tooltip(String tooltipText) {
        this.tooltipText = tooltipText;
        return this;
    }

    /**
     * Checks whether the interactable condition is met.
     * @return {@code true} if interactable; otherwise {@code false}
     */
    public boolean isInteractable() {
        return interactable;
    }

    /**
     * Sets the interactable.
     * @param interactable the interactable value
     */
    public void setInteractable(boolean interactable) {
        this.interactable = interactable;
    }

    /**
     * Checks whether the focusable condition is met.
     * @return {@code true} if focusable; otherwise {@code false}
     */
    public boolean isFocusable() {
        return focusable;
    }

    /**
     * Sets the focusable.
     * @param focusable the focusable value
     */
    public void setFocusable(boolean focusable) {
        this.focusable = focusable;
    }

    /**
     * Checks whether the hovered condition is met.
     * @return {@code true} if hovered; otherwise {@code false}
     */
    public boolean isHovered() {
        return hovered;
    }

    /**
     * Sets the hovered.
     * @param hovered the hovered value
     */
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

    /**
     * Checks whether the pressed condition is met.
     * @return {@code true} if pressed; otherwise {@code false}
     */
    public boolean isPressed() {
        return pressed;
    }

    /**
     * Sets the pressed.
     * @param pressed the pressed value
     */
    public void setPressed(boolean pressed) {
        this.pressed = pressed;
    }

    /**
     * Checks whether the focused condition is met.
     * @return {@code true} if focused; otherwise {@code false}
     */
    public boolean isFocused() {
        return focused;
    }

    /**
     * Sets the focused.
     * @param focused the focused value
     */
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

    /**
     * Checks whether the mouse inside condition is met.
     * @return {@code true} if mouse inside; otherwise {@code false}
     */
    public boolean isMouseInside() {
        return mouseInside;
    }

    /**
     * Sets the mouse inside.
     * @param mouseInside the mouse inside value
     */
    public void setMouseInside(boolean mouseInside) {
        this.mouseInside = mouseInside;
    }

    /**
     * Checks whether the clip children condition is met.
     * @return {@code true} if clip children; otherwise {@code false}
     */
    public boolean isClipChildren() {
        return clipChildren;
    }

    /**
     * Sets the clip children.
     * @param clipChildren the clip children value
     */
    public void setClipChildren(boolean clipChildren) {
        this.clipChildren = clipChildren;
    }

    /**
     * Returns the anchor.
     * @return the anchor
     */
    public Anchor getAnchor() {
        return anchor;
    }

    /**
     * Sets the anchor.
     * @param anchor the anchor value
     */
    public void setAnchor(Anchor anchor) {
        this.anchor = anchor == null ? Anchor.TOP_LEFT : anchor;
    }

    /**
     * Performs the anchor operation.
     * @param anchor the anchor value
     * @return the anchor result
     */
    public UIElement anchor(Anchor anchor) {
        setAnchor(anchor);
        return this;
    }

    /**
     * Returns the tint.
     * @return the tint
     */
    public Vector4f getTint() {
        return new Vector4f(tint);
    }

    /**
     * Sets the tint.
     * @param tint the tint value
     */
    public void setTint(Vector4f tint) {
        if (tint == null) {
            this.tint.set(1.0f, 1.0f, 1.0f, 1.0f);
            return;
        }

        this.tint.set(tint);
    }

    /**
     * Sets the tint.
     * @param r the r value
     * @param g the g value
     * @param b the b value
     * @param a the a value
     */
    public void setTint(float r, float g, float b, float a) {
        tint.set(r, g, b, a);
    }

    /**
     * Performs the tint operation.
     * @param r the r value
     * @param g the g value
     * @param b the b value
     * @param a the a value
     * @return the tint result
     */
    public UIElement tint(float r, float g, float b, float a) {
        setTint(r, g, b, a);
        return this;
    }

    /**
     * Performs the tint operation.
     * @param r the r value
     * @param g the g value
     * @param b the b value
     * @return the tint result
     */
    public UIElement tint(float r, float g, float b) {
        setTint(r, g, b, 1.0f);
        return this;
    }

    /**
     * Returns the sprite.
     * @return the sprite
     */
    public Texture getSprite() {
        return sprite;
    }

    /**
     * Sets the sprite.
     * @param sprite the sprite value
     */
    public void setSprite(Texture sprite) {
        this.sprite = sprite;
        this.spriteSheet = null;
        this.spriteCol = 0;
        this.spriteFrameCount = 1;
        this.spriteAnimated = false;
    }

    /**
     * Performs the sprite operation.
     * @param sprite the sprite value
     * @return the sprite result
     */
    public UIElement sprite(Texture sprite) {
        setSprite(sprite);
        return this;
    }

    /**
     * Returns the sprite sheet.
     * @return the sprite sheet
     */
    public SpriteSheet getSpriteSheet() {
        return spriteSheet;
    }

    /**
     * Sets the sprite sheet.
     * @param spriteSheet the sprite sheet value
     */
    public void setSpriteSheet(SpriteSheet spriteSheet) {
        this.spriteSheet = spriteSheet;
        this.sprite = null;
        this.spriteCol = 0;
        this.spriteFrameCount = spriteSheet != null ? spriteSheet.getTotalFrames() : 1;
        this.spriteAnimationTimer = 0.0f;
    }

    /**
     * Performs the sprite sheet operation.
     * @param spriteSheet the sprite sheet value
     * @return the sprite sheet result
     */
    public UIElement spriteSheet(SpriteSheet spriteSheet) {
        setSpriteSheet(spriteSheet);
        return this;
    }

    /**
     * Checks whether the sprite condition is met.
     * @return {@code true} if sprite; otherwise {@code false}
     */
    public boolean hasSprite() {
        return sprite != null || spriteSheet != null;
    }

    /**
     * Checks whether the static sprite condition is met.
     * @return {@code true} if static sprite; otherwise {@code false}
     */
    public boolean hasStaticSprite() {
        return sprite != null;
    }

    /**
     * Checks whether the sprite sheet condition is met.
     * @return {@code true} if sprite sheet; otherwise {@code false}
     */
    public boolean hasSpriteSheet() {
        return spriteSheet != null;
    }

    /**
     * Returns the sprite col.
     * @return the sprite col
     */
    public int getSpriteCol() {
        return spriteCol;
    }

    /**
     * Sets the sprite column.
     * @param spriteFrame the sprite frame value
     */
    public void setSpriteColumn(int spriteFrame) {
        if (spriteSheet == null) {
            this.spriteCol = 0;
            return;
        }

        this.spriteCol = Math.clamp(spriteFrame, 0, spriteFrameCount - 1);
    }

    /**
     * Returns the sprite row.
     * @return the sprite row
     */
    public int getSpriteRow() {
        return spriteRow;
    }

    /**
     * Sets the sprite row.
     * @param spriteRow the sprite row value
     */
    public void setSpriteRow(int spriteRow) {
        if (spriteSheet == null) {
            this.spriteRow = 0;
            return;
        }

        this.spriteRow = Math.clamp(spriteRow, 0, spriteFrameCount - 1);
    }

    /**
     * Performs the col operation.
     * @param frame the frame value
     * @return the col result
     */
    public UIElement col(int frame) {
        setSpriteColumn(frame);
        return this;
    }

    /**
     * Performs the row operation.
     * @param row the row value
     * @return the row result
     */
    public UIElement row(int row) {
        setSpriteRow(row);
        return this;
    }

    /**
     * Returns the sprite frame count.
     * @return the sprite frame count
     */
    public int getSpriteFrameCount() {
        return spriteFrameCount;
    }

    /**
     * Sets the sprite frame count.
     * @param spriteFrameCount the sprite frame count value
     */
    public void setSpriteFrameCount(int spriteFrameCount) {
        this.spriteFrameCount = Math.max(1, spriteFrameCount);

        if (spriteCol >= this.spriteFrameCount) {
            spriteCol = this.spriteFrameCount - 1;
        }
    }

    /**
     * Returns the sprite frame duration.
     * @return the sprite frame duration
     */
    public float getSpriteFrameDuration() {
        return spriteFrameDuration;
    }

    /**
     * Sets the sprite frame duration.
     * @param spriteFrameDuration the sprite frame duration value
     */
    public void setSpriteFrameDuration(float spriteFrameDuration) {
        this.spriteFrameDuration = Math.max(0.001f, spriteFrameDuration);
    }

    /**
     * Performs the frame duration operation.
     * @param duration the duration value
     * @return the frame duration result
     */
    public UIElement frameDuration(float duration) {
        setSpriteFrameDuration(duration);
        return this;
    }

    /**
     * Checks whether the sprite animated condition is met.
     * @return {@code true} if sprite animated; otherwise {@code false}
     */
    public boolean isSpriteAnimated() {
        return spriteAnimated;
    }

    /**
     * Sets the sprite animated.
     * @param spriteAnimated the sprite animated value
     */
    public void setSpriteAnimated(boolean spriteAnimated) {
        this.spriteAnimated = spriteAnimated && spriteSheet != null && spriteFrameCount > 1;
    }

    /**
     * Performs the animate sprite operation.
     * @param animate the animate value
     * @return the animate sprite result
     */
    public UIElement animateSprite(boolean animate) {
        setSpriteAnimated(animate);
        return this;
    }

    /**
     * Checks whether the sprite looping condition is met.
     * @return {@code true} if sprite looping; otherwise {@code false}
     */
    public boolean isSpriteLooping() {
        return spriteLoop;
    }

    /**
     * Sets the sprite looping.
     * @param spriteLoop the sprite loop value
     */
    public void setSpriteLooping(boolean spriteLoop) {
        this.spriteLoop = spriteLoop;
    }

    /**
     * Performs the loop sprite operation.
     * @param loop the loop value
     * @return the loop sprite result
     */
    public UIElement loopSprite(boolean loop) {
        setSpriteLooping(loop);
        return this;
    }

    /**
     * Returns the sprite animation timer.
     * @return the sprite animation timer
     */
    public float getSpriteAnimationTimer() {
        return spriteAnimationTimer;
    }

    /**
     * Performs the reset sprite animation operation.
     */
    public void resetSpriteAnimation() {
        spriteCol = 0;
        spriteAnimationTimer = 0.0f;
    }

    /**
     * Checks whether the sprite flip x condition is met.
     * @return {@code true} if sprite flip x; otherwise {@code false}
     */
    public boolean isSpriteFlipX() {
        return spriteFlipX;
    }

    /**
     * Sets the sprite flip x.
     * @param spriteFlipX the sprite flip x value
     */
    public void setSpriteFlipX(boolean spriteFlipX) {
        this.spriteFlipX = spriteFlipX;
    }

    /**
     * Performs the flip x operation.
     * @param flip the flip value
     * @return the flip x result
     */
    public UIElement flipX(boolean flip) {
        setSpriteFlipX(flip);
        return this;
    }

    /**
     * Checks whether the sprite flip y condition is met.
     * @return {@code true} if sprite flip y; otherwise {@code false}
     */
    public boolean isSpriteFlipY() {
        return spriteFlipY;
    }

    /**
     * Sets the sprite flip y.
     * @param spriteFlipY the sprite flip y value
     */
    public void setSpriteFlipY(boolean spriteFlipY) {
        this.spriteFlipY = spriteFlipY;
    }

    /**
     * Performs the flip y operation.
     * @param flip the flip value
     * @return the flip y result
     */
    public UIElement flipY(boolean flip) {
        setSpriteFlipY(flip);
        return this;
    }

    /**
     * Returns the sprite scale x.
     * @return the sprite scale x
     */
    public float getSpriteScaleX() {
        return spriteScaleX;
    }

    /**
     * Sets the sprite scale x.
     * @param spriteScaleX the sprite scale x value
     */
    public void setSpriteScaleX(float spriteScaleX) {
        this.spriteScaleX = spriteScaleX;
    }

    /**
     * Returns the sprite scale y.
     * @return the sprite scale y
     */
    public float getSpriteScaleY() {
        return spriteScaleY;
    }

    /**
     * Sets the sprite scale y.
     * @param spriteScaleY the sprite scale y value
     */
    public void setSpriteScaleY(float spriteScaleY) {
        this.spriteScaleY = spriteScaleY;
    }

    /**
     * Sets the sprite scale.
     * @param scale the scale value
     * @return the set sprite scale result
     */
    public UIElement setSpriteScale(float scale) {
        this.spriteScaleX = scale;
        this.spriteScaleY = scale;
        return this;
    }

    /**
     * Returns the sprite offset.
     * @return the sprite offset
     */
    public Vector2f getSpriteOffset() {
        return new Vector2f(spriteOffset);
    }

    /**
     * Sets the sprite offset.
     * @param x the x value
     * @param y the y value
     */
    public void setSpriteOffset(float x, float y) {
        spriteOffset.set(x, y);
    }

    /**
     * Performs the sprite offset operation.
     * @param x the x value
     * @param y the y value
     * @return the sprite offset result
     */
    public UIElement spriteOffset(float x, float y) {
        setSpriteOffset(x, y);
        return this;
    }

    /**
     * Returns the absolute x.
     * @return the absolute x
     */
    public float getAbsoluteX() {
        if (parent == null) {
            return x;
        }

        return parent.getContentX() + x;
    }

    /**
     * Returns the absolute y.
     * @return the absolute y
     */
    public float getAbsoluteY() {
        if (parent == null) {
            return y;
        }

        return parent.getContentY() + y;
    }

    /**
     * Returns the content x.
     * @return the content x
     */
    public float getContentX() {
        return getAbsoluteX();
    }

    /**
     * Returns the content y.
     * @return the content y
     */
    public float getContentY() {
        return getAbsoluteY();
    }

    /**
     * Returns the absolute width.
     * @return the absolute width
     */
    public float getAbsoluteWidth() {
        return width * scaleX;
    }

    /**
     * Returns the absolute height.
     * @return the absolute height
     */
    public float getAbsoluteHeight() {
        return height * scaleY;
    }

    /**
     * Returns the absolute position.
     * @return the absolute position
     */
    public Vector2f getAbsolutePosition() {
        return new Vector2f(getAbsoluteX(), getAbsoluteY());
    }

    /**
     * Returns the absolute size.
     * @return the absolute size
     */
    public Vector2f getAbsoluteSize() {
        return new Vector2f(getAbsoluteWidth(), getAbsoluteHeight());
    }

    /**
     * Performs the contains operation.
     * @param mouseX the mouse x value
     * @param mouseY the mouse y value
     * @return the contains result
     */
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

    /**
     * Finds and returns the element at.
     * @param mouseX the mouse x value
     * @param mouseY the mouse y value
     * @return the located element at
     */
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

    /**
     * Performs the mouse moved operation.
     * @param mouseX the mouse x value
     * @param mouseY the mouse y value
     */
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

    /**
     * Performs the mouse pressed operation.
     * @param mouseX the mouse x value
     * @param mouseY the mouse y value
     * @param button the button value
     * @return the mouse pressed result
     */
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

    /**
     * Performs the mouse released operation.
     * @param mouseX the mouse x value
     * @param mouseY the mouse y value
     * @param button the button value
     * @return the mouse released result
     */
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

    /**
     * Performs the mouse scrolled operation.
     * @param mouseX the mouse x value
     * @param mouseY the mouse y value
     * @param scrollX the scroll x value
     * @param scrollY the scroll y value
     * @return the mouse scrolled result
     */
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

    /**
     * Performs the key pressed operation.
     * @param key the key value
     * @param scancode the scancode value
     * @param modifiers the modifiers value
     * @return the key pressed result
     */
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

    /**
     * Performs the key released operation.
     * @param key the key value
     * @param scancode the scancode value
     * @param modifiers the modifiers value
     * @return the key released result
     */
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

    /**
     * Performs the char typed operation.
     * @param codepoint the codepoint value
     * @return the char typed result
     */
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

    /**
     * Performs the on click operation.
     * @param listener the listener value
     */
    public void onClick(Consumer<UIElement> listener) {
        this.clickListener = listener;
    }

    /**
     * Performs the on mouse enter operation.
     * @param listener the listener value
     */
    public void onMouseEnter(Consumer<UIElement> listener) {
        this.mouseEnterListener = listener;
    }

    /**
     * Performs the on mouse exit operation.
     * @param listener the listener value
     */
    public void onMouseExit(Consumer<UIElement> listener) {
        this.mouseExitListener = listener;
    }

    /**
     * Performs the on mouse press operation.
     * @param listener the listener value
     */
    public void onMousePress(Consumer<UIElement> listener) {
        this.mousePressListener = listener;
    }

    /**
     * Performs the on mouse release operation.
     * @param listener the listener value
     */
    public void onMouseRelease(Consumer<UIElement> listener) {
        this.mouseReleaseListener = listener;
    }

    /**
     * Performs the on focus operation.
     * @param listener the listener value
     */
    public void onFocus(Consumer<UIElement> listener) {
        this.focusListener = listener;
    }

    /**
     * Performs the on blur operation.
     * @param listener the listener value
     */
    public void onBlur(Consumer<UIElement> listener) {
        this.blurListener = listener;
    }

    /**
     * Returns the sorted children.
     * @return the sorted children
     */
    public List<UIElement> getSortedChildren() {
        List<UIElement> sorted = new ArrayList<>(children);
        sorted.sort(Comparator.comparingInt(UIElement::getZIndex));
        return sorted;
    }

    /**
     * Returns the world opacity.
     * @return the world opacity
     */
    public float getWorldOpacity() {
        if (parent == null) {
            return opacity;
        }

        return parent.getWorldOpacity() * opacity;
    }

    /**
     * Checks whether the actually visible condition is met.
     * @return {@code true} if actually visible; otherwise {@code false}
     */
    public boolean isActuallyVisible() {
        if (!visible) {
            return false;
        }

        return parent == null || parent.isActuallyVisible();
    }

    /**
     * Checks whether the actually enabled condition is met.
     * @return {@code true} if actually enabled; otherwise {@code false}
     */
    public boolean isActuallyEnabled() {
        if (!enabled) {
            return false;
        }

        return parent == null || parent.isActuallyEnabled();
    }

    /**
     * Performs the dispose operation.
     */
    public void dispose() {
        for (UIElement child : children) {
            child.dispose();
        }

        children.clear();
        parent = null;
    }

    /**
     * Performs the to string operation.
     * @return the to string result
     */
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

    /**
     * Enumerates the supported anchor values.
     */
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