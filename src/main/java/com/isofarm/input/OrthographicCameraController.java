package com.isofarm.input;

import com.isofarm.data.Hit;
import com.isofarm.entity.Player;
import com.isofarm.graphics.OrthographicCamera;
import com.isofarm.pathfinding.GridPos;
import com.isofarm.pathfinding.PathFinder;
import com.isofarm.service.Service;
import com.isofarm.wrld.GameMaster;
import com.isofarm.wrld.World;
import org.joml.Vector3f;

import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

public record OrthographicCameraController(OrthographicCamera camera)
        implements Service<OrthographicCamera> {
    private static final float NORMAL_ZOOM = 18.0f;
    private static final float ZOOMED_ZOOM = NORMAL_ZOOM / 2.5f;
    private static final float VERTICAL_OFFSET = 0.0f;
    private static final float DISTANCE = 500.0f;
    private static final float PATH_SPEED = 4.0f;
    private static final float PATH_REACH_DISTANCE = 0.08f;
    private static boolean mouseCaptured = false;

    public void update(GameMaster gameMaster, float delta) {
        if (gameMaster.isInventoryOpen() || gameMaster.isPromptingForInput()) {
            return;
        }

        Player player = gameMaster.getPlayer();
        if (player == null) return;

        click(gameMaster, player, gameMaster.getWorld());
        followPath(player, gameMaster.getWorld(), delta);
        followPlayer(player);
        updateZoom();
    }

    private void updateZoom() {
        boolean isPressingC = Keyboard.isKeyDown(GLFW_KEY_C);
        float targetZoom = isPressingC ? ZOOMED_ZOOM : NORMAL_ZOOM;
        if (camera.getZoom() != targetZoom) {
            camera.setZoom(targetZoom);
        }
    }

    private void click(GameMaster gameMaster, Player player, World world) {
        if (!Mouse.isButtonPressed(GLFW_MOUSE_BUTTON_LEFT)) {
            return;
        }

        float mouseX = Mouse.getX();
        float mouseY = Mouse.getY();

        float screenWidth = gameMaster.getWindowWidth();
        float screenHeight = gameMaster.getWindowHeight();

        Hit hit = camera.highlight(world, mouseX, mouseY,
                screenWidth, screenHeight);

        if (hit == null) return;

        GridPos start = PathFinder.getPlayerGridPosition(player);
        GridPos goal = getGoalPosition(world, hit);

        if (goal == null) return;
        if (start.equals(goal)) return;
        var path = PathFinder.findPath(world, player, start, goal);

        if (path.isEmpty()) return;
        player.setPath(path);
    }

    private void followPath(Player player, World world, float delta) {
        if (!player.isFollowingPath()) {
            player.move(world, delta);
            return;
        }

        List<GridPos> path = player.getPath();
        int pathIndex = player.getPathIndex();

        if (pathIndex >= path.size()) {
            player.clearPath();
            stopPlayer(player, world, delta);
            return;
        }

        GridPos target = path.get(pathIndex);
        Vector3f playerPosition = player.getPosition();

        float targetX = target.x() + 0.5f;
        float targetZ = target.z() + 0.5f;

        float dx = targetX - playerPosition.x;
        float dz = targetZ - playerPosition.z;

        int currentY = (int) Math.floor(playerPosition.y);
        if (target.y() > currentY && player.isOnGround()) {
            player.jump();
        }

        float distanceSquared = dx * dx + dz * dz;

        if (distanceSquared <= PATH_REACH_DISTANCE * PATH_REACH_DISTANCE) {
            player.setPosition(targetX, playerPosition.y, targetZ);
            int nextIndex = pathIndex + 1;
            player.setPathIndex(nextIndex);

            if (nextIndex >= path.size()) {
                player.clearPath();
                stopPlayer(player, world, delta);
            }
            return;
        }

        Vector3f direction = new Vector3f(dx, 0.0f, dz);
        if (direction.lengthSquared() > 0.0f) {
            direction.normalize().mul(PATH_SPEED);
        }

        player.move(world, delta);
        player.lookAt(targetX, targetZ, camera.getYaw());
    }

    private void stopPlayer(Player player, World world, float delta) {
        player.move(world, delta);
    }

    private GridPos getGoalPosition(World world, Hit hit) {
        int x = hit.x();
        int y = hit.y();
        int z = hit.z();

        if (hit.normalY() > 0) {
            return new GridPos(x, y + 1, z);
        }

        GridPos highestAltitude = world.getHighestY(x + 0.5f, z + 0.5f);
        int walkY = highestAltitude.y();
        if (walkY < 0) return null;
        return new GridPos(x, walkY, z);
    }

    private void followPlayer(Player player) {
        Vector3f playerPos = player.getPosition();
        Vector3f camForward = camera.getForwardVector();
        Vector3f camUp = camera.getUpVector();
        Vector3f cameraPos = new Vector3f(playerPos)
                .sub(new Vector3f(camForward).mul(DISTANCE))
                .add(new Vector3f(camUp).mul(VERTICAL_OFFSET));
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