package com.tilled.graphics;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public interface CameraView {
    Matrix4f getViewMatrix();
    Matrix4f getProjectionMatrix();
    Vector3f getPosition();
    float getPitch();
    float getYaw();
}