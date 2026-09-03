package com.isofarm.entity.states;

import com.isofarm.data.PlayerState;
import com.isofarm.entity.Player;
import com.isofarm.input.Keyboard;
import com.isofarm.wrld.GameMaster;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Provides flying state behavior.
 */
public class FlyingState implements PlayerState {
    private static final float FLY_SPEED = 8.0f;

    /**
     * Performs the enter operation.
     * @param player the player value
     */
    @Override
    public void enter(Player player) {
        player.getVelocity().set(0.0f, 0.0f, 0.0f);
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

        if (!player.getGamemode().isGodmode()) {
            player.changeState(new FallingState());
            return;
        }

        if (Keyboard.isKeyDown(GLFW_KEY_SPACE)) {
            player.getVelocity().y = FLY_SPEED;
        } else if (Keyboard.isKeyDown(GLFW_KEY_LEFT_CONTROL)) {
            player.getVelocity().y = -FLY_SPEED;
        } else {
            player.getVelocity().y = 0.0f;
        }
    }

    /**
     * Updates the current state.
     * @param player the player value
     * @param delta the delta value
     */
    @Override
    public void update(Player player, float delta) {
        float yaw = GameMaster.game
                .getActiveCamera()
                .getYaw();

        player.fly(delta, yaw, true);
    }

    /**
     * Performs the exit operation.
     * @param player the player value
     */
    @Override
    public void exit(Player player) {}
}