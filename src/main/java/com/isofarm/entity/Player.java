package com.isofarm.entity;

import com.isofarm.data.*;
import com.isofarm.entity.states.CrouchingState;
import com.isofarm.entity.states.GroundedState;
import com.isofarm.graphics.CameraView;
import com.isofarm.graphics.ResourceManager;
import com.isofarm.graphics.Shader;
import com.isofarm.graphics.SpriteSheet;
import com.isofarm.item.Backpack;
import com.isofarm.item.CraftingKit;
import com.isofarm.item.Item;
import com.isofarm.item.Tool;
import com.isofarm.pathfinding.GridPos;
import com.isofarm.utils.K;
import com.isofarm.utils.Settings;
import com.isofarm.utils.ToastFactory;
import com.isofarm.wrld.GameMaster;
import com.isofarm.wrld.World;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

import static org.lwjgl.opengl.GL13.*;

@DataClass
public class Player extends Character {
    private static final Logger log = LoggerFactory.getLogger(Player.class);
    private final Matrix4f modelMatrix;
    private final GameMaster gameMaster;
    private Direction direction = Direction.SOUTH;
    private List<GridPos> path;
    private int currentFrame;

    private float currentEyeHeight = 1.6f;
    private float targetEyeHeight = 1.6f;

    private int pathIndex = 0;
    private int damageSequence = 0;
    private float lastDamageAmount = 0.0f;
    private float respawnTimer = -1.0f;
    private boolean isFalling = false;

    private PlayerState currentState;

    public Player(String name, World world, GameMaster gameMaster) {
        super(name);
        this.gameMaster = gameMaster;
        this.modelMatrix = new Matrix4f();
        this.path = new LinkedList<>();

        float spawnX = 0.5f;
        float spawnZ = 0.5f;
        GridPos highestAltitude = world.getHighestY(spawnX, spawnZ);
        setPosition(new Vector3f(spawnX, highestAltitude.y(), spawnZ));
        setVelocity(new Vector3f(0.0f, 0.0f, 0.0f));
        setDimensions(new Vector3f(1.0f, 2.0f, 1.0f));
        setMaxHitpoints(20);
        setHitpoints(getMaxHitpoints());
        setMaxStamina(100);
        setStamina(getMaxStamina());
        setSpeed(6.0f);
        setReputation(Reputation.NEUTRAL);
        setGamemode(Gamemode.SURVIVAL);
        setUpInventory();

        this.currentState = new GroundedState();
        this.currentState.enter(this);
    }

    @Override
    public void update(Hit hit, float delta) {
        if (!this.isAlive()) {
            if (respawnTimer <= 0.0f) {
                respawnTimer = 5.0f;
                dropLoot();
                gameMaster.toggleHUD();
                setGamemode(Gamemode.NO_CLIP);
            }

            respawnTimer -= delta;
            if (respawnTimer <= 0.0f) {
                respawn();
            }

            return;
        }

        currentState.input(this, gameMaster);
        currentState.update(this, delta);

        if (velocity.x != 0.0f || velocity.z != 0.0f) {
            float threshold = 0.3f;
            boolean right = velocity.x > threshold;
            boolean left = velocity.x < -threshold;
            boolean down = velocity.z > threshold;
            boolean up = velocity.z < -threshold;

            if (up && right) {
                direction = Direction.NORTH_EAST;
            } else if (up && left) {
                direction = Direction.NORTH_WEST;
            } else if (down && right) {
                direction = Direction.SOUTH_EAST;
            } else if (down && left) {
                direction = Direction.SOUTH_WEST;
            } else if (right) {
                direction = Direction.EAST;
            } else if (left) {
                direction = Direction.WEST;
            } else if (down) {
                direction = Direction.SOUTH;
            } else if (up) {
                direction = Direction.NORTH;
            }
        }

        float lerpSpeed = 10.0f;
        currentEyeHeight = org.joml.Math.lerp(currentEyeHeight, targetEyeHeight,
                Math.clamp(delta * lerpSpeed, 0.0f, 1.0f));

        setAnimTimer(getAnimTimer() + delta);
        heal(((0.5f + getLevel()) * delta) / getDifficultyRegen());
        autoJump(gameMaster.getWorld(), getVelocity(), delta);
        checkDurability();
    }

