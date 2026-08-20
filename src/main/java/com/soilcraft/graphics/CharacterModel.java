package com.soilcraft.graphics;

public class CharacterModel {
    private final BodyPart body;
    private final BodyPart leftArm;
    private final BodyPart rightArm;
    private final BodyPart leftLeg;
    private final BodyPart rightLeg;
    private float rotationY = 0.0f;

    public CharacterModel() {
        this.body = new BodyPart();
        this.leftArm = new BodyPart();
        this.rightArm = new BodyPart();
        this.leftLeg = new BodyPart();
        this.rightLeg = new BodyPart();
        configure();
    }

    private void configure() {
        float bodyScale = 1.0f;
        float armRatio = 0.5f;
        float legRatio = 0.3f;

        body.getSize().set(bodyScale);
        body.getPosition().set(0.0f, bodyScale * 0.5f, 0.0f);

        float halfBody = bodyScale * 0.5f;
        float armY = bodyScale * armRatio;
        float armX = bodyScale * 0.25f;
        float legY = bodyScale * legRatio;
        float legX = bodyScale * 0.35f;

        float armCenterY = halfBody - (armY * 0.5f);

        leftArm.getSize().set(armX, armY, armX);
        leftArm.getPosition().set(-(halfBody + armX * 0.5f), armCenterY, 0.0f);

        rightArm.getSize().set(armX, armY, armX);
        rightArm.getPosition().set(halfBody + armX * 0.5f, armCenterY, 0.0f);

        float legCenterY = -(legY * 0.5f);

        leftLeg.getSize().set(legX, legY, legX);
        leftLeg.getPosition().set(-halfBody * 0.4f, legCenterY, 0.0f);

        rightLeg.getSize().set(legX, legY, legX);
        rightLeg.getPosition().set(halfBody * 0.4f, legCenterY, 0.0f);

        saveBaseTransforms();
    }

    public float getRotationY() {
        return rotationY;
    }

    public void setRotationY(float rotationY) {
        this.rotationY = rotationY;
    }

    public BodyPart getBody() { return body; }
    public BodyPart getLeftArm() { return leftArm; }
    public BodyPart getRightArm() { return rightArm; }
    public BodyPart getLeftLeg() { return leftLeg; }
    public BodyPart getRightLeg() { return rightLeg; }

    private void saveBaseTransforms() {
        body.saveBaseTransform();
        leftArm.saveBaseTransform();
        rightArm.saveBaseTransform();
        leftLeg.saveBaseTransform();
        rightLeg.saveBaseTransform();
    }
}