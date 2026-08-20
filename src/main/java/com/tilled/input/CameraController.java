package com.tilled.input;

import com.tilled.entity.Player;
import com.tilled.graphics.Camera;
import com.tilled.service.Service;
import com.tilled.utils.K;
import com.tilled.utils.Settings;
import com.tilled.wrld.GameMaster;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

public class CameraController implements Service<Camera> {
    private final Camera camera;
    private boolean mouseCaptured = false;
    private final Vector3f targetVelocity;
    private float bobTime = 0.0f;

    public CameraController(Camera camera) {
        this.camera = camera;
        this.targetVelocity = new Vector3f();
    }

    public Camera getCamera() {
        return camera;
    }

    public void update(GameMaster gameMaster, float delta) {
        if (gameMaster.isInventoryOpen() || gameMaster.isPromptingForInput()) {
            releaseMouse(gameMaster);
        } else {
            captureMouse(gameMaster);
            mouseLook();
        }

        Player player = gameMaster.getPlayer();
        if (player != null) {
            movement(gameMaster, delta);
            if (!gameMaster.isInventoryOpen() && !gameMaster.isPromptingForInput()
                    && Keyboard.isKeyPressed(GLFW_KEY_SPACE)) {
                player.jump();
            }

            camera.setZooming(!gameMaster.isInventoryOpen() &&
                    !gameMaster.isPromptingForInput() && Keyboard.isKeyDown(GLFW_KEY_C));
            
            Vector3f eyePos = player.getEyePosition();
            if (player.isOnGround() && targetVelocity.lengthSquared() > 0.1f) {
                boolean isSprinting = Keyboard.isKeyDown(GLFW_KEY_LEFT_SHIFT);
                float speedFactor = isSprinting ? K.Camera.SPRINT_MULTIPLIER : 1.0f;
                bobTime += delta * K.Camera.BOB_FREQUENCY * speedFactor;
                float bobOffset = (float) Math.sin(bobTime) * K.Camera.BOB_AMOUNT;
                eyePos.y += bobOffset;
            } else {
                bobTime = 0;
            }
            camera.getPosition().set(eyePos);
        }
    }

    private void movement(GameMaster gameMaster, float delta) {
        Player player = gameMaster.getPlayer();

        float speed = K.Camera.MOVEMENT_SPEED;
        if (!gameMaster.isInventoryOpen() && !gameMaster.isPromptingForInput()) {
            if (Keyboard.isKeyDown(GLFW_KEY_LEFT_SHIFT)) {
                speed *= K.Camera.SPRINT_MULTIPLIER;
            }
        }

        float yaw = (float) Math.toRadians(camera.getYaw());

        float forwardX = (float) Math.sin(yaw);
        float forwardZ = (float) -Math.cos(yaw);

        float rightX = (float) Math.cos(yaw);
        float rightZ = (float) Math.sin(yaw);

        float moveX = 0.0f;
        float moveZ = 0.0f;

        if (!gameMaster.isInventoryOpen() && !gameMaster.isPromptingForInput()) {
            if (Keyboard.isKeyDown(GLFW_KEY_W)) {
                moveX += forwardX;
                moveZ += forwardZ;
            }

            if (Keyboard.isKeyDown(GLFW_KEY_S)) {
                moveX -= forwardX;
                moveZ -= forwardZ;
            }

            if (Keyboard.isKeyDown(GLFW_KEY_D)) {
                moveX += rightX;
                moveZ += rightZ;
            }

            if (Keyboard.isKeyDown(GLFW_KEY_A)) {
                moveX -= rightX;
                moveZ -= rightZ;
            }
        }

        float length = (float) Math.sqrt(moveX * moveX + moveZ * moveZ);

        if (length > 0.0f) {
            moveX = (moveX / length) * speed;
            moveZ = (moveZ / length) * speed;
        }

        targetVelocity.set(moveX, 0.0f, moveZ);
        player.moveAndCollide(gameMaster.getWorld(), targetVelocity, delta,
                gameMaster.isOrthographicCamera());
    }

    private void mouseLook() {
        float dx = Mouse.getDeltaX();
        float dy = Mouse.getDeltaY();

        float newYaw = camera.getYaw() + dx * Settings.mouseSensitivity;
        float newPitch = camera.getPitch() + dy * Settings.mouseSensitivity;

        camera.setYaw(newYaw);
        camera.setPitch(Math.clamp(newPitch, -89.0f, 89.0f));
    }

    private void captureMouse(GameMaster gameMaster) {
        if (mouseCaptured) return;
        glfwSetInputMode(gameMaster.getWindowHandle(), GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        mouseCaptured = true;
    }

    private void releaseMouse(GameMaster gameMaster) {
        if (!mouseCaptured) return;
        glfwSetInputMode(gameMaster.getWindowHandle(), GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        mouseCaptured = false;
    }

    public void release(GameMaster gameMaster) {
        releaseMouse(gameMaster);
    }
}