    @Override
    public void render(GameMaster gameMaster) {
        ResourceManager rm = gameMaster.getResourceManager();
        CameraView camera = gameMaster.getActiveCamera();
        SpriteSheet sheet = ResourceManager.getPlayerSpriteSheet();

        if (sheet == null || rm.getPlayerMesh() == null) {
            return;
        }

        Shader shader = rm.getDefaultShader();
        int textureUnit = K.Render.PRIMARY_TEXTURE_UNIT;
        boolean isMoving = (getVelocity().x * getVelocity().x +
                getVelocity().z * getVelocity().z) > 0.001f;

        float moveAngle = 0.0f;
        boolean hasVelocity = (getVelocity().x * getVelocity().x + getVelocity().z * getVelocity().z) > 0.001f;

        if (hasVelocity) {
            moveAngle = (float) Math.toDegrees(Math.atan2(getVelocity().z, getVelocity().x));
        } else {
            moveAngle = switch (this.direction) {
                case EAST -> 0.0f;
                case SOUTH_EAST -> 45.0f;
                case SOUTH -> 90.0f;
                case SOUTH_WEST -> 135.0f;
                case WEST -> 180.0f;
                case NORTH_WEST -> -135.0f;
                case NORTH -> -90.0f;
                case NORTH_EAST -> -45.0f;
            };
        }

        float relativeAngle = moveAngle - camera.getYaw();
        relativeAngle = (relativeAngle % 360.0f + 360.0f) % 360.0f;

        int directionOffset;
        if (relativeAngle >= 225.0f && relativeAngle < 315.0f) {
            directionOffset = 0;
        } else if (relativeAngle >= 135.0f && relativeAngle < 225.0f) {
            directionOffset = 1;
        } else if (relativeAngle >= 45.0f && relativeAngle < 135.0f) {
            directionOffset = 2;
        } else {
            directionOffset = 3;
        }

        int rowIndex;
        int totalFramesInRow;
        int startSpriteIndex;

        if (isMoving) {
            rowIndex = 4 + directionOffset;
            totalFramesInRow = K.UI.PLAYER_SPRITE_COLS_RUN;
        } else {
            rowIndex = directionOffset;
            totalFramesInRow = K.UI.PLAYER_SPRITE_COLS;
        }

        currentFrame = (int) (getAnimTimer() / getFrameDuration()) % totalFramesInRow;
        int spriteIndex = (rowIndex * K.UI.PLAYER_SPRITE_COLS) + currentFrame;

        Vector4f uvBounds = sheet.getUVBounds(spriteIndex);
        shader.bind();

        glActiveTexture(GL_TEXTURE0 + textureUnit);
        sheet.bind();

        shader.setUniform("uTexture", textureUnit);
        shader.setUniform("uUseTexture", true);
        shader.setUniform("uUseFaceAtlas", false);

        shader.setUniform("uUVBounds", uvBounds);
        shader.setUniform("uAtlasScale", new Vector2f(1.0f, 1.0f));
        shader.setUniform("uAtlasOffset", new Vector2f(0.0f, 0.0f));
        shader.setUniform("uTopAtlasOffset", new Vector2f(0.0f, 0.0f));
        shader.setUniform("uBottomAtlasOffset", new Vector2f(0.0f, 0.0f));
        shader.setUniform("uSideAtlasOffset", new Vector2f(0.0f, 0.0f));
        shader.setUniform("uSunColor", new Vector3f(1.0f));
        shader.setUniform("uLightIntensity", 0.0f);
        shader.setUniform("uLightDirection", new Vector3f(0.0f, -1.0f, 0.0f));
        shader.setUniform("uAmbientIntensity", 1.0f);
        shader.setUniform("uSkyColor", new Vector3f(1.0f));
        shader.setUniform("uBaseColor", new Vector3f(1.0f));
        shader.setUniform("uParticleAlpha", 1.0f);
        shader.setUniform("uIsMaskPass", false);
        shader.setUniform("uEnableShadows", false);
        shader.setUniform("uLightSpaceMatrix", new Matrix4f());
        shader.setUniform("uProjection", camera.getProjectionMatrix());
        shader.setUniform("uView", camera.getViewMatrix());

        float baseScaleY = (dimensions == null || dimensions.y <= 0) ? 2.0f : dimensions.y;
        float baseScaleX = (dimensions == null || dimensions.x <= 0) ? 1.0f : dimensions.x;
        float baseScaleZ = (dimensions == null || dimensions.z <= 0) ? 1.0f : dimensions.z;
        float globalScale = Settings.getScaledEntity();

        float finalScaleX = baseScaleX * globalScale;
        float finalScaleY = baseScaleY * globalScale;
        float finalScaleZ = baseScaleZ * globalScale;

        float yawRad = (float) Math.toRadians(-camera.getYaw());
        float yOffset = finalScaleY * -0.15f;

        modelMatrix.identity()
                .translate(position.x, position.y + yOffset, position.z)
                .rotateY(yawRad)
                .scale(finalScaleX, finalScaleY, finalScaleZ);

        shader.setUniform("uModel", modelMatrix);

        glDisable(GL_CULL_FACE);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_DEPTH_TEST);
        glDepthMask(true);

