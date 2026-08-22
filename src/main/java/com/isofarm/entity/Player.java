package com.isofarm.entity;

import com.isofarm.data.*;
import com.isofarm.graphics.CameraView;
import com.isofarm.graphics.ResourceManager;
import com.isofarm.graphics.Shader;
import com.isofarm.graphics.SpriteSheet;
import com.isofarm.pathfinding.GridPos;
import com.isofarm.service.SoundService;
import com.isofarm.service.ToastService;
import com.isofarm.utils.K;
import com.isofarm.utils.Settings;
import com.isofarm.wrld.GameMaster;
import com.isofarm.wrld.World;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

import static org.lwjgl.opengl.GL13.*;

@DataClass
public class Player extends Character {
    private static final Logger log = LoggerFactory.getLogger(Player.class);
    private final String name;
    private final ToastService toastService;
    private final SoundService soundService;
    private final Matrix4f modelMatrix;

    private Direction direction = Direction.SOUTH_WEST;
    private List<GridPos> path;
    private int pathIndex = 0;

    public Player(String name, World world, ToastService toastService,
                  SoundService soundService) {
        super(name, toastService);
        this.name = name;
        this.toastService = toastService;
        this.soundService = soundService;
        this.modelMatrix = new Matrix4f();
        this.path = new LinkedList<>();

        float spawnX = 0.5f;
        float spawnZ = 0.5f;
        GridPos highestAltitude = world.getHighestY(spawnX, spawnZ);
        setPosition(new Vector3f(spawnX, highestAltitude.y(), spawnZ));
        setVelocity(new Vector3f(0.0f, 0.0f, 0.0f));
        setDimensions(new Vector3f(0.5f, 1.0f, 0.5f));
        setReputation(Reputation.NEUTRAL);
        setGamemode(Gamemode.SURVIVAL);
        setUpInventory();
    }

    @Override
    public void update(Hit hit, float delta) {
        updateCrouching(delta);
        heal((0.5f + getLevel()) * delta);

        if (!isOnGround()) {
            setIsOffGroundTimer(getIsOffGroundTimer() + delta);
        }

        if (isOnGround() && !wasOnGround()) {
            float fallVelocity = Math.abs(getVelocity().y);
            if (fallVelocity > 4.0f) {
                float damage = (fallVelocity - 8.0f) * 5.0f;
                fallDamage(damage);
                soundService.playUseSound(SoundGroup.ENTITY, 1.0f,
                        Settings.getMaxInteractionDistance());
            }
            setIsOffGroundTimer(0.0f);
        }

        setWasOnGround(isOnGround());
        for (InventorySlot slot : getInventory().getSlots()) {
            if (slot.getItem() instanceof Tool tool && tool.getDurability() <= 0) {
                remove(tool);
                toastService.error("Your " + tool.getName() + " broke!");
                soundService.playBreakSound(SoundGroup.ITEMS, 1.0f,
                        Settings.getMaxInteractionDistance());
            }
        }
    }

    @Override
    public void render(GameMaster gameMaster) {
        ResourceManager rm = gameMaster.getResourceManager();
        CameraView camera = gameMaster.getActiveCamera();
        SpriteSheet sheet = rm.getPlayerSpriteSheet();

        if (sheet == null || rm.getPlayerMesh() == null) return;

        Shader shader = rm.getDefaultShader();
        shader.bind();

        glActiveTexture(GL_TEXTURE0);
        sheet.bind();
        shader.setUniform("uTexture", 0);
        shader.setUniform("uUseTexture", true);
        shader.setUniform("uUseFaceAtlas", false);

        int totalFrames = Math.max(1, sheet.getTotalFrames());
        int frameIndex = direction != null ? direction.frame() : 0;
        shader.setUniform("uTotalFrames", totalFrames);
        shader.setUniform("uFrameIndex", frameIndex);
        shader.setUniform("uAtlasScale", new Vector2f(1.0f, 1.0f));
        shader.setUniform("uAtlasOffset", new Vector2f(0.0f, 0.0f));
        shader.setUniform("uUVBounds", new org.joml.Vector4f(0.0f, 0.0f, 1.0f, 1.0f));

        shader.setUniform("uSunColor", new Vector3f(1.0f, 1.0f, 1.0f));
        shader.setUniform("uLightIntensity", 0.0f);
        shader.setUniform("uLightDirection", new Vector3f(0.0f, -1.0f, 0.0f));
        shader.setUniform("uAmbientIntensity", 1.0f);

        shader.setUniform("uSkyColor", new Vector3f(1.0f));
        shader.setUniform("uParticleAlpha", 1.0f);
        shader.setUniform("uIsMaskPass", false);
        shader.setUniform("uEnableShadows", false);

        shader.setUniform("uLightSpaceMatrix", new Matrix4f());
        shader.setUniform("uProjection", camera.getProjectionMatrix());
        shader.setUniform("uView", camera.getViewMatrix());

        float aspect = (sheet.getFrameWidth() > 0 && sheet.getFrameHeight() > 0)
                ? (float) sheet.getFrameWidth() / (float) sheet.getFrameHeight()
                : 1.0f;

        float baseScaleY = (dimensions == null || dimensions.y <= 0) ? 1.0f : dimensions.y;
        float baseScaleX = baseScaleY * aspect;
        float baseScaleZ = (dimensions == null || dimensions.z <= 0) ? 1.0f : dimensions.z;

        float yawRad = (float) Math.toRadians(-camera.getYaw());
        modelMatrix.identity()
                .translate(position)
                .rotateY(yawRad)
                .scale(baseScaleX, baseScaleY, baseScaleZ);

        shader.setUniform("uModel", modelMatrix);

        glDisable(GL_CULL_FACE);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_DEPTH_TEST);
        glDepthMask(true);

