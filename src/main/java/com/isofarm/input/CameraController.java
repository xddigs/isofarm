package com.isofarm.input;

import com.isofarm.data.BlockPos;
import com.isofarm.data.Ray;
import com.isofarm.entity.Player;
import com.isofarm.entity.states.SwimmingState;
import com.isofarm.graphics.Camera;
import com.isofarm.pathfinding.GridPos;
import com.isofarm.pathfinding.PathFinder;
import com.isofarm.service.Service;
import com.isofarm.wrld.GameMaster;
import com.isofarm.wrld.World;
import org.joml.Vector3f;

import static org.joml.Math.lerp;
import static org.lwjgl.glfw.GLFW.*;

public record CameraController(Camera camera) implements Service<Camera> {
    private static final float NORMAL_ZOOM = 18.0f;
    private static final float ZOOMED_ZOOM = NORMAL_ZOOM / 2.5f;
    private static final float VERTICAL_OFFSET = 0.0f;
    private static final float DISTANCE = 500.0f;
    private static final Vector3f currentOffset = new Vector3f(0, 0, 0);
    private static final float NORMAL_CURSOR_WEIGHT = 0.35f;
    private static final float ZOOMED_CURSOR_WEIGHT = 0.50f;
    private static final float MAX_CURSOR_OFFSET_DISTANCE = 8.0f;
    private static boolean mouseCaptured = false;
    private static GridPos lastGoal = null;

    public void update(GameMaster gameMaster, float delta) {
        if (gameMaster.isInventoryOpen() || gameMaster.isChatOpen()) {
            lastGoal = null;
            return;
        }

        Player player = gameMaster.getPlayer();
        if (player == null) return;

        if (Keyboard.isKeyDown(GLFW_KEY_W) || Keyboard.isKeyDown(GLFW_KEY_A) ||
                Keyboard.isKeyDown(GLFW_KEY_S) || Keyboard.isKeyDown(GLFW_KEY_D)) {
            player.clearPath();
        }

        if (player.getCurrentState() instanceof SwimmingState) {
            if (Keyboard.isKeyDown(GLFW_KEY_SPACE)) {
                player.getVelocity().y = 4.0f;
            } else if (Keyboard.isKeyDown(GLFW_KEY_LEFT_CONTROL)) {
                player.getVelocity().y = -3.0f;
            }
        }

//        click(gameMaster, player, gameMaster.getWorld());
//        followPath(player, gameMaster.getWorld(), delta);
        updateZoom();
        followPlayer(gameMaster, player, delta);
    }

    private void updateZoom() {
        boolean isPressingC = Keyboard.isKeyDown(GLFW_KEY_C);
        float targetZoom = isPressingC ? ZOOMED_ZOOM : NORMAL_ZOOM;
        if (camera.getZoom() != targetZoom) {
            camera.setZoom(targetZoom);
        }
    }

    private void followPlayer(GameMaster gameMaster, Player player, float delta) {
        Vector3f playerPos = player.getPosition();

        float mouseX = Mouse.getX();
        float mouseY = Mouse.getY();
        float screenWidth = gameMaster.getWindowWidth();
        float screenHeight = gameMaster.getWindowHeight();

        Vector3f mouseWorldPos = getMouseWorldPosition(
                mouseX, mouseY, screenWidth, screenHeight, playerPos.y);

        Vector3f directionToMouse = new Vector3f(mouseWorldPos).sub(playerPos);
        directionToMouse.y = 0.0f;

        boolean isZoomed = Keyboard.isKeyDown(GLFW_KEY_C);
        float cursorWeight = isZoomed ? ZOOMED_CURSOR_WEIGHT : NORMAL_CURSOR_WEIGHT;

        Vector3f targetOffset = new Vector3f(directionToMouse).mul(cursorWeight);

        if (targetOffset.length() > MAX_CURSOR_OFFSET_DISTANCE) {
            targetOffset.normalize().mul(MAX_CURSOR_OFFSET_DISTANCE);
        }

        float lerpFactor = Math.min(1.0f, 8.0f * delta);
        currentOffset.x = lerp(currentOffset.x, targetOffset.x, lerpFactor);
        currentOffset.z = lerp(currentOffset.z, targetOffset.z, lerpFactor);

        Vector3f targetFocus = new Vector3f(playerPos).add(currentOffset);
        Vector3f camForward = camera.getForwardVector();
        Vector3f camUp = camera.getUpVector();
        Vector3f cameraPos = new Vector3f(targetFocus)
                .sub(new Vector3f(camForward).mul(DISTANCE))
                .add(new Vector3f(camUp).mul(VERTICAL_OFFSET));

        camera.getPosition().set(cameraPos);
    }

    private void click(GameMaster gameMaster, Player player, World world) {
        boolean isRightClickDown = Mouse.isButtonDown(GLFW_MOUSE_BUTTON_RIGHT);
        if (!isRightClickDown) {
            lastGoal = null;
            return;
        }

        float mouseX = Mouse.getX();
        float mouseY = Mouse.getY();
        float screenWidth = gameMaster.getWindowWidth();
        float screenHeight = gameMaster.getWindowHeight();

        BlockPos blockPos = camera.highlight(world, player.getPosition(), mouseX, mouseY,
                screenWidth, screenHeight, false);

        if (blockPos == null) return;
        GridPos start = PathFinder.getPlayerGridPosition(player);
        GridPos goal = getGoalPosition(world, blockPos);
        if (goal == null) return;
        if (start.equals(goal)) return;
        if (goal.equals(lastGoal)) return;
        var path = PathFinder.findPath(world, player, start, goal);
        if (path.isEmpty()) return;
        player.setPath(path);
        lastGoal = goal;
    }

    private void followPath(Player player, World world, float delta) {
        if (player.isFollowingPath()) {
            player.move(world, delta);
        }
    }

    private GridPos getGoalPosition(World world, BlockPos blockPos) {
        int x = blockPos.x();
        int z = blockPos.z();

        GridPos highestAltitude = world.getHighestY(x + 0.5f, z + 0.5f);
        int walkY = highestAltitude.y();
        if (walkY < 0) return null;
        return new GridPos(x, walkY, z);
    }

    public Vector3f getMouseWorldPosition(float mouseX, float mouseY, float screenWidth,
                                          float screenHeight, float planeY) {
        Ray ray = camera.getMouseRay(mouseX, mouseY, screenWidth, screenHeight);
        if (Math.abs(ray.direction().y) < 0.0001f) {
            return new Vector3f(ray.origin());
        }

        float t = (planeY - ray.origin().y) / ray.direction().y;
        return new Vector3f(ray.origin()).add(new Vector3f(ray.direction()).mul(t));
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