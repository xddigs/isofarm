package com.isofarm.entity.states;

import com.isofarm.data.PlayerState;
import com.isofarm.entity.Player;
import com.isofarm.input.Keyboard;
import com.isofarm.input.Mouse;
import com.isofarm.wrld.GameMaster;

import static org.lwjgl.glfw.GLFW.*;

public class SneakingState implements PlayerState {
    private static final float SNEAK_EYE_HEIGHT = 1.2f;

    @Override
    public void enter(Player player) {
        player.setTargetEyeHeight(SNEAK_EYE_HEIGHT);
        player.setSpeed(player.getSpeed() * 0.5f);
    }

    @Override
    public void input(Player player, GameMaster gameMaster) {
        if (gameMaster.isInventoryOpen() || gameMaster.isChatOpen()) {
            return;
        }

        if (Mouse.isButtonPressed(GLFW_MOUSE_BUTTON_LEFT) ||
                Mouse.isButtonPressed(GLFW_MOUSE_BUTTON_RIGHT)) {
            player.interact();
            return;
        }

        if (!Keyboard.isKeyDown(GLFW_KEY_LEFT_CONTROL)) {
            player.changeState(new GroundedState());
        }
    }

    @Override
    public void update(Player player, float delta) {
        if (player.isInFluid(player.getGameMaster().getWorld())) {
            player.changeState(new SwimmingState());
            return;
        }

        float yaw = player.getGameMaster().getActiveCamera().getYaw();
        player.wasd(player.getGameMaster().getWorld(), delta, yaw, false);
    }

    @Override
    public void exit(Player player) {
        player.setTargetEyeHeight(1.6f);
        player.setSpeed(player.getSpeed() * 2.0f);
    }
}
