package com.isofarm.input;

import com.isofarm.data.Hit;
import com.isofarm.entity.Player;
import com.isofarm.entity.states.SwimmingState;
import com.isofarm.graphics.Camera;
import com.isofarm.pathfinding.GridPos;
import com.isofarm.pathfinding.PathFinder;
import com.isofarm.service.Service;
import com.isofarm.wrld.GameMaster;
import com.isofarm.wrld.World;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

public record CameraController(Camera camera)
        implements Service<Camera> {
    private static final float NORMAL_ZOOM = 18.0f;
    private static final float ZOOMED_ZOOM = NORMAL_ZOOM / 2.5f;
    private static final float VERTICAL_OFFSET = 0.0f;
    private static final float DISTANCE = 500.0f;
    private static boolean mouseCaptured = false;
    private static GridPos lastGoal = null;

    public void update(GameMaster gameMaster, float delta) {
        if (gameMaster.isInventoryOpen() || gameMaster.isChatOpen()) {
            lastGoal = null;
            return;
        }

        Player player = gameMaster.getPlayer();
        if (player == null) return;

        if (player.getCurrentState() instanceof SwimmingState) {
            if (Keyboard.isKeyDown(GLFW_KEY_SPACE)) {
                player.getVelocity().y = 4.0f;
            } else if (Keyboard.isKeyDown(GLFW_KEY_LEFT_CONTROL)) {
                player.getVelocity().y = -3.0f;
            }
        }

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
        boolean isRightClickDown = Mouse.isButtonDown(GLFW_MOUSE_BUTTON_RIGHT);

        if (!isRightClickDown) {
            lastGoal = null;
            return;
        }

        float mouseX = Mouse.getX();
        float mouseY = Mouse.getY();
        float screenWidth = gameMaster.getWindowWidth();
        float screenHeight = gameMaster.getWindowHeight();

        Hit hit = camera.highlight(world, player.getPosition(), mouseX, mouseY,
                screenWidth, screenHeight);

        if (hit == null) return;
        GridPos start = PathFinder.getPlayerGridPosition(player);
        GridPos goal = getGoalPosition(world, hit);
        if (goal == null) return;
        if (start.equals(goal)) return;
        if (goal.equals(lastGoal)) return;
        var path = PathFinder.findPath(world, player, start, goal);
        if (path.isEmpty()) return;
        player.setPath(path);
        lastGoal = goal;
    }

    private void followPath(Player player, World world, float delta) {
        player.move(world, delta, camera.getYaw());
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