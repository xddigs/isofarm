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

    public void update(GameMaster gameMaster, float delta) {
        if (gameMaster.isInventoryOpen() || gameMaster.isPromptingForInput()) return;
        movement(gameMaster, delta);
    }

    private void movement(GameMaster gameMaster, float delta) {
        Player player = gameMaster.getPlayer();
        if (player == null) return;

        float speed = K.Camera.MOVEMENT_SPEED;
        if (Keyboard.isKeyDown(GLFW_KEY_LEFT_SHIFT)) {
            speed *= K.Camera.SPRINT_MULTIPLIER;
        }

        Vector3f forward = camera.getMovementForwardVector();
        Vector3f right = new Vector3f(-forward.z, 0.0f, forward.x);
        Vector3f moveDir = new Vector3f();
        if (Keyboard.isKeyDown(GLFW_KEY_W)) moveDir.add(forward);
        if (Keyboard.isKeyDown(GLFW_KEY_S)) moveDir.sub(forward);
        if (Keyboard.isKeyDown(GLFW_KEY_D)) moveDir.add(right);
        if (Keyboard.isKeyDown(GLFW_KEY_A)) moveDir.sub(right);

        if (moveDir.lengthSquared() > 0) {
            moveDir.normalize().mul(speed);
        }

        player.moveAndCollide(gameMaster.getWorld(), moveDir, delta,
                gameMaster.isOrthographicCamera());

        camera.getPosition().set(player.getPosition().x,
                player.getPosition().y + 10.0f, player.getPosition().z);
    }

    public void release(GameMaster gameMaster) {}
}