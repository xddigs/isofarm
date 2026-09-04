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
    private final Player player = Player.plyr;
    private static final float FLY_SPEED = 8.0f;

    /**
     * Performs the enter operation.
     */
    @Override
    public void enter() {
        player.getVelocity().set(0.0f, 0.0f, 0.0f);
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

        if (!player.getGamemode().isGodmode()) {
            player.changeState(new FallingState());
            return;
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
            player.getVelocity().y = FLY_SPEED;
        } else if (Keyboard.isKeyDown(Keyboard.KEY_LEFT_CONTROL)) {
            player.getVelocity().y = -FLY_SPEED;
        } else {
            player.getVelocity().y = 0.0f;
        }
    }

    /**
     * Updates the current state.
     * @param delta the delta value
     */
    @Override
    public void update(float delta) {
        float yaw = GameMaster.game
                .getActiveCamera()
                .getYaw();

        player.fly(delta, yaw, true);
    }

    /**
     * Performs the exit operation.
     */
    @Override
    public void exit() {}
}
