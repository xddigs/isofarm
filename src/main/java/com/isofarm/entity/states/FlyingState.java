package com.isofarm.entity.states;

import com.isofarm.data.PlayerState;
import com.isofarm.entity.Player;
import com.isofarm.input.Keyboard;
import com.isofarm.wrld.GameMaster;

import static org.lwjgl.glfw.GLFW.*;

public class FlyingState implements PlayerState {
    private static final float FLY_SPEED = 8.0f;

    @Override
    public void enter(Player player) {
        player.getVelocity().set(0.0f, 0.0f, 0.0f);
    }

    @Override
    public void input(Player player, GameMaster gameMaster) {
        if (gameMaster.isInventoryOpen() || gameMaster.isChatOpen()) {
            return;
        }

        if (!player.getGamemode().isGodmode()) {
            player.changeState(new FallingState());
            return;
        }

        if (Keyboard.isKeyDown(GLFW_KEY_SPACE)) {
            player.getVelocity().y = FLY_SPEED;
        } else if (Keyboard.isKeyDown(GLFW_KEY_LEFT_CONTROL)) {
            player.getVelocity().y = -FLY_SPEED;
        } else {
            player.getVelocity().y = 0.0f;
        }
    }

    @Override
    public void update(Player player, float delta) {
        float yaw = player.getGameMaster()
                .getActiveCamera()
                .getYaw();

        player.fly(delta, yaw, true);
    }

    @Override
    public void exit(Player player) {}
}