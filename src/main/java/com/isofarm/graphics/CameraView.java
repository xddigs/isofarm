package com.isofarm.graphics;

import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Defines the camera view contract.
 */
public interface CameraView {
    /**
     * Returns the view matrix.
     * @return the view matrix
     */
    Matrix4f getViewMatrix();
    /**
     * Returns the projection matrix.
     * @return the projection matrix
     */
    Matrix4f getProjectionMatrix();
    /**
     * Returns the position.
     * @return the position
     */
    Vector3f getPosition();
    /**
     * Returns the pitch.
     * @return the pitch
     */
    float getPitch();
    /**
     * Returns the yaw.
     * @return the yaw
     */
    float getYaw();
}