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
    private final Player player = Player.plyr;
    private static final float FALL_DISTANCE = 4.0f;
    private static final float DOUBLE_JUMP_WINDOW = 0.75f;

    private float fallStartY;
    private float fallTime;
    private float jumpTime;

    /**
     * Performs the enter operation.
     */
    @Override
    public void enter() {
        this.fallStartY = player.getPosition().y;
        this.fallTime = 0.0f;
        this.jumpTime = 0.0f;

        player.setFalling(true);
    }

    /**
     * Performs the input operation.
     * @param gameMaster the game master value
     */
    @Override
    public void input(GameMaster gameMaster) {
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
     * @param delta the delta value
     */
    @Override
    public void update(float delta) {
        fallTime += delta;
        jumpTime += delta;

        if (player.isInFluid(World.wrld)) {
            player.setFalling(false);
            player.changeState(new SwimmingState());
            return;
        }

        float yaw = GameMaster.game.getActiveCamera().getYaw();
        player.wasd(delta, yaw, false);

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
     */
    @Override
    public void exit() {
        player.setFalling(false);
    }
}
