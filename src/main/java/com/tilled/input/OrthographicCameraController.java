package com.tilled.input;

import com.tilled.entity.Player;
import com.tilled.graphics.OrthographicCamera;
import com.tilled.service.Service;
import com.tilled.utils.K;
import com.tilled.wrld.GameMaster;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

public record OrthographicCameraController(OrthographicCamera camera)
        implements Service<OrthographicCamera> {

    private static boolean mouseCaptured = false;
    private static final float NORMAL_ZOOM = 18.0f;
    private static final float ZOOMED_ZOOM = NORMAL_ZOOM / 2.5f;
    private static float verticalOffset = 0.0f;
    private static final float distance = 500.0f;

    public void update(GameMaster gameMaster, float delta) {
        if (gameMaster.isInventoryOpen() || gameMaster.isPromptingForInput()) return;
        movement(gameMaster, delta);
        updateZoom();
    }

    private void updateZoom() {
        boolean isPressingC = Keyboard.isKeyDown(GLFW_KEY_C);
        float targetZoom = isPressingC ? ZOOMED_ZOOM : NORMAL_ZOOM;

        if (camera.getZoom() != targetZoom) {
            camera.setZoom(targetZoom);

        }
    }

    private void movement(GameMaster gameMaster, float delta) {
        Player player = gameMaster.getPlayer();
        if (player == null) return;

        float speed = K.Camera.MOVEMENT_SPEED * 1.5f;
        if (Keyboard.isKeyDown(GLFW_KEY_LEFT_SHIFT)) {
            speed *= K.Camera.SPRINT_MULTIPLIER;
        }

        Vector3f moveForward = camera.getMovementForwardVector();
        Vector3f right = new Vector3f(-moveForward.z, 0.0f, moveForward.x);
        Vector3f moveDir = new Vector3f();

        if (Keyboard.isKeyDown(GLFW_KEY_W)) moveDir.add(moveForward);
        if (Keyboard.isKeyDown(GLFW_KEY_S)) moveDir.sub(moveForward);
        if (Keyboard.isKeyDown(GLFW_KEY_D)) moveDir.add(right);
        if (Keyboard.isKeyDown(GLFW_KEY_A)) moveDir.sub(right);

        if (moveDir.lengthSquared() > 0) {
            moveDir.normalize().mul(speed);
        }

        player.moveAndCollide(gameMaster.getWorld(), moveDir, delta,
                gameMaster.isOrthographicCamera());

        if (Keyboard.isKeyDown(GLFW_KEY_SPACE)) {
            verticalOffset += speed * delta;
        }

        if (Keyboard.isKeyDown(GLFW_KEY_LEFT_CONTROL)) {
            verticalOffset -= speed * delta;
        }

        Vector3f playerPos = player.getPosition();
        Vector3f camForward = camera.getForwardVector();
        Vector3f camUp = camera.getUpVector();
        Vector3f cameraPos = new Vector3f(playerPos)
                .sub(new Vector3f(camForward).mul(distance))
                .add(new Vector3f(camUp).mul(verticalOffset));
        camera.getPosition().set(cameraPos);
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