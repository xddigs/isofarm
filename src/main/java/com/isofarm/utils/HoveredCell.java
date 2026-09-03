package com.isofarm.utils;

import com.isofarm.data.BlockPos;
import com.isofarm.input.Mouse;
import com.isofarm.wrld.GameMaster;

/**
 * Provides hovered cell behavior.
 */
@Utils
public class HoveredCell {

    /**
     * Returns get.
     * @param gameMaster the game master value
     * @param isShiftHeld the is shift held value
     * @return the get result
     */
    public static BlockPos get(GameMaster gameMaster, boolean isShiftHeld) {
        return gameMaster.getOrthoCamera().highlight(gameMaster.getWorld(),
                gameMaster.getPlayer().getPosition(), Mouse.getX(), Mouse.getY(),
                gameMaster.getWindowWidth(),
                gameMaster.getWindowHeight(),
                isShiftHeld);
    }

    /**
     * Returns get.
     * @param gameMaster the game master value
     * @return the get result
     */
    public static BlockPos get(GameMaster gameMaster) {
        return get(gameMaster, false);
    }
}
