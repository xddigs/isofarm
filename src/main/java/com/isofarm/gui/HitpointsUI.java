package com.isofarm.gui;

import com.isofarm.data.UIElement;
import com.isofarm.entity.Player;
import com.isofarm.graphics.SpriteSheet;
import com.isofarm.utils.K;

public class HitpointsUI extends UIElement {
    private final Player player;
    private final HotbarUI hotbar;
    private final SpriteSheet hitpoints;

    public HitpointsUI(float x, float y, float width, float height,
                       Player player, HotbarUI hotbar) {
        super(x, y, width, height);
        this.player = player;
        this.hotbar = hotbar;
        this.hitpoints = new SpriteSheet(K.Paths.WIDGET_HITPOINTS);
    }

    @Override
    public void render() {
        GUI.drawSprite();
    }

    public Player getPlayer() {
        return player;
    }

    public HotbarUI getHotbar() {
        return hotbar;
    }
}
