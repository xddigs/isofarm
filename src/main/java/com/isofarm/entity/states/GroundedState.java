package com.isofarm.entity.states;

import com.isofarm.data.PlayerState;
import com.isofarm.entity.Player;
import com.isofarm.input.Keyboard;
import com.isofarm.wrld.GameMaster;

import static org.lwjgl.glfw.GLFW.*;

public class GroundedState implements PlayerState {

    @Override
    public void enter(Player player) {
        player.setTargetEyeHeight(1.6f);
    }

    @Override
    public void input(Player player, GameMaster gameMaster) {
        if (gameMaster.isInventoryOpen() || gameMaster.isChatOpen()) {
            return;
        }

        if (Keyboard.isKeyDown(GLFW_KEY_LEFT_CONTROL)) {
            player.changeState(new SneakingState());
            return;
        }

        if (Keyboard.isKeyPressed(GLFW_KEY_SPACE)) {
            player.jump();
        }
    }

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

    @Override
    public void exit(Player player) {
    }
}