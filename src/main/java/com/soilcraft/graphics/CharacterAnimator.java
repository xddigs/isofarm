package com.soilcraft.graphics;

import com.soilcraft.entity.Entity;

public class CharacterAnimator {
    private static final float WALK_SPEED = 8.0f;
    private static final float WALK_ROTATION = 0.35f;
    private final CharacterModel model;
    private float animationTime = 0.0f;

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

        if (!entity.isOnGround()) {
            animateAirborne(entity, delta);
        } else if (entity.isCrounching()) {
            animateCrouch(entity, delta);
        } else if (horizontalSpeed > 0.05f) {
            animateWalk(entity, horizontalSpeed, delta);
        } else {
            animateIdle(entity, delta);
        }
    }

    private void resetModel() {
        model.getBody().resetTransform();
        model.getLeftArm().resetTransform();
        model.getRightArm().resetTransform();
        model.getLeftLeg().resetTransform();
        model.getRightLeg().resetTransform();
    }

    private void animateIdle(Entity entity, float delta) {
        BodyPart body = model.getBody();
        BodyPart leftArm = model.getLeftArm();
        BodyPart rightArm = model.getRightArm();

        float breathing = (float) Math.sin(animationTime * 2.0f);

        body.getPosition().y += breathing * 0.015f;

        leftArm.getRotation().x = breathing * 0.02f;
        rightArm.getRotation().x = breathing * 0.02f;
    }

    private void animateWalk(Entity entity, float speed, float delta) {
        float animationSpeed = WALK_SPEED * Math.min(speed / 3.0f, 1.5f);
        float swing = (float) Math.sin(animationTime * animationSpeed) * WALK_ROTATION;

        model.getLeftArm().getRotation().x = -swing;
        model.getRightArm().getRotation().x = swing;

        model.getLeftLeg().getRotation().x = swing;
        model.getRightLeg().getRotation().x = -swing;
    }

    private void animateCrouch(Entity entity, float delta) {
        BodyPart body = model.getBody();
        BodyPart leftArm = model.getLeftArm();
        BodyPart rightArm = model.getRightArm();
        BodyPart leftLeg = model.getLeftLeg();
        BodyPart rightLeg = model.getRightLeg();

        body.getPosition().y -= 0.25f;
        leftArm.getRotation().x = -0.2f;
        rightArm.getRotation().x = -0.2f;
        leftLeg.getRotation().x = 0.15f;
        rightLeg.getRotation().x = -0.15f;
    }

    private void animateAirborne(Entity entity, float delta) {
        BodyPart leftArm = model.getLeftArm();
        BodyPart rightArm = model.getRightArm();
        BodyPart leftLeg = model.getLeftLeg();
        BodyPart rightLeg = model.getRightLeg();
        boolean rising = entity.getVelocity().y > 0.0f;

        if (rising) {
            leftArm.getRotation().x = -0.35f;
            rightArm.getRotation().x = -0.35f;

            leftLeg.getRotation().x = 0.15f;
            rightLeg.getRotation().x = -0.15f;
        } else {
            leftArm.getRotation().x = 0.25f;
            rightArm.getRotation().x = 0.25f;

            leftLeg.getRotation().x = -0.1f;
            rightLeg.getRotation().x = 0.1f;
        }
    }
}