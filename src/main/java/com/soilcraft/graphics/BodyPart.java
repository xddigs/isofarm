package com.soilcraft.graphics;

import org.joml.Vector3f;

public class BodyPart {
    private final Vector3f position;
    private final Vector3f rotation;
    private final Vector3f size;

    public BodyPart() {
        this.position = new Vector3f();
        this.rotation = new Vector3f();
        this.size = new Vector3f(1.0f);
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
}