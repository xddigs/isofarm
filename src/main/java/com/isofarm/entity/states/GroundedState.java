package com.isofarm.entity.states;

import com.isofarm.data.PlayerState;
import com.isofarm.entity.Player;
import com.isofarm.input.ControlAction;
import com.isofarm.input.Controls;
import com.isofarm.wrld.GameMaster;

/**
 * Provides grounded state behavior.
 */
public class GroundedState implements PlayerState {
    private final Player player = Player.plyr;

    /**
     * Performs the enter operation.
     */
    @Override
    public void enter() {
        player.setTargetEyeHeight(1.6f);
    }

    /**
     * Performs the input operation.
     * @param gameMaster the game master value
     */
    @Override
    public void input(GameMaster gameMaster) {
        if (gameMaster.isInventoryOpen() || gameMaster.isChatOpen()) {
            return;
        }

        if (Controls.isDown(ControlAction.SNEAK)) {
            player.changeState(new SneakingState());
            return;
        }
    }

    /**
     * Updates the current state.
     * @param delta the delta value
     */
    @Override
    public void update(float delta) {
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

        player.wasd(delta, yaw, false);
    }

    /**
     * Performs the exit operation.
     */
    @Override
    public void exit() {
    }
}
