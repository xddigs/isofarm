package com.isofarm.entity.states;

import com.isofarm.data.PlayerState;
import com.isofarm.entity.Player;
import com.isofarm.input.Keyboard;
import com.isofarm.wrld.GameMaster;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Provides grounded state behavior.
 */
public class GroundedState implements PlayerState {

    /**
     * Performs the enter operation.
     * @param player the player value
     */
    @Override
    public void enter(Player player) {
        player.setTargetEyeHeight(1.6f);
    }

    /**
     * Performs the input operation.
     * @param player the player value
     * @param gameMaster the game master value
     */
    @Override
    public void input(Player player, GameMaster gameMaster) {
        if (gameMaster.isInventoryOpen() || gameMaster.isChatOpen()) {
            return;
        }

        if (Keyboard.isKeyDown(GLFW_KEY_LEFT_CONTROL)) {
            player.changeState(new SneakingState());
            return;
        }
    }

    /**
     * Updates the current state.
     * @param player the player value
     * @param delta the delta value
     */
    @Override
    public void update(Player player, float delta) {
        if (player.isInFluid(GameMaster.game.getWorld())) {
            player.changeState(new SwimmingState());
            return;
        }

        if (!player.isOnGround() && !player.isFalling()) {
            player.changeState(new FallingState());
            return;
        }

        float yaw = GameMaster.game
                .getActiveCamera()
                .getYaw();

        player.wasd(GameMaster.game.getWorld(), delta, yaw, false);
    }

    /**
     * Performs the exit operation.
     * @param player the player value
     */
    @Override
    public void exit(Player player) {
    }
}