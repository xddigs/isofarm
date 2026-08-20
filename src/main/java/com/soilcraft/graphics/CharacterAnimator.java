package com.soilcraft.graphics;

import com.soilcraft.entity.Entity;

public class CharacterAnimator {
    private static final float WALK_SPEED = 8.0f;
    private static final float WALK_ROTATION = 0.35f;
    private final CharacterModel model;
    private float animationTime = 0.0f;

    private float currentLeftArmRot = 0.0f;
    private float currentRightArmRot = 0.0f;
    private float currentLeftLegRot = 0.0f;
    private float currentRightLegRot = 0.0f;

    public CharacterAnimator(CharacterModel model) {
        this.model = model;
    }

    public void update(Entity entity, float delta) {
        animationTime += delta;

        resetModel();
        float horizontalSpeed = (float) Math.sqrt(
                entity.getVelocity().x * entity.getVelocity().x +
                        entity.getVelocity().z * entity.getVelocity().z
        );

        float targetLeftArm, targetRightArm;
        float targetLeftLeg, targetRightLeg;

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

        float lerpSpeed = delta * 15.0f;
        currentLeftArmRot = lerp(currentLeftArmRot, targetLeftArm, lerpSpeed);
        currentRightArmRot = lerp(currentRightArmRot, targetRightArm, lerpSpeed);
        currentLeftLegRot = lerp(currentLeftLegRot, targetLeftLeg, lerpSpeed);
        currentRightLegRot = lerp(currentRightLegRot, targetRightLeg, lerpSpeed);

        model.getLeftArm().getRotation().x = currentLeftArmRot;
        model.getRightArm().getRotation().x = currentRightArmRot;
        model.getLeftLeg().getRotation().x = currentLeftLegRot;
        model.getRightLeg().getRotation().x = currentRightLegRot;
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
}