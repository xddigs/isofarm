package com.isofarm.entity.plyr;

import com.isofarm.entity.Player;
import com.isofarm.data.PlayerState;
import com.isofarm.entity.states.GroundedState;
import com.isofarm.entity.states.SneakingState;
import com.isofarm.input.ControlAction;
import com.isofarm.input.Controls;
import com.isofarm.pathfinding.GridPos;
import com.isofarm.service.BookService;
import com.isofarm.wrld.GameMaster;
import com.isofarm.wrld.World;
import org.joml.Vector3f;

import java.util.LinkedList;
import java.util.List;

import static org.joml.Math.lerp;

/** Manages player state, input-driven movement, paths and edge-safe sneaking. */
public final class PlayerManager {
    private static final float ZERO = 0.0f;
    private static final float EYE_HEIGHT = 1.6f;
    private static final float EYE_LERP_SPEED = 10.0f;
    private static final float STEP_HEIGHT = 1.05f;
    private static final float PATH_DISTANCE_SQUARED = 0.01f;
    private static final float WAYPOINT_OFFSET = 0.5f;

    private List<GridPos> path = new LinkedList<>();
    private int pathIndex;
    private float currentEyeHeight = EYE_HEIGHT;
    private float targetEyeHeight = EYE_HEIGHT;
    private boolean falling;
    private PlayerState currentState;

    /** Creates the shared player's movement manager. */
    public PlayerManager() {}

    /** Installs the initial grounded state. */
    public void initialize() {
        currentState = new GroundedState();
        currentState.enter();
    }

    /** @param delta frame time in seconds */
    public void update(float delta) {
        currentState.input(GameMaster.game);
        currentState.update(delta);
        currentEyeHeight = lerp(currentEyeHeight, targetEyeHeight,
                Math.clamp(delta * EYE_LERP_SPEED, ZERO, 1.0f));
        if (!(currentState instanceof SneakingState)) {
            autoJump(Player.plyr.getVelocity(), delta);
        }
    }

    /** @param newState state to enter */
    public void changeState(PlayerState newState) {
        if (currentState != null) currentState.exit();
        currentState = newState;
        currentState.enter();
    }

    /** @param velocity intended velocity @param delta frame time */
    public void autoJump(Vector3f velocity, float delta) {
        Player player = Player.plyr;
        World world = World.wrld;
        if (!player.isOnGround() || (velocity.x == ZERO && velocity.z == ZERO)) return;
        Vector3f original = new Vector3f(player.getPosition());
        player.getPosition().add(velocity.x * delta, ZERO, velocity.z * delta);
        boolean blocked = player.checkCollision(world);
        player.setPosition(original);
        if (blocked) {
            player.getPosition().add(velocity.x * delta, STEP_HEIGHT, velocity.z * delta);
            boolean clear = !player.checkCollision(world);
            player.setPosition(original);
            if (clear) player.jump();
        }
    }

    /** @param delta frame time */
    public void move(float delta) {
        Player player = Player.plyr;
        World world = World.wrld;
        if (!isFollowingPath()) {
            player.setVelocity(new Vector3f(ZERO, player.getVelocity().y, ZERO));
        } else {
            GridPos target = path.get(pathIndex);
            float dx = target.x() + WAYPOINT_OFFSET - player.getPosition().x;
            float dz = target.z() + WAYPOINT_OFFSET - player.getPosition().z;
            if (dx * dx + dz * dz < PATH_DISTANCE_SQUARED) {
                pathIndex++;
                if (!isFollowingPath()) player.setVelocity(new Vector3f(ZERO, player.getVelocity().y, ZERO));
            } else {
                Vector3f direction = new Vector3f(dx, ZERO, dz);
                if (direction.lengthSquared() > ZERO) direction.normalize();
                Vector3f velocity = direction.mul(player.getSpeed());
                velocity.y = player.getVelocity().y;
                player.setVelocity(velocity);
            }
        }
        player.collide(world, player.getVelocity(), delta);
    }

