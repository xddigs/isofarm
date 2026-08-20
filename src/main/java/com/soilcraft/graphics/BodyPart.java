package com.soilcraft.graphics;

import org.joml.Vector3f;

public class BodyPart {
    private final Vector3f position;
    private final Vector3f rotation;
    private final Vector3f size;

    private final Vector3f basePosition;
    private final Vector3f baseRotation;
    private final Vector3f baseSize;

    public BodyPart() {
        this.position = new Vector3f();
        this.rotation = new Vector3f();
        this.size = new Vector3f(1.0f);

        this.basePosition = new Vector3f();
        this.baseRotation = new Vector3f();
        this.baseSize = new Vector3f(1.0f);
    }

    public Vector3f getPosition() {
        return position;
    }

    public Vector3f getRotation() {
        return rotation;
    }

    public Vector3f getSize() {
        return size;
    }

    public Vector3f getBasePosition() {
        return basePosition;
    }

    public Vector3f getBaseRotation() {
        return baseRotation;
    }

    public Vector3f getBaseSize() {
        return baseSize;
    }

    public void saveBaseTransform() {
        basePosition.set(position);
        baseRotation.set(rotation);
        baseSize.set(size);
    }

    public void resetTransform() {
        position.set(basePosition);
        rotation.set(baseRotation);
        size.set(baseSize);
    }
}