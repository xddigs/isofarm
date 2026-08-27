package com.isofarm.entity.states;

import com.isofarm.data.PlayerState;
import com.isofarm.entity.Player;
import com.isofarm.input.Keyboard;
import com.isofarm.wrld.GameMaster;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;

public class GroundedState implements PlayerState {

    @Override
    public void enter(Player player) {
        player.setTargetEyeHeight(1.6f);
    }

    @Override
    public void input(Player player, GameMaster gameMaster) {
        if (Keyboard.isKeyPressed(GLFW_KEY_SPACE)) {
            player.jump();
            player.changeState(new JumpingState());
        } else if (Keyboard.isKeyDown(GLFW_KEY_LEFT_CONTROL)) {
            player.changeState(new CrouchingState());
        }
    }

    @Override
    public void update(Player player, float delta) {
        if (player.isUnderFluid(player.getGameMaster().getWorld())) {
            player.changeState(new SwimmingState());
            return;
        }

        if (!player.isOnGround()) {
            player.changeState(new FallingState());
        }
    }

    @Override public void exit(Player player) {}
}