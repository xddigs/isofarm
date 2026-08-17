package com.tilled.input;

import com.tilled.data.Block;
import com.tilled.data.Player;
import com.tilled.data.SoundGroup;
import com.tilled.service.SoundService;
import com.tilled.wrld.World;
import org.joml.Vector3f;

public class StepController {
    private float stepDistanceAccumulator = 0.0f;
    private static final float STEP_DISTANCE_THRESHOLD = 1.6f;
    private final Vector3f lastPosition = new Vector3f();

    public void update(Player player, World world, SoundService soundService, float delta) {
        if (player == null) return;
        Vector3f currentPos = player.getPosition();
        float dx = currentPos.x - lastPosition.x;
        float dz = currentPos.z - lastPosition.z;
        float distanceMoved = (float) Math.sqrt(dx * dx + dz * dz);

        lastPosition.set(currentPos);
        if (distanceMoved < 0.001f || distanceMoved > 2.0f) {
            return;
        }

        stepDistanceAccumulator += distanceMoved;

        if (stepDistanceAccumulator >= STEP_DISTANCE_THRESHOLD) {
            stepDistanceAccumulator -= STEP_DISTANCE_THRESHOLD;

            int blockX = (int) Math.floor(currentPos.x);
            int blockY = (int) Math.floor(currentPos.y - 0.5f);
            int blockZ = (int) Math.floor(currentPos.z);

            Block block = world.getBlockAt(blockX, blockY, blockZ);
            if (block != null && block.getType() != null) {
                SoundGroup soundGroup = block.getType().getStepSoundGroup();
                soundService.playStepSound(soundGroup);
            }
        }
    }
}