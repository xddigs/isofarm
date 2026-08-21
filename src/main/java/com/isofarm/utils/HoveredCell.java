package com.isofarm.utils;

import com.isofarm.data.Hit;
import com.isofarm.input.Mouse;
import com.isofarm.wrld.GameMaster;

@Utils
public class HoveredCell {

    public static Hit get(GameMaster gameMaster) {
        return gameMaster.getOrthoCamera().highlight(gameMaster.getWorld(),
                Mouse.getX(), Mouse.getY(), gameMaster.getWindowWidth(),
                gameMaster.getWindowHeight());
    }
}
