package com.soilcraft.input;

import com.soilcraft.data.Block;
import com.soilcraft.entity.Player;
import com.soilcraft.data.SoundGroup;
import com.soilcraft.service.SoundService;
import com.soilcraft.utils.Settings;
import com.soilcraft.wrld.GameMaster;
import com.soilcraft.wrld.World;
import org.joml.Vector3f;

public class StepController {
    private float stepDistanceAccumulator = 0.0f;
    private static final float STEP_DISTANCE_THRESHOLD = 1.6f;
    private final Vector3f lastPosition = new Vector3f();

    public void update(GameMaster gameMaster, Player player, SoundService soundService, float delta) {
        if (player == null) return;
        if (gameMaster.isOrthographicCamera()) return;
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
                soundService.playStepSound(soundGroup, gameMaster.getGameInteraction().getDistanceToBlock(gameMaster,
                        gameMaster.getGameInteraction().getHoveredCell(gameMaster)), Settings.maxInteractionDistance);
            }
        }
    }
}