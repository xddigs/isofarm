package com.isofarm.entity.states;

import com.isofarm.data.PlayerState;
import com.isofarm.data.SoundGroup;
import com.isofarm.entity.Player;
import com.isofarm.wrld.GameMaster;
import com.isofarm.wrld.World;

public class FallingState implements PlayerState {
    private static final float FALL_DISTANCE = 4.0f;
    private static final float FALL_ANIMATION_DELAY = 0.75f;

    private float fallStartY;
    private float fallTime;

    @Override
    public void enter(Player player) {
        this.fallStartY = player.getPosition().y;
        this.fallTime = 0.0f;
        player.setFalling(true);
    }

    @Override
    public void input(Player player, GameMaster gameMaster) {}

    @Override
    public void update(Player player, float delta) {
        World world = player.getGameMaster().getWorld();
        fallTime += delta;
        if (player.isUnderFluid(world)) {
            player.setFalling(false);
            player.changeState(new SwimmingState());
            return;
        }

        float yaw = player.getGameMaster().getActiveCamera().getYaw();
        player.wasd(world, delta, yaw);

        if (player.isOnGround()) {
            float fallDistance = fallStartY - player.getPosition().y;

            if (fallDistance > FALL_DISTANCE) {
                float damage = (fallDistance - FALL_DISTANCE) * 2.0f;
                player.fallDamage(damage);
                player.getSoundService().playBreakSound(SoundGroup.ENTITY, 1.0f, 1.0f);
            }

            player.setFalling(false);
            player.changeState(new GroundedState());
        }
    }

    @Override
    public void exit(Player player) {
        player.setFalling(false);
    }

    public boolean shouldAnimateFall() {
        return fallTime >= FALL_ANIMATION_DELAY;
    }
}