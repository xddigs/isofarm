package com.isofarm.entity;

import com.isofarm.data.*;
import com.isofarm.entity.states.GroundedState;
import com.isofarm.entity.states.SneakingState;
import com.isofarm.graphics.*;
import com.isofarm.graphics.gltf.GLTFModel;
import com.isofarm.graphics.gltf.GLTFNode;
import com.isofarm.input.Keyboard;
import com.isofarm.item.Backpack;
import com.isofarm.item.CraftingBook;
import com.isofarm.item.Item;
import com.isofarm.item.Tool;
import com.isofarm.pathfinding.GridPos;
import com.isofarm.service.BookService;
import com.isofarm.service.TimeService;
import com.isofarm.utils.Settings;
import com.isofarm.utils.ToastFactory;
import com.isofarm.wrld.GameMaster;
import com.isofarm.wrld.World;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL13.*;

@DataClass
public class Player extends Character {
    private static final Logger log = LoggerFactory.getLogger(Player.class);
    private final Matrix4f modelMatrix;
    private final GameMaster gameMaster;
    private final GLTFModel playerModel;
    private Direction direction = Direction.S;
    private List<GridPos> path;
    private float currentEyeHeight = 1.6f;
    private float targetEyeHeight = 1.6f;
    private int pathIndex = 0;
    private int damageSequence = 0;
    private float respawnTimer = -1.0f;
    private boolean isFalling = false;
    private float modelYaw = 0.0f;
    private float targetModelYaw = 0.0f;
    private PlayerState currentState;
    private GLTFNode rightArmNode;
    private GLTFNode leftArmNode;
    private GLTFNode rightLegNode;
    private GLTFNode leftLegNode;
    private float walkAnimTime = 0.0f;

