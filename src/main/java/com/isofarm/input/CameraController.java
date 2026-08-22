package com.isofarm.input;

import com.isofarm.entity.Player;
import com.isofarm.graphics.Camera;
import com.isofarm.service.Service;
import com.isofarm.utils.K;
import com.isofarm.utils.Settings;
import com.isofarm.wrld.GameMaster;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

public class CameraController implements Service<Camera> {
    private final Camera camera;
    private boolean mouseCaptured = false;
    private final Vector3f targetVelocity;
    private final Vector3f currentFlyVelocity;
    private float bobTime = 0.0f;

    private boolean isFlying = false;
    private float spaceLastPressedTime = 0.0f;
    private static final float DOUBLE_TAP_TIME = 0.3f;

    private static final float FLY_SPEED_MULTIPLIER = 1.2f;
    private static final float FLY_DAMPING = 12.0f;

    public CameraController(Camera camera) {
        this.camera = camera;
        this.targetVelocity = new Vector3f();
        this.currentFlyVelocity = new Vector3f();
    }

    public Camera getCamera() {
        return camera;
    }

    public void update(GameMaster gameMaster, float delta) {
        if (gameMaster.isInventoryOpen() || gameMaster.isChatOpen()) {
            releaseMouse(gameMaster);
        } else {
            captureMouse(gameMaster);
            mouseLook();
        }

        Player player = gameMaster.getPlayer();
        if (player != null) {
            boolean isGodmode = player.getGamemode().isGodmode();
            boolean isNoClip = player.isNoClip();

            if (isNoClip) {
                isFlying = true;
            } else if (!isGodmode) {
                if (isFlying) {
                    isFlying = false;
                    currentFlyVelocity.set(0, 0, 0);
                }
            }

            if (isGodmode && !isNoClip && !gameMaster.isInventoryOpen()
                    && !gameMaster.isChatOpen()) {
                if (Keyboard.isKeyPressed(GLFW_KEY_SPACE)) {
                    float currentTime = (float) glfwGetTime();
                    if (currentTime - spaceLastPressedTime <= DOUBLE_TAP_TIME) {
                        isFlying = !isFlying;
                        if (!isFlying) {
                            currentFlyVelocity.set(0, 0, 0);
                        }
                    }
                    spaceLastPressedTime = currentTime;
                }
            }

            movement(gameMaster, delta);

            if (!isFlying) {
                if (!gameMaster.isInventoryOpen() && !gameMaster.isChatOpen()
                        && Keyboard.isKeyPressed(GLFW_KEY_SPACE)) {
                    player.jump();
                }

                if (Keyboard.isKeyDown(GLFW_KEY_LEFT_CONTROL)) {
                    player.crunch();
                } else {
                    player.uncrouch(gameMaster.getWorld());
                }
            }

            camera.setZooming(!gameMaster.isInventoryOpen() &&
                    !gameMaster.isChatOpen() && Keyboard.isKeyDown(GLFW_KEY_C));

            Vector3f eyePos = player.getEyePosition();
            if (!isFlying && player.isOnGround() && targetVelocity.lengthSquared() > 0.1f) {
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
        if (!gameMaster.isInventoryOpen() && !gameMaster.isChatOpen()) {
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
        float moveY = 0.0f;
        float moveZ = 0.0f;

        if (!gameMaster.isInventoryOpen() &&
                !gameMaster.isChatOpen()) {
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

            if (isFlying) {
                if (Keyboard.isKeyDown(GLFW_KEY_SPACE)) {
                    moveY += 1.0f;
                }
                if (Keyboard.isKeyDown(GLFW_KEY_LEFT_CONTROL)) {
                    moveY -= 1.0f;
                }
            }
        }

        float horizontalLength = (float) Math.sqrt(moveX * moveX + moveZ * moveZ);
        if (horizontalLength > 0.0f) {
            moveX = (moveX / horizontalLength) * speed;
            moveZ = (moveZ / horizontalLength) * speed;
        }

        if (isFlying) {
            moveX *= FLY_SPEED_MULTIPLIER;
            moveZ *= FLY_SPEED_MULTIPLIER;
            moveY *= (speed * FLY_SPEED_MULTIPLIER);

            targetVelocity.set(moveX, moveY, moveZ);
            currentFlyVelocity.lerp(targetVelocity, Math.clamp(delta * FLY_DAMPING, 0.0f, 1.0f));
            player.getPosition().add(
                    currentFlyVelocity.x * delta,
                    currentFlyVelocity.y * delta,
                    currentFlyVelocity.z * delta
            );

            player.getVelocity().set(0, 0, 0);
        } else {
            targetVelocity.set(moveX, 0.0f, moveZ);
            player.moveAndCollide(gameMaster.getWorld(), targetVelocity, delta);
        }
    }

    private void mouseLook() {
        float dx = Mouse.getDeltaX();
        float dy = Mouse.getDeltaY();

        float newYaw = camera.getYaw() + dx * Settings.getMouseSensitivity();
        float newPitch = camera.getPitch() + dy * Settings.getMouseSensitivity();

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

    public boolean isFlying() {
        return isFlying;
    }
}