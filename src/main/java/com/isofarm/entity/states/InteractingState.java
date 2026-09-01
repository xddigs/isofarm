package com.isofarm.entity.states;

import com.isofarm.data.PlayerState;
import com.isofarm.entity.Player;
import com.isofarm.input.Mouse;
import com.isofarm.wrld.GameMaster;
import com.isofarm.wrld.World;

import static org.lwjgl.glfw.GLFW.*;

public class InteractingState implements PlayerState {
    private static final float INTERACTION_DURATION = 0.35f;
    private float timer;

    @Override
    public void enter(Player player) {
        this.timer = INTERACTION_DURATION;
        player.setAnimTimer(0.0f);
        player.interact();
    }

    @Override
    public void input(Player player, GameMaster gameMaster) {
        if (gameMaster.isInventoryOpen() || gameMaster.isChatOpen()) return;
        if (Mouse.isButtonDown(GLFW_MOUSE_BUTTON_LEFT) || Mouse.isButtonDown(GLFW_MOUSE_BUTTON_RIGHT)) {
            this.timer = INTERACTION_DURATION;
        }
    }

    @Override
    public void update(Player player, float delta) {
        World world = player.getGameMaster().getWorld();

        if (player.isUnderFluid(world)) {
            player.changeState(new SwimmingState());
            return;
        }

        if (!player.isOnGround()) {
            player.changeState(new FallingState());
            return;
        }

        float yaw = player.getGameMaster().getActiveCamera().getYaw();
        player.wasd(world, delta, yaw);

        timer -= delta;
        if (timer <= 0.0f) {
            player.changeState(new GroundedState());
        }
    }

    @Override
    public void exit(Player player) {}
}