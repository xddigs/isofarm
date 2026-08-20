package com.soilcraft.graphics;

import com.soilcraft.data.Hit;
import com.soilcraft.entity.Entity;
import com.soilcraft.utils.K;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CharacterAnimator {
    private static final float WALK_SPEED = 8.0f;
    private static final float WALK_ROTATION = 0.35f;
    private static final Logger log = LoggerFactory.getLogger(CharacterAnimator.class);
    private final CharacterModel model;
    private float animationTime = 0.0f;

    private float currentLeftArmRot = 0.0f;
    private float currentRightArmRot = 0.0f;
    private float currentLeftLegRot = 0.0f;
    private float currentRightLegRot = 0.0f;
    private float currentRotationY = 0.0f;
    private boolean rotationInitialized = false;

    public CharacterAnimator(CharacterModel model) {
        this.model = model;
    }

    public void update(Entity entity, Hit lookTarget, float delta) {
        animationTime += delta;
        resetModel();

        float horizontalSpeed = (float) Math.sqrt(entity.getVelocity().x * entity.getVelocity().x +
                entity.getVelocity().z * entity.getVelocity().z);

        float targetLeftArm;
        float targetRightArm;
        float targetLeftLeg;
        float targetRightLeg;

        if (!entity.isOnGround()) {
            boolean rising = entity.getVelocity().y > 0.0f;

            targetLeftArm = rising ? -0.35f : 0.25f;
            targetRightArm = rising ? -0.35f : 0.25f;

            targetLeftLeg = rising ? 0.15f : -0.1f;
            targetRightLeg = rising ? -0.15f : 0.1f;

        } else if (entity.isCrounching()) {
            model.getBody().getPosition().y -= 0.25f;

            targetLeftArm = -0.2f;
            targetRightArm = -0.2f;

            targetLeftLeg = 0.15f;
            targetRightLeg = -0.15f;

        } else if (horizontalSpeed > 0.05f) {
            float animationSpeed = WALK_SPEED * Math.min(horizontalSpeed / 3.0f, 1.5f);
            float swing = (float) Math.sin(animationTime * animationSpeed) * WALK_ROTATION;

            targetLeftArm = -swing;
            targetRightArm = swing;
            targetLeftLeg = swing;
            targetRightLeg = -swing;

        } else {
            float breathing = (float) Math.sin(animationTime * 2.0f);
            model.getBody().getPosition().y += breathing * 0.015f;
            targetLeftArm = breathing * 0.02f;
            targetRightArm = breathing * 0.02f;

            targetLeftLeg = 0.0f;
            targetRightLeg = 0.0f;
        }

        float lerpSpeed = Math.min(delta * 15.0f, 1.0f);

        currentLeftArmRot = lerp(currentLeftArmRot, targetLeftArm, lerpSpeed);
        currentRightArmRot = lerp(currentRightArmRot, targetRightArm, lerpSpeed);
        currentLeftLegRot = lerp(currentLeftLegRot, targetLeftLeg, lerpSpeed);
        currentRightLegRot = lerp(currentRightLegRot, targetRightLeg, lerpSpeed);

        model.getLeftArm().getRotation().x = currentLeftArmRot;
        model.getRightArm().getRotation().x = currentRightArmRot;

        model.getLeftLeg().getRotation().x = currentLeftLegRot;
        model.getRightLeg().getRotation().x = currentRightLegRot;
        updateLookRotation(entity, lookTarget, delta);
    }

    private void updateLookRotation(Entity entity, Hit lookTarget,
                                    float delta) {

        if (lookTarget == null) return;
        float tileSize = K.World.TILE_SIZE;

        float targetX = (lookTarget.x() + 0.5f) * tileSize;
        float targetZ = (lookTarget.z() + 0.5f) * tileSize;

        float dx = targetX - entity.getPosition().x;
        float dz = targetZ - entity.getPosition().z;

        if (dx * dx + dz * dz < 0.0001f) {
            return;
        }

        float targetRotation = (float) Math.atan2(dx, dz);

        if (!rotationInitialized) {
            currentRotationY = targetRotation;
            rotationInitialized = true;
        } else {
            float rotationLerp = Math.min(delta * 12.0f, 1.0f);
            currentRotationY = lerpAngle(currentRotationY,
                    targetRotation, rotationLerp);
        }

        model.setRotationY(currentRotationY);
    }

    private void resetModel() {
        model.getBody().resetTransform();
        model.getLeftArm().resetTransform();
        model.getRightArm().resetTransform();
        model.getLeftLeg().resetTransform();
        model.getRightLeg().resetTransform();
    }

    private float lerp(float start, float end, float alpha) {
        return start + alpha * (end - start);
    }

    private float lerpAngle(float start, float end, float alpha) {
        float difference = end - start;
        while (difference > Math.PI) {
            difference -= (float) (Math.PI * 2.0f);
        }

        while (difference < -Math.PI) {
            difference += (float) (Math.PI * 2.0f);
        }
        return start + difference * alpha;
    }
}