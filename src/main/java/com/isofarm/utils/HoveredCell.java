package com.isofarm.utils;

import com.isofarm.data.BlockPos;
import com.isofarm.input.Mouse;
import com.isofarm.wrld.GameMaster;

@Utils
public class HoveredCell {

    public static BlockPos get(GameMaster gameMaster, boolean isShiftHeld) {
        return gameMaster.getOrthoCamera().highlight(gameMaster.getWorld(),
                gameMaster.getPlayer().getPosition(), Mouse.getX(), Mouse.getY(),
                gameMaster.getWindowWidth(),
                gameMaster.getWindowHeight(),
                isShiftHeld);
    }

    public static BlockPos get(GameMaster gameMaster) {
        return get(gameMaster, false);
    }
}
