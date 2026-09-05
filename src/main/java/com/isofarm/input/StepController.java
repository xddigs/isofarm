package com.isofarm.input;

import com.isofarm.data.Singleton;
import com.isofarm.item.Block;
import com.isofarm.entity.Player;
import com.isofarm.data.SoundGroup;
import com.isofarm.service.SoundService;
import com.isofarm.utils.HoveredCell;
import com.isofarm.utils.Settings;
import com.isofarm.wrld.GameMaster;
import com.isofarm.wrld.World;
import org.joml.Vector3f;

/**
 * Encapsulates the state and operations required by step controller within the game runtime.
 */
@Singleton
public class StepController {
    public static final StepController step = new StepController();
    private float stepDistanceAccumulator = 0.0f;
    private static final float STEP_DISTANCE_THRESHOLD = 1.6f;
    private final Vector3f lastPosition = new Vector3f();

    /**
     * Updates the current state.
     * @param gameMaster the {@link GameMaster} supplied as {@code gameMaster}
     * @param soundService the {@link SoundService} supplied as {@code soundService}
     * @param delta the {@code float} supplied as {@code delta}
     */
    public void update(GameMaster gameMaster, SoundService soundService, float delta) {
        Player player = Player.plyr;
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

            World world = gameMaster.getWorld();
            Block block = world.getBlockAt(blockX, blockY, blockZ);
            if (block != null && block.getType() != null) {
                SoundGroup soundGroup = block.getType().getSoundGroup();
                soundService.playStepSound(soundGroup);
            }
        }
    }
}
