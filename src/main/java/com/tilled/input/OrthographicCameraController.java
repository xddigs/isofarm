package com.tilled.input;

import com.tilled.graphics.OrthographicCamera;
import com.tilled.service.Service;
import com.tilled.utils.K;
import com.tilled.wrld.GameMaster;

import static org.lwjgl.glfw.GLFW.*;

public record OrthographicCameraController(OrthographicCamera camera) 
        implements Service<OrthographicCamera> {
    public void update(GameMaster gameMaster, float delta) {
        if (gameMaster.isInventoryOpen() || gameMaster.isPromptingForInput()) {
            return;
        }

        movement(delta);
    }

    private void movement(float delta) {
        float speed = K.Camera.MOVEMENT_SPEED * delta;

        float moveForward = 0.0f;
        float moveRight = 0.0f;

        if (Keyboard.isKeyPressed(GLFW_KEY_W)) {
            moveForward += 1.0f;
        }

        if (Keyboard.isKeyPressed(GLFW_KEY_S)) {
            moveForward -= 1.0f;
        }

        if (Keyboard.isKeyPressed(GLFW_KEY_D)) {
            moveRight += 1.0f;
        }

        if (Keyboard.isKeyPressed(GLFW_KEY_A)) {
            moveRight -= 1.0f;
        }

        if (moveForward == 0.0f && moveRight == 0.0f) {
            return;
        }

        float length = (float) Math.sqrt(moveForward * moveForward + moveRight * moveRight);
        moveForward /= length;
        moveRight /= length;

        camera.getPosition().add(camera.getRightVector()
                .mul(moveRight * speed), camera.getPosition());

        camera.getPosition().add(camera.getForwardVector()
                .mul(moveForward * speed), camera.getPosition());
    }

    public void release(GameMaster gameMaster) {}
}