package com.isofarm.entity.states;

import com.isofarm.data.PlayerState;
import com.isofarm.entity.Player;
import com.isofarm.wrld.GameMaster;
import com.isofarm.wrld.World;

public class JumpingState implements PlayerState {

    @Override
    public void enter(Player player) {}

    @Override
    public void input(Player player, GameMaster gameMaster) {}

    @Override
    public void update(Player player, float delta) {
        World world = player.getGameMaster().getWorld();

        if (player.isUnderFluid(world)) {
            player.changeState(new SwimmingState());
            return;
        }

        if (player.getVelocity().y <= 0.0f) {
            player.changeState(new FallingState());
        }
    }

    @Override
    public void exit(Player player) {}
}