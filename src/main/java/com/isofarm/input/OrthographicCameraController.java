package com.isofarm.input;

import com.isofarm.data.BlockData;
import com.isofarm.data.Hit;
import com.isofarm.entity.Player;
import com.isofarm.graphics.OrthographicCamera;
import com.isofarm.paths.AStar;
import com.isofarm.paths.GridPos;
import com.isofarm.service.Service;
import com.isofarm.utils.K;
import com.isofarm.wrld.GameMaster;
import com.isofarm.wrld.World;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

public record OrthographicCameraController(OrthographicCamera camera)
        implements Service<OrthographicCamera> {

    private static boolean mouseCaptured = false;
    private static final float NORMAL_ZOOM = 18.0f;
    private static final float ZOOMED_ZOOM = NORMAL_ZOOM / 2.5f;
    private static final float verticalOffset = 0.0f;
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
        World world = gameMaster.getWorld();

        if (player == null) return;
        click(gameMaster, player, world);
        player.move(world, delta);
        followPlayer(player);
    }

    private void click(GameMaster gameMaster, Player player, World world) {
        if (!Mouse.isButtonPressed(GLFW_MOUSE_BUTTON_LEFT)) {
            return;
        }

        float mouseX = Mouse.getX();
        float mouseY = Mouse.getY();

        float screenWidth = gameMaster.getWindowWidth();
        float screenHeight = gameMaster.getWindowHeight();

        Hit hit = camera.highlight(world, mouseX, mouseY, screenWidth, screenHeight);
        if (hit == null) return;

        GridPos start = getPlayerGridPosition(player);
        GridPos goal = getGoalPosition(world, hit);

        if (goal == null) {
            return;
        }

        if (start.equals(goal)) {
            return;
        }

        var path = AStar.findPath(world, start, goal);
        if (!path.isEmpty()) {
            player.setPath(path);
        }
    }

    private void followPlayer(Player player) {
        Vector3f playerPos = player.getPosition();
        Vector3f camForward = camera.getForwardVector();
        Vector3f camUp = camera.getUpVector();
        Vector3f cameraPos = new Vector3f(playerPos)
                .sub(new Vector3f(camForward).mul(distance))
                .add(new Vector3f(camUp).mul(verticalOffset));

        camera.getPosition().set(cameraPos);
    }

    private GridPos getPlayerGridPosition(Player player) {
        Vector3f position = player.getPosition();

        return new GridPos(
                (int) Math.floor(position.x),
                (int) Math.floor(position.y),
                (int) Math.floor(position.z)
        );
    }

    private GridPos getGoalPosition(World world, Hit hit) {
        int x = hit.x();
        int y = hit.y();
        int z = hit.z();

        if (hit.normalY() > 0) return new GridPos(x, y + 1, z);
        return world.getHighestY(x, z);
    }

    private boolean shouldAutoJump(Player player, World world, Vector3f moveDir) {
        if (moveDir.lengthSquared() == 0.0f) {
            return false;
        }

        Vector3f direction = new Vector3f(moveDir).normalize();
        Vector3f playerPos = player.getPosition();

        float checkDistance = 0.6f;
        int checkX = (int) Math.floor(playerPos.x + direction.x * checkDistance);
        int checkZ = (int) Math.floor(playerPos.z + direction.z * checkDistance);
        int playerY = (int) Math.floor(playerPos.y);

        byte blockAtFeet = world.getBlockTypeAt(checkX, playerY, checkZ);
        byte blockAtHead = world.getBlockTypeAt(checkX, playerY + 1, checkZ);
        return blockAtFeet != BlockData.AIR.getId() && blockAtHead == BlockData.AIR.getId();
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