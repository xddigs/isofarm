package com.tilled.input;

import com.tilled.data.Block;
import com.tilled.data.Player;
import com.tilled.data.StepSoundGroup;
import com.tilled.service.SoundService;
import com.tilled.wrld.World;
import org.joml.Vector3f;

public class StepController {
    private float stepDistanceAccumulator = 0.0f;
    private static final float STEP_DISTANCE_THRESHOLD = 1.8f;

    public void update(Player player, World world, SoundService soundService, float delta) {
        if (!player.isOnGround()) {
            stepDistanceAccumulator = 0.0f;
            return;
        }

        Vector3f vel = player.getVelocity();
        float horizontalSpeed = (float) Math.sqrt(vel.x * vel.x + vel.z * vel.z);

        if (horizontalSpeed < 0.1f) {
            stepDistanceAccumulator = 0.0f;
            return;
        }

        stepDistanceAccumulator += horizontalSpeed * delta;
        if (stepDistanceAccumulator >= STEP_DISTANCE_THRESHOLD) {
            stepDistanceAccumulator -= STEP_DISTANCE_THRESHOLD;

            Vector3f pos = player.getPosition();
            int blockX = (int) Math.floor(pos.x);
            int blockY = (int) Math.floor(pos.y - 0.2f);
            int blockZ = (int) Math.floor(pos.z);

            Block block = world.getBlockAt(blockX, blockY, blockZ);
            if (block != null && block.getType() != null) {
                StepSoundGroup soundGroup = block.getType().getStepSoundGroup();
                soundService.playStepSound(soundGroup);
            }
        }
    }
}