        rm.getPlayerMesh().render();
        sheet.unbind();
    }

    public void move(World world, Vector3f direction, float delta) {
        moveAndCollide(world, direction, delta);
    }

    public void move(World world, float delta, float cameraYaw) {
        if (!isFollowingPath()) {
            setVelocity(new Vector3f(0.0f, 0.0f, 0.0f));
            return;
        }

        GridPos target = path.get(pathIndex);

        float targetX = target.x() + 0.5f;
        float targetY = target.y();
        float targetZ = target.z() + 0.5f;

        Vector3f position = getPosition();

        float dx = targetX - position.x;
        float dz = targetZ - position.z;

        float distanceSquared = dx * dx + dz * dz;

        if (distanceSquared < 0.01f) {
            position.x = targetX;
            position.z = targetZ;
            position.y = targetY;

            pathIndex++;

            if (!isFollowingPath()) {
                setVelocity(new Vector3f(0.0f, 0.0f, 0.0f));
            }

            return;
        }

        Vector3f direction = new Vector3f(dx, 0.0f, dz);

        if (direction.lengthSquared() > 0.0f) {
            direction.normalize();
        }

        Vector3f velocity = new Vector3f(direction)
                .mul(K.Camera.MOVEMENT_SPEED);

        setVelocity(velocity);
        lookAt(targetX, targetZ, cameraYaw);
        move(world, velocity, delta);
        position.y = targetY;
    }

    public void lookAt(float targetX, float targetZ, float cameraYaw) {
        float dx = targetX - position.x;
        float dz = targetZ - position.z;

        if (dx * dx + dz * dz < 0.09f) {
            return;
        }
        this.direction = Direction.fromVector(
                dx, dz, cameraYaw);
    }

    public String getName() {
        return name;
    }

    private void setUpInventory() {
        switch (getGamemode()) {
            case SURVIVAL -> {
                add(new Seed(), 4);
                add(new Seed(CropType.CARROT), 4);
                add(new Hoe(), 1);
                add(new Pickaxe(), 1);
            }
            case GODMODE -> {
                BlockData[] blocks = BlockData.values();
                for (BlockData b : blocks) { add(new Block(b)); }

                CropType[] crops = CropType.values();
                for (CropType c : crops) { add(new Seed(c)); }
                sort();
            }
        }
    }

    public void sell(Item item, int amount) {
        if (item == null || amount <= 0) return;

        int current = getInventory().getAmount(item);
        if (current <= 0) {
            log.warn("No {} in inventory to sell", item.getName());
            return;
        }

        int toSell = Math.min(current, amount);
        getInventory().remove(item, toSell);
        int earnings = toSell * item.getValue();
        toastService.sell("You successfully sold " + item.getName() + " for " + earnings + " coins");
        earn(earnings);
    }

    public void add(Item item, int amount) {
        getInventory().add(item, amount);
        log.info("Added x{} of {} to inventory", amount, item.getName());
    }

    public void add(Item item) {
        getInventory().add(item, 1);
        log.info("Added x1 of {} to inventory", item.getName());
    }

    public void sort() {
        getInventory().sort();
    }

    public void remove(Item item, int amount) {
        if (getGamemode().isGodmode()) return;
        getInventory().remove(item, amount);
        log.info("Removed x{} of {} to inventory", amount, item.getName());
    }

    public void remove(Item item) {
        if (getGamemode().isGodmode()) return;
        getInventory().remove(item, 1);
        log.info("Removed x1 of {} from inventory", item.getName());
    }

    public void clear() {
        getInventory().clear();
        log.info("Cleared inventory");
    }

    public boolean isEmpty() {
        return getInventory().isEmpty();
    }

    public int size() {
        return getInventory().size();
    }

    public Item get(int index) {
        return getInventory().get(index);
    }

    public Item get(Item item) {
        return getInventory().get(item);
    }

    public int getAmount(Item item) {
        return getInventory().getAmount(item);
    }

    public void earn(int amount) {
        log.info("Earned ${}", amount);
        getPurse().add(amount);
    }

    public void spend(int amount) {
        if (amount <= 0) return;
        log.info("Spent ${}", amount);
        getPurse().remove(amount);
    }

    public boolean hasSeeds() {
        return getInventory().hasItemOfType(Seed.class);
    }

    public Vector3f getEyePosition() {
        return new Vector3f(position.x, position.y + getCurrentEyeHeight(), position.z);
    }

    public float getForward() {
        return (float) Math.atan2(velocity.z, velocity.x);
    }

    public Direction getDirection() {
        return direction;
    }

    public boolean isFollowingPath() {
        return pathIndex < path.size();
    }

    public List<GridPos> getPath() {
        return path;
    }

    public void setPath(List<GridPos> path) {
        this.path = path != null ? path : List.of();
        this.pathIndex = 0;
    }

    public int getPathIndex() {
        return pathIndex;
    }

    public void setPathIndex(int pathIndex) {
        this.pathIndex = Math.max(0, pathIndex);
    }

    public void clearPath() {
        this.path = List.of();
        this.pathIndex = 0;
    }
}