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

/**
 * Stores camera controller data.
 */
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

    /**
     * Updates the current state.
     * @param gameMaster the game master value
     * @param delta the delta value
     */
    public void update(GameMaster gameMaster, float delta) {
        camera.updateDamageTilt(delta);

        if (gameMaster.isInventoryOpen() || gameMaster.isChatOpen()) {
            lastGoal = null;
            return;
        }

        Player player = Player.plyr;
        if (Controls.isDown(ControlAction.MOVE_FORWARD)
                || Controls.isDown(ControlAction.MOVE_BACKWARD)
                || Controls.isDown(ControlAction.MOVE_LEFT)
                || Controls.isDown(ControlAction.MOVE_RIGHT)
                || Controls.getAxis(ControlAction.MOVE_X) != 0.0f
                || Controls.getAxis(ControlAction.MOVE_Y) != 0.0f) {
            player.clearPath();
        }

        if (player.getCurrentState() instanceof SwimmingState) {
            if (Controls.isDown(ControlAction.SWIM_UP)) {
                player.getVelocity().y = 4.0f;
            } else if (Controls.isDown(ControlAction.SWIM_DOWN)) {
                player.getVelocity().y = -3.0f;
            }
        }

        updateZoom();
        followPlayer(gameMaster, delta);
    }

    /**
     * Updates the zoom.
     */
    private void updateZoom() {
        boolean isZooming = Controls.isDown(ControlAction.ZOOM);
        float targetZoom = isZooming ? ZOOMED_ZOOM : NORMAL_ZOOM;
        if (camera.getZoom() != targetZoom) {
            camera.setZoom(targetZoom);
        }
    }

    /**
     * Performs the follow player operation.
     * @param gameMaster the game master value
     * @param delta the delta value
     */
    private void followPlayer(GameMaster gameMaster, float delta) {
        Player player = Player.plyr;
        Vector3f playerPos = player.getPosition();

        float mouseX = Mouse.getX();
        float mouseY = Mouse.getY();
        float screenWidth = gameMaster.getWindowWidth();
        float screenHeight = gameMaster.getWindowHeight();

        Vector3f mouseWorldPos = getMouseWorldPosition(
                mouseX, mouseY, screenWidth, screenHeight, playerPos.y);

        Vector3f directionToMouse = new Vector3f(mouseWorldPos).sub(playerPos);
        directionToMouse.y = 0.0f;

        boolean isZoomed = Controls.isDown(ControlAction.ZOOM);
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

    /**
     * Performs the click operation.
     * @param gameMaster the game master value
     * @param world the world value
     */
    private void click(GameMaster gameMaster, World world) {
        Player player = Player.plyr;
        boolean isRightClickDown = Controls.isDown(ControlAction.PATHFIND);
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
        GridPos start = PathFinder.getPlayerGridPosition();
        GridPos goal = getGoalPosition(world, blockPos);
        if (goal == null) return;
        if (start.equals(goal)) return;
        if (goal.equals(lastGoal)) return;
        var path = PathFinder.findPath(world, start, goal);
        if (path.isEmpty()) return;
        player.setPath(path);
        lastGoal = goal;
    }

    /**
     * Performs the follow path operation.
     * @param world the world value
     * @param delta the delta value
     */
    private void followPath(World world, float delta) {
        Player player = Player.plyr;
        if (player.isFollowingPath()) {
            player.move(delta);
        }
    }

    /**
     * Returns the goal position.
     * @param world the world value
     * @param blockPos the block pos value
     * @return the goal position
     */
    private GridPos getGoalPosition(World world, BlockPos blockPos) {
        int x = blockPos.x();
        int z = blockPos.z();

        GridPos highestAltitude = world.getHighestY(x + 0.5f, z + 0.5f);
        int walkY = highestAltitude.y();
        if (walkY < 0) return null;
        return new GridPos(x, walkY, z);
    }

    /**
     * Returns the mouse world position.
     * @param mouseX the mouse x value
     * @param mouseY the mouse y value
     * @param screenWidth the screen width value
     * @param screenHeight the screen height value
     * @param planeY the plane y value
     * @return the mouse world position
     */
    public Vector3f getMouseWorldPosition(float mouseX, float mouseY, float screenWidth,
                                          float screenHeight, float planeY) {
        Ray ray = camera.getMouseRay(mouseX, mouseY, screenWidth, screenHeight);
        if (Math.abs(ray.direction().y) < 0.0001f) {
            return new Vector3f(ray.origin());
        }

        float t = (planeY - ray.origin().y) / ray.direction().y;
        return new Vector3f(ray.origin()).add(new Vector3f(ray.direction()).mul(t));
    }

    /**
     * Performs the release mouse operation.
     * @param gameMaster the game master value
     */
    private void releaseMouse(GameMaster gameMaster) {
        if (!mouseCaptured) return;
        glfwSetInputMode(gameMaster.getWindowHandle(), GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        mouseCaptured = false;
    }

    /**
     * Performs the release operation.
     * @param gameMaster the game master value
     */
    public void release(GameMaster gameMaster) {
        releaseMouse(gameMaster);
    }
}