    public Player(String name, World world, GameMaster gameMaster) {
        super(name);
        this.gameMaster = gameMaster;
        this.modelMatrix = new Matrix4f();
        this.path = new LinkedList<>();

        this.playerModel = ResourceManager.getPlayerModel();
        if (this.playerModel != null) {
            this.rightArmNode = playerModel.findNode("Right Arm");
            this.leftArmNode = playerModel.findNode("Left Arm");
            this.rightLegNode = playerModel.findNode("Right Leg");
            this.leftLegNode = playerModel.findNode("Left Leg");
        }

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
    public void update(BlockPos blockPos, float delta) {
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
        updateRotation(delta);

        boolean isMoving = Math.abs(velocity.x) > 0.05f || Math.abs(velocity.z) > 0.05f;

        if (isMoving) {
            walkAnimTime += delta * 8.0f;
        } else {
            walkAnimTime = org.joml.Math.lerp(walkAnimTime, 0.0f, Math.clamp(delta * 10.0f, 0.0f, 1.0f));
        }

        float swingAngle = (float) Math.sin(walkAnimTime) * 0.45f;

        Quaternionf rightArmRot = new Quaternionf().rotateX(swingAngle);
        Quaternionf leftArmRot = new Quaternionf().rotateX(-swingAngle);
        Quaternionf rightLegRot = new Quaternionf().rotateX(-swingAngle);
        Quaternionf leftLegRot = new Quaternionf().rotateX(swingAngle);

        if (rightArmNode != null) rightArmNode.setRotation(rightArmRot);
        if (leftArmNode != null) leftArmNode.setRotation(leftArmRot);
        if (rightLegNode != null) rightLegNode.setRotation(rightLegRot);
        if (leftLegNode != null) leftLegNode.setRotation(leftLegRot);

        if (playerModel != null) {
            playerModel.updateTransforms();
        }

        if (Math.abs(velocity.x) > 0.05f || Math.abs(velocity.z) > 0.05f) {
            float rawYaw = (float) Math.toDegrees(Math.atan2(velocity.x, velocity.z));
            targetModelYaw = (rawYaw + 180.0f) % 360.0f;
            if (targetModelYaw < 0) targetModelYaw += 360.0f;

            int sector = (int) Math.round(targetModelYaw / 45.0) % 8;
            this.direction = switch (sector) {
                case 0 -> Direction.N;
                case 1 -> Direction.NE;
                case 2 -> Direction.E;
                case 3 -> Direction.SE;
                case 4 -> Direction.S;
                case 5 -> Direction.SW;
                case 6 -> Direction.W;
                case 7 -> Direction.NW;
                default -> Direction.N;
            };
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

        if (playerModel == null) {
            return;
        }

        Shader shader = rm.getDefaultShader();
        if (shader == null) {
            return;
        }

        CelestialLighting lighting = gameMaster.getCelestialLighting();
        shader.bind();
        shader.setUniform("uProjection", camera.getProjectionMatrix());
        shader.setUniform("uView", camera.getViewMatrix());

        shader.setUniform("uLightIntensity", lighting.getIntensity());
        shader.setUniform("uLightDirection", lighting.getDirection());
        shader.setUniform("uAmbientIntensity", lighting.getAmbientIntensity());
        shader.setUniform("uSkyColor", TimeService.getSkyColor());
        shader.setUniform("uBaseColor", new Vector3f(1.0f));

        shader.setUniform("uIsSprite", false);
        shader.setUniform("uUseTexture", true);
        shader.setUniform("uUVBounds", new Vector4f(0.0f, 0.0f, 1.0f, 1.0f));
        shader.setUniform("uParticleAlpha", 1.0f);
        shader.setUniform("uIsMaskPass", false);
        shader.setUniform("uEnableShadows", false);
        shader.setUniform("uLightSpaceMatrix", new Matrix4f());

        float globalScale = Settings.getScaledEntity();

        modelMatrix.identity()
                .translate(position.x, position.y, position.z)
                .rotateY((float) Math.toRadians(modelYaw))
                .scale(globalScale);

        shader.setUniform("uModel", modelMatrix);
        glEnable(GL_DEPTH_TEST);
        glDepthMask(true);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        playerModel.render(shader, modelMatrix);

        glBindTexture(GL_TEXTURE_2D, 0);
        glDisable(GL_BLEND);
        shader.unbind();
    }

    @Override
    public void onDamageTaken(float amount) {
        damageSequence++;
        getSoundService().playEntitySound(SoundGroup.ENTITY);
    }

    @Override
    protected void dropLoot() {
        for (Item i : List.copyOf(getInventory()
                .getItems().keySet())) {
            if (i == null) continue;
            switch (i) {
                case Backpack ignored -> {
                    continue;
                }
                case CraftingBook ignored -> {
                    continue;
                }
                default -> {
                }
            }

            int amount = getInventory().getAmount(i);
            if (amount <= 0) continue;
            WorldItem item = new WorldItem(i, amount, new Vector3f(position.x,
                    position.y, position.z));
            remove(i, amount);
            gameMaster.addEntity(item);
        }
    }

    private float getDirectionYaw(Direction direction) {
        return switch (direction) {
            case N  -> 0.0f;
            case NE -> 45.0f;
            case E  -> 90.0f;
            case SE -> 135.0f;
            case S  -> 180.0f;
            case SW -> 225.0f;
            case W  -> 270.0f;
            case NW -> 315.0f;
        };
    }

    private void updateRotation(float delta) {
        float difference = targetModelYaw - modelYaw;

        while (difference > 180.0f) {
            difference -= 360.0f;
        }

        while (difference < -180.0f) {
            difference += 360.0f;
        }

        float rotationSpeed = 360.0f;
        float maxRotation = rotationSpeed * delta;
        if (Math.abs(difference) <= maxRotation) {
            modelYaw = targetModelYaw;
        } else {
            modelYaw += Math.copySign(maxRotation, difference);
        }

        modelYaw %= 360.0f;
        if (modelYaw < 0.0f) {
            modelYaw += 360.0f;
        }
    }

    private int getPlayerRow(Direction direction, boolean isMoving) {
        int baseOffset = isMoving ? 8 : 0;
        return baseOffset + switch (direction) {
            case NW -> 0;
            case W -> 1;
            case SW -> 2;
            case S -> 3;
            case SE -> 4;
            case E -> 5;
            case NE -> 6;
            case N -> 7;
        };
    }

    private boolean isBackpackInFront(Direction direction) {
        return switch (direction) {
            case N, NE, NW, W, E -> true;
            default -> false;
        };
    }

    private int getBackpackFrame(Direction direction, boolean isMoving) {
        return switch (direction) {
            case NW -> 0;
            case W -> 1;
            case SW -> 2;
            case S -> 3;
            case NE -> 4;
            case E -> 5;
            case SE -> 6;
            case N -> 7;
        };
    }

    private int getActionOffset() {
        return switch (direction) {
            case NW -> 0;
            case W -> 1;
            case SW -> 2;
            case S -> 3;
            case SE -> 4;
            case E -> 5;
            case NE -> 6;
            case N -> 7;
        };
    }

    private void renderBackpack(Shader shader, ResourceManager rm, Direction direction,
                                boolean backpackInFront, boolean isMoving) {

        SpriteSheet bpSheet = ResourceManager.getBackpackSpriteSheet();
        if (bpSheet == null) return;

        int backpackFrame = getBackpackFrame(direction, isMoving);
        Vector4f uv = bpSheet.getUVBounds(backpackFrame);

        bpSheet.bind();
        shader.setUniform("uUVBounds", uv);

        glEnable(GL_POLYGON_OFFSET_FILL);
        if (backpackInFront) {
            glPolygonOffset(-1.0f, -1.0f);
        } else {
            glPolygonOffset(1.0f, 1.0f);
        }

        rm.getPlayerMesh().render();
        glDisable(GL_POLYGON_OFFSET_FILL);
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
        gameMaster.addEntity(this);
        setPosition(new Vector3f(spawnX, highestAltitude.y() + 1.0f, spawnZ));
        setVelocity(new Vector3f(0.0f, 0.0f, 0.0f));
        setDimensions(new Vector3f(1.0f, 2.0f, 1.0f));
        setSpeed(6.0f);

        setReputation(Reputation.NEUTRAL);
        setGamemode(Gamemode.SURVIVAL);

        setIsOffGroundTimer(0.0f);
        setWasOnGround(false);

        setMaxHitpoints(20);
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

                Vector3f velocity = new Vector3f(direction).mul(getSpeed());
                velocity.y = getVelocity().y;
                setVelocity(velocity);
            }
        }

        collide(world, getVelocity(), delta);
    }

