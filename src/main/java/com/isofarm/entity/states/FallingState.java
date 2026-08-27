package com.isofarm.entity.states;

import com.isofarm.data.PlayerState;
import com.isofarm.data.SoundGroup;
import com.isofarm.entity.Player;
import com.isofarm.wrld.GameMaster;
import com.isofarm.wrld.World;

public class FallingState implements PlayerState {
    private float fallStartY;

    @Override
    public void enter(Player player) {
        this.fallStartY = player.getPosition().y;
        player.setFalling(true);
    }

    @Override
    public void input(Player player, GameMaster gameMaster) {

    }

    @Override
    public void update(Player player, float delta) {
        World world = player.getGameMaster().getWorld();

        if (player.isUnderFluid(world)) {
            player.setFalling(false);
            player.changeState(new SwimmingState());
            return;
        }

        if (player.isOnGround()) {
            float fallDistance = fallStartY - player.getPosition().y;

            if (fallDistance > 3.0f) {
                float damage = (fallDistance - 3.0f) * 2.0f;
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
}