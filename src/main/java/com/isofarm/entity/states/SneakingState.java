package com.isofarm.entity.states;

import com.isofarm.data.PlayerState;
import com.isofarm.entity.Player;
import com.isofarm.input.ControlAction;
import com.isofarm.input.Controls;
import com.isofarm.wrld.GameMaster;

/**
 * Provides sneaking state behavior.
 */
public class SneakingState implements PlayerState {
    private final Player player = Player.plyr;
    private static final float SNEAK_EYE_HEIGHT = 1.2f;

    /**
     * Performs the enter operation.
     */
    @Override
    public void enter() {
        player.setTargetEyeHeight(SNEAK_EYE_HEIGHT);
        player.setSpeed(player.getSpeed() * 0.5f);
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

        if (Controls.isPressed(ControlAction.PRIMARY_ACTION) ||
                Controls.isPressed(ControlAction.SECONDARY_ACTION)) {
            player.interact();
            return;
        }

        if (!Controls.isDown(ControlAction.SNEAK)) {
            player.changeState(new GroundedState());
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

        float yaw = GameMaster.game.getActiveCamera().getYaw();
        player.wasd(delta, yaw, false);
    }

    /**
     * Performs the exit operation.
     */
    @Override
    public void exit() {
        player.setTargetEyeHeight(1.6f);
        player.setSpeed(player.getSpeed() * 2.0f);
    }
}
