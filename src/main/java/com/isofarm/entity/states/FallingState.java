package com.isofarm.entity.states;

import com.isofarm.data.PlayerState;
import com.isofarm.data.SoundGroup;
import com.isofarm.entity.Player;
import com.isofarm.input.Keyboard;
import com.isofarm.service.SoundService;
import com.isofarm.wrld.GameMaster;
import com.isofarm.wrld.World;

/**
 * Provides falling state behavior.
 */
public class FallingState implements PlayerState {
    private static final float FALL_DISTANCE = 4.0f;
    private static final float DOUBLE_JUMP_WINDOW = 0.75f;

    private float fallStartY;
    private float fallTime;
    private float jumpTime;

    /**
     * Performs the enter operation.
     * @param player the player value
     */
    @Override
    public void enter(Player player) {
        this.fallStartY = player.getPosition().y;
        this.fallTime = 0.0f;
        this.jumpTime = 0.0f;

        player.setFalling(true);
    }

    /**
     * Performs the input operation.
     * @param player the player value
     * @param gameMaster the game master value
     */
    @Override
    public void input(Player player, GameMaster gameMaster) {
        if (gameMaster.isInventoryOpen() || gameMaster.isChatOpen()) {
            return;
        }

        if (player.getGamemode().isGodmode() && jumpTime <= DOUBLE_JUMP_WINDOW
                && Keyboard.isKeyPressed(Keyboard.KEY_SPACE)) {
            player.changeState(new FlyingState());
        }
    }

    /**
     * Updates the current state.
     * @param player the player value
     * @param delta the delta value
     */
    @Override
    public void update(Player player, float delta) {
        fallTime += delta;
        jumpTime += delta;

        if (player.isInFluid(World.wrld)) {
            player.setFalling(false);
            player.changeState(new SwimmingState());
            return;
        }

        float yaw = GameMaster.game.getActiveCamera().getYaw();
        player.wasd(World.wrld, delta, yaw, false);

        if (player.isOnGround()) {
            float fallDistance = fallStartY - player.getPosition().y;
            if (fallDistance > FALL_DISTANCE) {
                float damage = (fallDistance - FALL_DISTANCE) * 2.0f;
                player.fallDamage(damage);
                SoundService.fx.playBreakSound(SoundGroup.ENTITY);
            }

            player.setFalling(false);
            player.changeState(new GroundedState());
        }
    }

    /**
     * Performs the exit operation.
     * @param player the player value
     */
    @Override
    public void exit(Player player) {
        player.setFalling(false);
    }
}