        rm.getPlayerMesh().render();

        if (getInventory().hasBackpackEquipped()) {
            int backpackRowOffset = 8;
            int backpackSpriteIndex = spriteIndex + (backpackRowOffset * K.UI.PLAYER_SPRITE_COLS);

            Vector4f backpackUVBounds = sheet.getUVBounds(backpackSpriteIndex);
            shader.setUniform("uUVBounds", backpackUVBounds);

            glEnable(GL_POLYGON_OFFSET_FILL);
            glPolygonOffset(-1.0f, -1.0f);
            rm.getPlayerMesh().render();
            glDisable(GL_POLYGON_OFFSET_FILL);
        }

        sheet.unbind();
        shader.unbind();
    }

    @Override
    public void onDamageTaken(float amount) {
        lastDamageAmount = amount;
        damageSequence++;
        getSoundService().playEntitySound(SoundGroup.ENTITY);
    }

    @Override
    protected void dropLoot() {
        for (Item i : List.copyOf(getInventory().getItems().keySet())) {
            if (i == null) continue;
            switch (i) {
                case Backpack ignored -> {
                    continue;
                }
                case CraftingKit ignored -> {
                    continue;
                }
                default -> {
                }
            }

            int amount = getInventory().getAmount(i);
            if (amount <= 0) continue;
            WorldItem item = new WorldItem(i, amount, new Vector3f(position.x, position.y, position.z));
            remove(i, amount);
            gameMaster.addEntity(item);
        }
    }

    public void changeState(PlayerState newState) {
        if (currentState != null) {
            currentState.exit(this);
        }
        currentState = newState;
        currentState.enter(this);
    }

    public void autoJump(World world, Vector3f velocity, float delta) {
        if (!isOnGround() || (velocity.x == 0.0f && velocity.z == 0.0f)) {
            return;
        }

        Vector3f originalPos = new Vector3f(getPosition());
        getPosition().x += velocity.x * delta;
        getPosition().z += velocity.z * delta;
        boolean isBlockedAtFeet = checkCollision(world);
        setPosition(originalPos);

        if (isBlockedAtFeet) {
            getPosition().y += 1.05f;
            getPosition().x += velocity.x * delta;
            getPosition().z += velocity.z * delta;

            boolean canClearStep = !checkCollision(world);
            setPosition(originalPos);

            if (canClearStep) {
                jump();
            }
        }
    }

    private void checkDurability() {
        for (InventorySlot slot : getInventory().getSlots()) {
            if (slot.getItem() instanceof Tool tool) {
                if (tool.getDurability() == 10) {
                    ToastFactory.warning("Your " + tool.getName() + " is about to break!");
                    return;
                }

                if (tool.getDurability() <= 0) {
                    remove(tool);
                    ToastFactory.error("Your " + tool.getName() + " broke!");
                    getSoundService().playBreakSound(SoundGroup.ITEMS, 1.0f,
                            Settings.getMaxInteractionDistance());
                }
            }
        }
    }

    public PlayerState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(PlayerState currentState) {
        this.currentState = currentState;
    }

    public void respawn() {
        if (!Settings.doKeepInventory()) {
            clear();
        }

        float spawnX = 0.5f;
        float spawnZ = 0.5f;

        GridPos highestAltitude = gameMaster.getWorld().getHighestY(spawnX, spawnZ);
        setPosition(new Vector3f(spawnX, highestAltitude.y() + 1.0f, spawnZ));
        setVelocity(new Vector3f(0.0f, 0.0f, 0.0f));
        setDimensions(new Vector3f(1.0f, 2.0f, 1.0f));
        setSpeed(6.0f);

        setReputation(Reputation.NEUTRAL);
        setGamemode(Gamemode.SURVIVAL);

        setIsOffGroundTimer(0.0f);
        setWasOnGround(false);

        setMaxHitpoints(8);
        setHitpoints(getMaxHitpoints());

        setMaxStamina(100);
        setStamina(getMaxStamina());

        setExperience(0);
        setLevel(1);
        resetAttributes();
        respawnTimer = -1.0f;
        gameMaster.toggleHUD();
    }

    public void resetAttributes() {
        setStrength(1);
        setDexterity(1);
        setConstitution(1);
        setIntelligence(1);
        setWisdom(1);
        setCharisma(1);
    }

    public int getDamageSequence() {
        return damageSequence;
    }

    public float getLastDamageAmount() {
        return lastDamageAmount;
    }

    public void move(World world, Vector3f direction, float delta) {
        collide(world, direction, delta);
    }

    public void move(World world, float delta, float cameraYaw) {
        if (!isFollowingPath()) {
            setVelocity(new Vector3f(0.0f, getVelocity().y, 0.0f));
        } else {
            GridPos target = path.get(pathIndex);
            float targetX = target.x() + 0.5f;
            float targetZ = target.z() + 0.5f;

            Vector3f position = getPosition();
            float dx = targetX - position.x;
            float dz = targetZ - position.z;

            float distanceSquared = dx * dx + dz * dz;

            if (distanceSquared < 0.01f) {
                pathIndex++;
                if (!isFollowingPath()) {
                    setVelocity(new Vector3f(0.0f, getVelocity().y, 0.0f));
                }
            } else {
                Vector3f direction = new Vector3f(dx, 0.0f, dz);
                if (direction.lengthSquared() > 0.0f) {
                    direction.normalize();
                }

                float speed = (currentState instanceof CrouchingState) ? getSpeed() / 3f : getSpeed();
                Vector3f velocity = new Vector3f(direction).mul(speed);
                velocity.y = getVelocity().y;
                setVelocity(velocity);
            }
        }

        collide(world, getVelocity(), delta);
    }

    private void setUpInventory() {
        switch (getGamemode()) {
            case SURVIVAL -> {
                Kit kit = new StartingKit();
                for (Item item : kit.getItems()) {
                    add(item, 1);
                }
            }
            case GODMODE -> {
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
        ToastFactory.sell("You successfully sold " + item.getName() + " for " + earnings + " coins");
        earn(earnings);
    }

    public void add(Item item, int amount) {
        getInventory().add(item, amount);
        log.info("Added x{} of {} to inventory", amount, item.getName());
    }

    public void add(Item item) {
        add(item, 1);
        log.info("Added x1 of {} to inventory", item.getName());
    }

    public void addToBackpack(Item item, int amount) {
        if (getBackpack().hasBackpackEquipped()) getBackpack().add(item, amount);
        log.info("Added x{} of {} to backpack", amount, item.getName());
    }

    public void addToBackpack(Item item) {
        addToBackpack(item, 1);
        log.info("Added x1 of {} to backpack", item.getName());
    }

    public void removeFromBackpack(Item item, int amount) {
        if (getBackpack().hasBackpackEquipped()) getBackpack().remove(item, amount);
        log.info("Removed x{} of {} from backpack", amount, item.getName());
    }

    public void removeFromBackpack(Item item) {
        removeFromBackpack(item, 1);
        log.info("Removed x1 of {} from backpack", item.getName());
    }

    public void sort() {
        getInventory().sort();
        getBackpack().sort();
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
        for (Item item : getInventory().getItems().keySet()) {
            if (item == null) continue;
            if (item instanceof Backpack) continue;
            remove(item);
        }
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

    public GameMaster getGameMaster() {
        return gameMaster;
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

    public boolean hasSpace() {
        return !getInventory().isFull() ||
                (getBackpack().hasBackpackEquipped()
                        && !getBackpack().isFull());
    }

    public boolean hasSeeds() {
        return getInventory().hasItemOfType(Seed.class);
    }

    public Vector3f getEyePosition() {
        return new Vector3f(position.x, position.y + getCurrentEyeHeight(), position.z);
    }

    @Override
    public float getCurrentEyeHeight() {
        return currentEyeHeight;
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

    public float getRespawnTimer() {
        return respawnTimer;
    }

    public void setRespawnTimer(float respawnTimer) {
        this.respawnTimer = respawnTimer;
    }

    public float getTargetEyeHeight() {
        return targetEyeHeight;
    }

    public void setTargetEyeHeight(float targetEyeHeight) {
        this.targetEyeHeight = targetEyeHeight;
    }

    public boolean isFalling() {
        return isFalling;
    }

    public void setFalling(boolean falling) {
        isFalling = falling;
    }

    public float getDifficultyRegen() {
        return gameMaster.getDifficulty().getMultiplier();
    }

    public int getCurrentFrame() {
        return currentFrame;
    }
}