    /** @param delta frame time @param cameraYaw camera yaw @param flying flight flag */
    public void wasd(float delta, float cameraYaw, boolean flying) {
        Player player = Player.plyr;
        World world = World.wrld;
        if (GameMaster.game.isChatOpen() || GameMaster.game.isInventoryOpen() || BookService.bs.isOpen()) return;
        if (isFollowingPath()) { move(delta); return; }
        float x = Controls.getAxis(ControlAction.MOVE_X);
        float z = Controls.getAxis(ControlAction.MOVE_Y);
        if (Controls.isDown(ControlAction.MOVE_FORWARD)) z--;
        if (Controls.isDown(ControlAction.MOVE_BACKWARD)) z++;
        if (Controls.isDown(ControlAction.MOVE_LEFT)) x--;
        if (Controls.isDown(ControlAction.MOVE_RIGHT)) x++;
        Vector3f input = new Vector3f(x, ZERO, z);
        if (input.lengthSquared() > ZERO) {
            input.normalize();
            float yaw = (float) Math.toRadians(cameraYaw);
            float sin = (float) Math.sin(yaw), cos = (float) Math.cos(yaw);
            Vector3f velocity = new Vector3f(
                    (input.x * cos - input.z * sin) * player.getSpeed(),
                    player.getVelocity().y,
                    (input.x * sin + input.z * cos) * player.getSpeed());
            if (!flying) player.collide(world, velocity, delta);
        } else if (!flying) {
            player.collide(world, new Vector3f(ZERO, player.getVelocity().y, ZERO), delta);
        }
    }

    /** @param delta frame time @param yaw camera yaw @param flying flight flag */
    public void fly(float delta, float yaw, boolean flying) {
        if (!Player.plyr.isOnGround()) wasd(delta, yaw, flying);
    }

    /** @return whether a solid block supports at least one player corner */
    public boolean hasGroundBelow(float testX, float testZ) {
        Player player = Player.plyr;
        World world = World.wrld;
        float epsilon = 0.001f;
        float halfWidth = player.getDimensions().x / 2.0f - epsilon;
        float halfDepth = player.getDimensions().z / 2.0f - epsilon;
        int y = (int) Math.floor(player.getPosition().y - 0.2f);
        for (float x : new float[]{testX - halfWidth, testX + halfWidth})
            for (float z : new float[]{testZ - halfDepth, testZ + halfDepth})
                if (world.isBlockSolid((int) Math.floor(x), y, (int) Math.floor(z))) return true;
        return false;
    }

    /** Restricts sneaking velocity just enough to retain support while allowing edge travel. */
    public void adjustVelocity(float delta) {
        Player player = Player.plyr;
        World world = World.wrld;
        if (!(currentState instanceof SneakingState) || delta <= ZERO) return;
        Vector3f position = player.getPosition(), velocity = player.getVelocity();
        if (!player.isOnGround() && !hasGroundBelow(position.x, position.z)) return;
        float x = velocity.x * delta, z = velocity.z * delta;
        float step = 0.05f;
        while (x != ZERO && !hasGroundBelow(position.x + x, position.z)) x = towardZero(x, step);
        while (z != ZERO && !hasGroundBelow(position.x, position.z + z)) z = towardZero(z, step);
        while (x != ZERO && z != ZERO && !hasGroundBelow(position.x + x, position.z + z)) {
            x = towardZero(x, step); z = towardZero(z, step);
        }
        velocity.x = x / delta; velocity.z = z / delta;
    }

    private static float towardZero(float value, float amount) {
        return Math.abs(value) <= amount ? ZERO : value - Math.copySign(amount, value);
    }

    /** @return active state */ public PlayerState getCurrentState() { return currentState; }
    /** @param state state to store */ public void setCurrentState(PlayerState state) { currentState = state; }
    /** @return interpolated eye height */ public float getCurrentEyeHeight() { return currentEyeHeight; }
    /** @return desired eye height */ public float getTargetEyeHeight() { return targetEyeHeight; }
    /** @param height desired eye height */ public void setTargetEyeHeight(float height) { targetEyeHeight = height; }
    /** @return whether falling */ public boolean isFalling() { return falling; }
    /** @param value falling flag */ public void setFalling(boolean value) { falling = value; }
    /** @return movement direction angle */ public float getForward() { return (float) Math.atan2(Player.plyr.getVelocity().z, Player.plyr.getVelocity().x); }
    /** @return whether a waypoint remains */ public boolean isFollowingPath() { return pathIndex < path.size(); }
    /** @return active path */ public List<GridPos> getPath() { return path; }
    /** @param value path to follow */ public void setPath(List<GridPos> value) { path = value != null ? value : List.of(); pathIndex = 0; }
    /** @return path index */ public int getPathIndex() { return pathIndex; }
    /** @param value path index */ public void setPathIndex(int value) { pathIndex = Math.max(0, value); }
    /** Clears the active path. */ public void clearPath() { path = List.of(); pathIndex = 0; }
}
