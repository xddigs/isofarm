package com.tilled.input;

import com.tilled.graphics.Camera;
import com.tilled.service.Service;
import com.tilled.utils.K;
import com.tilled.wrld.GameMaster;

import static org.lwjgl.glfw.GLFW.*;

public class CameraController implements Service<Camera> {
    private final Camera camera;
    private boolean mouseCaptured = false;

    public CameraController(Camera camera) {
        this.camera = camera;
    }

    public Camera get() {
        return camera;
    }

    public void update(GameMaster gameMaster,float delta) {
        if (gameMaster.isInventoryOpen() || gameMaster.isPromptingForInput()) {
            releaseMouse(gameMaster);
            return;
        }

        captureMouse(gameMaster);
        movement(delta);
        mouseLook();
    }

    private void movement(float delta) {
        float speed = K.Camera.MOVEMENT_SPEED * delta;
        float yaw = (float)Math.toRadians(camera.getYaw());

        float forwardX = (float)Math.sin(yaw);
        float forwardZ = (float)Math.cos(yaw);

        float rightX = (float)-Math.cos(yaw);
        float rightZ = (float)Math.sin(yaw);

        float x = 0.0f;
        float z = 0.0f;

        if (Keyboard.isKeyDown(GLFW_KEY_W)) {
            x += forwardX;
            z += forwardZ;
        }

        if (Keyboard.isKeyDown(GLFW_KEY_S)) {
            x -= forwardX;
            z -= forwardZ;
        }

        if (Keyboard.isKeyDown(GLFW_KEY_D)) {
            x += rightX;
            z += rightZ;
        }

        if (Keyboard.isKeyDown(GLFW_KEY_A)) {
            x -= rightX;
            z -= rightZ;
        }

        float length = (float)Math.sqrt(x * x + z * z);

        if (length > 0.0f) {
            x /= length;
            z /= length;

            camera.getPosition().add(x * speed,0.0f,z * speed);
        }

        if (Keyboard.isKeyDown(GLFW_KEY_SPACE)) {
            camera.getPosition().y += speed;
        }

        if (Keyboard.isKeyDown(GLFW_KEY_LEFT_SHIFT)) {
            camera.getPosition().y -= speed;
        }
    }

    private void mouseLook() {
        float dx = Mouse.getDeltaX();
        float dy = Mouse.getDeltaY();

        if (dx != 0.0f) camera.rotateYaw(-dx * K.Camera.ROTATION_SENSITIVITY);
        if (dy != 0.0f) camera.rotatePitch(dy * K.Camera.ROTATION_SENSITIVITY);
    }

    private void captureMouse(GameMaster gameMaster) {
        if (mouseCaptured) return;

        glfwSetInputMode(gameMaster.getWindowHandle(),GLFW_CURSOR,GLFW_CURSOR_DISABLED);
        mouseCaptured = true;
    }

    private void releaseMouse(GameMaster gameMaster) {
        if (!mouseCaptured) return;

        glfwSetInputMode(gameMaster.getWindowHandle(),GLFW_CURSOR,GLFW_CURSOR_NORMAL);
        mouseCaptured = false;
    }

    public void release(GameMaster gameMaster) {
        releaseMouse(gameMaster);
    }
}