    public void wasd(World world, float delta, float cameraYaw) {
        if (gameMaster.isChatOpen() || gameMaster.isInventoryOpen() ||
                BookService.bs.isOpen()) return;
        if (isFollowingPath()) {
            move(world, delta, cameraYaw);
            return;
        }

        float moveX = 0.0f;
        float moveZ = 0.0f;

        if (Keyboard.isKeyDown(GLFW_KEY_W)) moveZ -= 1.0f;
        if (Keyboard.isKeyDown(GLFW_KEY_S)) moveZ += 1.0f;
        if (Keyboard.isKeyDown(GLFW_KEY_A)) moveX -= 1.0f;
        if (Keyboard.isKeyDown(GLFW_KEY_D)) moveX += 1.0f;

        Vector3f inputDir = new Vector3f(moveX, 0.0f, moveZ);

        if (inputDir.lengthSquared() > 0.0f) {
            inputDir.normalize();
            float yawRad = (float) Math.toRadians(cameraYaw);
            float sin = (float) Math.sin(yawRad);
            float cos = (float) Math.cos(yawRad);
            float worldX = inputDir.x * cos - inputDir.z * sin;
            float worldZ = inputDir.x * sin + inputDir.z * cos;
            Vector3f targetVelocity = new Vector3f(worldX * getSpeed(), getVelocity().y, worldZ * getSpeed());

            if (currentState instanceof SneakingState) {
                Vector3f nextPosition = new Vector3f(position).add(targetVelocity.x * delta,
                        0.0f, targetVelocity.z * delta);

                int blockX = (int) Math.floor(nextPosition.x);
                int blockZ = (int) Math.floor(nextPosition.z);
                int blockY = (int) Math.floor(position.y - 0.05f);

                if (world.getBlockTypeAt(blockX, blockY, blockZ) == BlockData.AIR.getId()) {
                    targetVelocity.x = 0.0f;
                    targetVelocity.z = 0.0f;
                }
            }

            collide(world, targetVelocity, delta);
        } else {
            Vector3f targetVelocity = new Vector3f(0.0f, getVelocity().y, 0.0f);
            collide(world, targetVelocity, delta);
        }
    }

    private void setUpInventory() {
        switch (getGamemode()) {
            case SURVIVAL -> {
                Kit kit = new StartingKit(this);
                for (Item item : kit.getItems()) {
                    add(item);
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
}