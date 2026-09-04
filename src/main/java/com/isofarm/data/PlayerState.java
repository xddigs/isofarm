package com.isofarm.data;

import com.isofarm.wrld.GameMaster;

/**
 * Defines the player state contract.
 */
public interface PlayerState {
    /**
     * Performs the input operation.
     * @param gameMaster the game master value
     */
    void input(GameMaster gameMaster);
    /**
     * Updates the current state.
     * @param delta the delta value
     */
    void update(float delta);
    /**
     * Performs the enter operation.
     */
    void enter();
    /**
     * Performs the exit operation.
     */
    void exit();
}
