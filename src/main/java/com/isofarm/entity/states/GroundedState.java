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
        if (gameMaster.isInventoryOpen() || gameMaster.isChatOpen()) return;
    }

    @Override
    public void update(Player player, float delta) {
        if (player.isUnderFluid(player.getGameMaster().getWorld())) {
            player.changeState(new SwimmingState());
            return;
        }

        if (!player.isOnGround()) {
            player.changeState(new FallingState());
            return;
        }

        float yaw = player.getGameMaster().getActiveCamera().getYaw();
        player.wasd(player.getGameMaster().getWorld(), delta, yaw);
    }

    @Override
    public void exit(Player player) {}
}