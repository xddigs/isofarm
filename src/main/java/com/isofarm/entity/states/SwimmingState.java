package com.isofarm.entity.states;

import com.isofarm.data.PlayerState;
import com.isofarm.entity.Player;
import com.isofarm.input.Keyboard;
import com.isofarm.wrld.GameMaster;
import com.isofarm.wrld.World;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;

public class SwimmingState implements PlayerState {

    @Override
    public void enter(Player player) {
        player.setTargetEyeHeight(0.8f);
    }

    @Override
    public void input(Player player, GameMaster gameMaster) {
        if (Keyboard.isKeyPressed(GLFW_KEY_SPACE)) {
            player.getVelocity().y = 3.0f; 
        }
    }

    @Override
    public void update(Player player, float delta) {
        World world = player.getGameMaster().getWorld();
        player.getVelocity().y *= 0.75f;
        if (!player.isUnderFluid(world)) {
            if (player.isOnGround()) {
                player.changeState(new GroundedState());
            } else {
                player.changeState(new FallingState());
            }
        }
    }

    @Override
    public void exit(Player player) {
        player.setTargetEyeHeight(1.6f);
    }
}