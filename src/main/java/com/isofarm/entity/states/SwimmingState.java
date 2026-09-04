package com.isofarm.entity.states;

import com.isofarm.data.PlayerState;
import com.isofarm.entity.Player;
import com.isofarm.input.ControlAction;
import com.isofarm.input.Controls;
import com.isofarm.wrld.GameMaster;
import com.isofarm.wrld.World;

/**
 * Provides swimming state behavior.
 */
public class SwimmingState implements PlayerState {
    private final Player player = Player.plyr;

    private static final float SWIM_UP_SPEED = 4.0f;
    private static final float SWIM_DOWN_SPEED = -3.0f;
    private static final float WATER_DRAG = 0.90f;
    private static final float BUOYANCY = 1.2f;

    /**
     * Performs the enter operation.
     */
    @Override
    public void enter() {
        player.setTargetEyeHeight(1.2f);
    }

    /**
     * Performs the input operation.
     * @param gameMaster the game master value
     */
    @Override
    public void input(GameMaster gameMaster) {
        if (Controls.isDown(ControlAction.SWIM_UP)) {
            player.getVelocity().y = SWIM_UP_SPEED;
        } else if (Controls.isDown(ControlAction.SWIM_DOWN)) {
            player.getVelocity().y = SWIM_DOWN_SPEED;
        }
    }

    /**
     * Updates the current state.
     * @param delta the delta value
     */
    @Override
    public void update(float delta) {
        float yaw = GameMaster.game.getActiveCamera().getYaw();
        player.wasd(delta, yaw, false);

        if (player.isOnGround()) {
            if (Controls.isDown(ControlAction.SWIM_UP)) {
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
     */
    @Override
    public void exit() {
        player.setTargetEyeHeight(1.6f);
    }
}
