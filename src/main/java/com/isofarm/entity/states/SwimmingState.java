package com.isofarm.entity.states;

import com.isofarm.data.PlayerState;
import com.isofarm.entity.Player;
import com.isofarm.input.Keyboard;
import com.isofarm.wrld.GameMaster;
import com.isofarm.wrld.World;

import static org.lwjgl.glfw.GLFW.*;

public class SwimmingState implements PlayerState {

    private static final float SWIM_UP_SPEED = 4.0f;
    private static final float SWIM_DOWN_SPEED = -3.0f;
    private static final float WATER_DRAG = 0.90f;
    private static final float BUOYANCY = 1.2f;

    @Override
    public void enter(Player player) {
        player.setTargetEyeHeight(1.2f);
    }

    @Override
    public void input(Player player, GameMaster gameMaster) {
        if (Keyboard.isKeyDown(GLFW_KEY_SPACE)) {
            player.getVelocity().y = SWIM_UP_SPEED;
        } else if (Keyboard.isKeyDown(GLFW_KEY_LEFT_CONTROL)) {
            player.getVelocity().y = SWIM_DOWN_SPEED;
        }
    }

    @Override
    public void update(Player player, float delta) {
        World world = player.getGameMaster().getWorld();

        float yaw = player.getGameMaster().getActiveCamera().getYaw();
        player.wasd(world, delta, yaw, false);

        if (!Keyboard.isKeyDown(GLFW_KEY_SPACE) && !Keyboard.isKeyDown(GLFW_KEY_LEFT_CONTROL)) {
            player.getVelocity().y += BUOYANCY * delta;
        }

        player.getVelocity().y *= WATER_DRAG;

        if (player.isOnGround()) {
            if (Keyboard.isKeyDown(GLFW_KEY_SPACE)) {
                player.jump();
                player.changeState(new GroundedState());
                return;
            }
        }

        if (!player.isInFluid(world)) {
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