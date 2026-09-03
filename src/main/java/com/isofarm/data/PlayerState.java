package com.isofarm.data;

import com.isofarm.entity.Player;
import com.isofarm.wrld.GameMaster;

/**
 * Defines the player state contract.
 */
public interface PlayerState {
    /**
     * Performs the input operation.
     * @param player the player value
     * @param gameMaster the game master value
     */
    void input(Player player, GameMaster gameMaster);
    /**
     * Updates the current state.
     * @param player the player value
     * @param delta the delta value
     */
    void update(Player player, float delta);
    /**
     * Performs the enter operation.
     * @param player the player value
     */
    void enter(Player player);
    /**
     * Performs the exit operation.
     * @param player the player value
     */
    void exit(Player player);
}