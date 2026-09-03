package com.isofarm.entity.states;

import com.isofarm.data.PlayerState;
import com.isofarm.entity.Player;
import com.isofarm.input.Keyboard;
import com.isofarm.wrld.GameMaster;
import com.isofarm.wrld.World;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Provides swimming state behavior.
 */
public class SwimmingState implements PlayerState {

    private static final float SWIM_UP_SPEED = 4.0f;
    private static final float SWIM_DOWN_SPEED = -3.0f;
    private static final float WATER_DRAG = 0.90f;
    private static final float BUOYANCY = 1.2f;

    /**
     * Performs the enter operation.
     * @param player the player value
     */
    @Override
    public void enter(Player player) {
        player.setTargetEyeHeight(1.2f);
    }

    /**
     * Performs the input operation.
     * @param player the player value
     * @param gameMaster the game master value
     */
    @Override
    public void input(Player player, GameMaster gameMaster) {
        if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
            player.getVelocity().y = SWIM_UP_SPEED;
        } else if (Keyboard.isKeyDown(Keyboard.KEY_LEFT_CONTROL)) {
            player.getVelocity().y = SWIM_DOWN_SPEED;
        }
    }

    /**
     * Updates the current state.
     * @param player the player value
     * @param delta the delta value
     */
    @Override
    public void update(Player player, float delta) {
        float yaw = GameMaster.game.getActiveCamera().getYaw();
        player.wasd(World.wrld, delta, yaw, false);

        if (player.isOnGround()) {
            if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
                player.changeState(new GroundedState());
                return;
            }
        }

        if (!player.isInFluid(World.wrld)) {
            if (player.isOnGround()) {
                player.changeState(new GroundedState());
            } else {
                player.changeState(new FallingState());
            }
        }
    }

    /**
     * Performs the exit operation.
     * @param player the player value
     */
    @Override
    public void exit(Player player) {
        player.setTargetEyeHeight(1.6f);
    }
}