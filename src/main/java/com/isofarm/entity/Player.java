package com.isofarm.entity;

import com.isofarm.data.*;
import com.isofarm.entity.states.GroundedState;
import com.isofarm.entity.states.SneakingState;
import com.isofarm.graphics.CameraView;
import com.isofarm.graphics.CelestialLighting;
import com.isofarm.graphics.ResourceManager;
import com.isofarm.graphics.Shader;
import com.isofarm.graphics.gltf.GLTFModel;
import com.isofarm.graphics.gltf.GLTFNode;
import com.isofarm.input.Keyboard;
import com.isofarm.item.*;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.joml.Math.lerp;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL13.*;

@DataClass
public class Player extends Character {
    private static final Logger log = LoggerFactory.getLogger(Player.class);
    private static final float PLAYER_WIDTH = 1.0f;
    private static final float PLAYER_HEIGHT = 2.0f;
    private static final float SPAWN_X = 0.5f;
    private static final float SPAWN_Z = 0.5f;
    private static final float INITIAL_EYE_HEIGHT = 1.6f;
    private static final float INITIAL_Y_VELOCITY = 0.0f;
    private static final float INITIAL_HORIZONTAL_VELOCITY = 0.0f;
    private static final int MAX_HITPOINTS = 20;
    private static final int MAX_STAMINA = 100;
    private static final float PLAYER_SPEED = 6.0f;
    private static final float MOVEMENT_THRESHOLD = 0.05f;
    private static final float SNEAK_OFFSET = 0.08f;
    private static final float SNEAK_TORSO_LEAN = 20.0f;
    private static final float SNEAK_ARM_BEND = 15.0f;
    private static final float SNEAK_STRIDE_AMPLITUDE = 0.25f;
    private static final float SNEAK_LEG_BACK_WEIGHT = 0.18f;
    private static final float NORMAL_WALK_SPEED = 10.0f;
    private static final float SNEAK_WALK_SPEED = 7.0f;
    private static final float BREATH_AMPLITUDE = 0.05f;
    private static final float BREATH_SWAY_AMPLITUDE = 0.02f;
    private static final float IDLE_BOBBING_AMPLITUDE = 0.025f;
    private static final float IDLE_SNEAK_FACTOR = 0.5f;
    private static final float IDLE_FADE_SPEED = 8.0f;
    private static final float IDLE_WEIGHT_SPEED = 5.0f;
    private static final float WALK_WEIGHT_SPEED = 10.0f;
    private static final float ROTATION_SPEED = 720.0f;
    private static final float ATTACK_ANIMATION_SPEED = 12.0f;
    private static final float ATTACK_ANGLE = 1.2f;
    private static final float COLLISION_STEP_HEIGHT = 1.05f;
    private static final float SNEAK_GROUND_OFFSET = 0.05f;
    private static final float PATH_REACHED_DISTANCE_SQUARED = 0.01f;
    private static final float ZERO_VELOCITY = 0.0f;
    private static final float DEATH_RESPAWN_TIME = 5.0f;
    private static final float RESPAWN_Y_OFFSET = 1.0f;
    private static final float EYE_HEIGHT_LERP_SPEED = 10.0f;
    private static final float SNEAK_WEIGHT_LERP_SPEED = 75.0f;
    private static final float ANIMATION_IDLE_BREATH_SPEED = 2.5f;
    private static final float STRIDE_AMPLITUDE = 0.45f;
    private static final float ANIMATION_SPEED_FACTOR = 10.0f;
    private final Matrix4f modelMatrix;
    private final GameMaster gameMaster;
    private final GLTFModel playerModel;
    private final Map<String, GLTFNode> equipmentNodes;
    private Direction direction = Direction.S;
    private List<GridPos> path;
    private float currentEyeHeight = INITIAL_EYE_HEIGHT;
    private float targetEyeHeight = INITIAL_EYE_HEIGHT;
    private int pathIndex = 0;
    private int damageSequence = 0;
    private float respawnTimer = -1.0f;
    private boolean isFalling = false;
    private float modelYaw = 0.0f;
    private float targetModelYaw = 0.0f;
    private PlayerState currentState;
    private GLTFNode headNode;
    private GLTFNode torsoNode;
    private GLTFNode backpackNode;
    private GLTFNode rightArmNode;
    private GLTFNode leftArmNode;
    private GLTFNode rightLegNode;
    private GLTFNode leftLegNode;
    private Vector3f headTranslation;
    private Vector3f torsoTranslation;
    private Vector3f backpackTranslation;
    private Vector3f rightArmTranslation;
    private Vector3f leftArmTranslation;
    private Vector3f rightLegTranslation;
    private Vector3f leftLegTranslation;

    private float idleAnimationTime = 0.0f;
    private float idleWeight = 0.0f;
    private float sneakWeight = 0.0f;
    private float walkAnimationTime = 0.0f;
    private float walkAnimSpeed = 0.0f;
    private float attackAnimationTime = 0.0f;
    private boolean isAttacking = false;

    public Player(String name, World world, GameMaster gameMaster) {
        super(name);
        this.gameMaster = gameMaster;
        this.modelMatrix = new Matrix4f();
        this.path = new LinkedList<>();
        this.equipmentNodes = new HashMap<>();

        this.playerModel = ResourceManager.getPlayerModel();
        if (this.playerModel != null) {
            this.headNode = playerModel.findNode("Head");
            this.torsoNode = playerModel.findNode("Body");
            this.backpackNode = playerModel.findNode("Backpack");
            this.rightArmNode = playerModel.findNode("Right Arm");
            this.leftArmNode = playerModel.findNode("Left Arm");
            this.rightLegNode = playerModel.findNode("Right Leg");
            this.leftLegNode = playerModel.findNode("Left Leg");

            registerNode("sword");
            registerNode("pickaxe");
            registerNode("axe");
            registerNode("hoe");
            registerNode("shovel");
        }

        if (headNode != null) {
            headTranslation = new Vector3f(headNode.getTranslation());
        }

        if (torsoNode != null) {
            torsoTranslation = new Vector3f(torsoNode.getTranslation());
        }

        String[] equipment = {"sword", "pickaxe", "axe", "hoe", "shovel"};
        for (String e : equipment) {
            hideEquipment(e);
        }

        if (backpackNode != null) {
            backpackTranslation = new Vector3f(backpackNode.getTranslation());
            backpackNode.setVisible(false);
        }

        if (rightArmNode != null) {
            rightArmTranslation = new Vector3f(rightArmNode.getTranslation());
        }

        if (leftArmNode != null) {
            leftArmTranslation = new Vector3f(leftArmNode.getTranslation());
        }

        if (rightLegNode != null) {
            rightLegTranslation = new Vector3f(rightLegNode.getTranslation());
        }

        if (leftLegNode != null) {
            leftLegTranslation = new Vector3f(leftLegNode.getTranslation());
        }

        float spawnX = SPAWN_X;
        float spawnZ = SPAWN_Z;
        GridPos highestAltitude = world.getHighestY(spawnX, spawnZ);
        setPosition(new Vector3f(spawnX, highestAltitude.y(), spawnZ));
        setVelocity(new Vector3f(INITIAL_HORIZONTAL_VELOCITY, INITIAL_Y_VELOCITY, INITIAL_HORIZONTAL_VELOCITY));
        setDimensions(new Vector3f(PLAYER_WIDTH, PLAYER_HEIGHT, PLAYER_WIDTH));
        setMaxHitpoints(MAX_HITPOINTS);
        setHitpoints(getMaxHitpoints());
        setMaxStamina(MAX_STAMINA);
        setStamina(getMaxStamina());
        setSpeed(PLAYER_SPEED);
        setReputation(Reputation.NEUTRAL);
        setGamemode(Gamemode.SURVIVAL);
        setUpInventory();
        this.currentState = new GroundedState();
        this.currentState.enter(this);
    }

    @Override
    public void update(BlockPos blockPos, float delta) {
        if (!this.isAlive()) {
            if (respawnTimer <= ZERO_VELOCITY) {
                respawnTimer = DEATH_RESPAWN_TIME;
                dropLoot();
                gameMaster.toggleHUD();
                setGamemode(Gamemode.NO_CLIP);
            }

            respawnTimer -= delta;
            if (respawnTimer <= ZERO_VELOCITY) {
                respawn();
            }

            return;
        }

        currentState.input(this, gameMaster);
        currentState.update(this, delta);
        updateRotation(delta);

        boolean hasBackpack = getInventory().hasBackpackEquipped();

        if (backpackNode != null) {
            backpackNode.setVisible(hasBackpack);
        }

        boolean isMoving = Math.abs(velocity.x) > MOVEMENT_THRESHOLD || Math.abs(velocity.z) > MOVEMENT_THRESHOLD;
        boolean isSneaking = currentState instanceof SneakingState;

        float targetSneakWeight = isSneaking ? 1.0f : ZERO_VELOCITY;
        sneakWeight = lerp(sneakWeight, targetSneakWeight, Math.clamp(delta * SNEAK_WEIGHT_LERP_SPEED, ZERO_VELOCITY, 1.0f));

        if (isMoving) {
            float speedFactor = isSneaking ? SNEAK_WALK_SPEED : NORMAL_WALK_SPEED;
            walkAnimationTime += delta * speedFactor;
            walkAnimSpeed = lerp(walkAnimSpeed, 1.0f, Math.clamp(delta * ANIMATION_SPEED_FACTOR, ZERO_VELOCITY, 1.0f));
            idleWeight = lerp(idleWeight, ZERO_VELOCITY, Math.clamp(delta * IDLE_FADE_SPEED, ZERO_VELOCITY, 1.0f));
        } else {
            walkAnimSpeed = lerp(walkAnimSpeed, ZERO_VELOCITY, Math.clamp(delta * WALK_WEIGHT_SPEED, ZERO_VELOCITY, 1.0f));
            idleWeight = lerp(idleWeight, 1.0f, Math.clamp(delta * IDLE_WEIGHT_SPEED, ZERO_VELOCITY, 1.0f));
            idleAnimationTime += delta * ANIMATION_IDLE_BREATH_SPEED;
        }

        float strideAmplitude = lerp(STRIDE_AMPLITUDE, SNEAK_STRIDE_AMPLITUDE, sneakWeight);
        float swingAngle = (float) Math.sin(walkAnimationTime) * strideAmplitude * walkAnimSpeed;

        float breathAngle = (float) Math.sin(idleAnimationTime) * BREATH_AMPLITUDE * idleWeight *
                (1.0f - sneakWeight * IDLE_SNEAK_FACTOR);

        float breathSwayZ = (float) Math.cos(idleAnimationTime * 0.5f) * BREATH_SWAY_AMPLITUDE * idleWeight;

        float sneakOffset = SNEAK_OFFSET * sneakWeight;
        float sneakTorsoLean = (float) Math.toRadians(SNEAK_TORSO_LEAN) * sneakWeight;
        float sneakArmBend = (float) Math.toRadians(SNEAK_ARM_BEND) * sneakWeight;

        float attackAngle = 0.0f;
        if (isAttacking) {
            attackAnimationTime += delta * ATTACK_ANIMATION_SPEED;
            attackAngle = (float) Math.sin(Math.min(attackAnimationTime, Math.PI)) * ATTACK_ANGLE;
            if (attackAnimationTime >= Math.PI) {
                isAttacking = false;
                attackAnimationTime = ZERO_VELOCITY;
            }
        }

        Quaternionf torsoRotation = new Quaternionf()
                .rotateX(-sneakTorsoLean + breathAngle);

        Quaternionf backpackRotation = new Quaternionf()
                .rotateX(-sneakTorsoLean + breathAngle);

        Quaternionf leftArmRotation = new Quaternionf()
                .rotateX(-swingAngle + breathAngle - sneakArmBend)
                .rotateZ(-breathSwayZ);

        Quaternionf rightArmRotation = new Quaternionf()
                .rotateX(swingAngle + breathAngle - sneakArmBend + attackAngle)
                .rotateZ(breathSwayZ);

        Quaternionf rightLegRotation = new Quaternionf()
                .rotateX(-swingAngle);

        Quaternionf leftLegRotation = new Quaternionf()
                .rotateX(swingAngle);

        float sneakLegBack = SNEAK_LEG_BACK_WEIGHT * sneakWeight;

        if (headNode != null && headTranslation != null) {
            Vector3f translation = new Vector3f(headTranslation);
            translation.y -= sneakOffset;
            headNode.setTranslation(translation);
        }

        if (torsoNode != null && torsoTranslation != null) {
            Vector3f translation = new Vector3f(torsoTranslation);
            translation.y -= sneakOffset;
            torsoNode.setTranslation(translation);
        }

        if (backpackNode != null && backpackTranslation != null) {
            Vector3f translation = new Vector3f(backpackTranslation);
            translation.y += sneakOffset;
            backpackNode.setTranslation(translation);
        }

        if (rightArmNode != null && rightArmTranslation != null) {
            Vector3f translation = new Vector3f(rightArmTranslation);
            translation.y -= sneakOffset;
            rightArmNode.setTranslation(translation);
        }

        if (leftArmNode != null && leftArmTranslation != null) {
            Vector3f translation = new Vector3f(leftArmTranslation);
            translation.y -= sneakOffset;
            leftArmNode.setTranslation(translation);
        }

        if (rightLegNode != null && rightLegTranslation != null) {
            Vector3f translation = new Vector3f(rightLegTranslation);
            translation.z += sneakLegBack;
            rightLegNode.setTranslation(translation);
        }

        if (leftLegNode != null && leftLegTranslation != null) {
            Vector3f translation = new Vector3f(leftLegTranslation);
            translation.z += sneakLegBack;
            leftLegNode.setTranslation(translation);
        }

        if (torsoNode != null) torsoNode.setRotation(torsoRotation);
        if (backpackNode != null) backpackNode.setRotation(backpackRotation);
        if (rightArmNode != null) rightArmNode.setRotation(rightArmRotation);
        if (leftArmNode != null) leftArmNode.setRotation(leftArmRotation);
        if (rightLegNode != null) rightLegNode.setRotation(rightLegRotation);
        if (leftLegNode != null) leftLegNode.setRotation(leftLegRotation);

        if (playerModel != null) {
            playerModel.updateTransforms();
        }

        if (Math.abs(velocity.x) > MOVEMENT_THRESHOLD || Math.abs(velocity.z) > MOVEMENT_THRESHOLD) {
            float rawYaw = (float) Math.toDegrees(Math.atan2(velocity.x, velocity.z));
            targetModelYaw = (rawYaw + 180.0f) % 360.0f;
            if (targetModelYaw < ZERO_VELOCITY) targetModelYaw += 360.0f;
            int sector = (int) Math.round(targetModelYaw / 45.0) % 8;

            this.direction = switch (sector) {
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

        currentEyeHeight = lerp(currentEyeHeight, targetEyeHeight,
                Math.clamp(delta * EYE_HEIGHT_LERP_SPEED, ZERO_VELOCITY, 1.0f));

        setAnimTimer(getAnimTimer() + delta);
        heal(((0.5f + getLevel()) * delta) / getDifficultyRegen());
        autoJump(gameMaster.getWorld(), getVelocity(), delta);
        updateEquipmentVisual();
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
        shader.setUniform("uParticleAlpha", 1.0f);
        shader.setUniform("uIsMaskPass", false);
        shader.setUniform("uEnableShadows", false);
        shader.setUniform("uLightSpaceMatrix", new Matrix4f());

        float globalScale = Settings.getScaledEntity();
        float idleBobbingY = (float) Math.sin(idleAnimationTime) * IDLE_BOBBING_AMPLITUDE * idleWeight;

        modelMatrix.identity()
                .translate(position.x, position.y + idleBobbingY, position.z)
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

    private void registerNode(String name) {
        GLTFNode node = playerModel.findNode(name);
        if (node != null) {
            node.setVisible(false);
            equipmentNodes.put(name, node);
        }
    }

    private void showEquipment(String equipment) {
        GLTFNode node = equipmentNodes.get(equipment);
        if (node != null) {
            node.setVisible(true);
        }
    }

    private void hideEquipment(String equipment) {
        GLTFNode node = equipmentNodes.get(equipment);
        if (node != null) {
            node.setVisible(false);
        }
    }

    private void updateEquipmentVisual() {
        for (GLTFNode node : equipmentNodes.values()) {
            node.setVisible(false);
        }

        Item item = gameMaster.getGameUIService()
                .getHotbarUI().getSelectedItem();
        if (item == null) {
            return;
        }

        String nodeName = switch (item) {
            case Pickaxe ignored -> "pickaxe";
            case Axe ignored -> "axe";
            case Hoe ignored -> "hoe";
            case Shovel ignored -> "shovel";
            case Sword ignored -> "sword";
            default -> null;
        };

        showEquipment(nodeName);
    }

    private void updateRotation(float delta) {
        float difference = targetModelYaw - modelYaw;

        while (difference > 180.0f) {
            difference -= 360.0f;
        }

        while (difference < -180.0f) {
            difference += 360.0f;
        }

        float maxRotation = ROTATION_SPEED * delta;
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

    public void interact() {
        this.isAttacking = true;
        this.attackAnimationTime = 0.0f;
    }

    public boolean isAttacking() {
        return isAttacking;
    }

    public void changeState(PlayerState newState) {
        if (currentState != null) {
            currentState.exit(this);
        }
        currentState = newState;
        currentState.enter(this);
    }

    public void autoJump(World world, Vector3f velocity, float delta) {
        if (!isOnGround() || (velocity.x == ZERO_VELOCITY && velocity.z == ZERO_VELOCITY)) {
            return;
        }

        Vector3f originalPos = new Vector3f(getPosition());
        getPosition().x += velocity.x * delta;
        getPosition().z += velocity.z * delta;
        boolean isBlockedAtFeet = checkCollision(world);
        setPosition(originalPos);

        if (isBlockedAtFeet) {
            getPosition().y += COLLISION_STEP_HEIGHT;
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

        float spawnX = SPAWN_X;
        float spawnZ = SPAWN_Z;

        GridPos highestAltitude = gameMaster.getWorld().getHighestY(spawnX, spawnZ);
        gameMaster.addEntity(this);
        setPosition(new Vector3f(spawnX, highestAltitude.y() + RESPAWN_Y_OFFSET, spawnZ));
        setVelocity(new Vector3f(INITIAL_HORIZONTAL_VELOCITY, INITIAL_Y_VELOCITY, INITIAL_HORIZONTAL_VELOCITY));
        setDimensions(new Vector3f(PLAYER_WIDTH, PLAYER_HEIGHT, PLAYER_WIDTH));
        setSpeed(PLAYER_SPEED);

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
            setVelocity(new Vector3f(ZERO_VELOCITY, getVelocity().y, ZERO_VELOCITY));
        } else {
            GridPos target = path.get(pathIndex);
            float targetX = target.x() + SPAWN_X;
            float targetZ = target.z() + SPAWN_Z;

            Vector3f position = getPosition();
            float dx = targetX - position.x;
            float dz = targetZ - position.z;

            float distanceSquared = dx * dx + dz * dz;

            if (distanceSquared < PATH_REACHED_DISTANCE_SQUARED) {
                pathIndex++;

                if (!isFollowingPath()) {
                    setVelocity(new Vector3f(ZERO_VELOCITY, getVelocity().y, ZERO_VELOCITY));
                }
            } else {
                Vector3f direction = new Vector3f(dx, ZERO_VELOCITY, dz);

                if (direction.lengthSquared() > ZERO_VELOCITY) {
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

        float moveX = ZERO_VELOCITY;
        float moveZ = ZERO_VELOCITY;

        if (Keyboard.isKeyDown(GLFW_KEY_W)) moveZ -= 1.0f;
        if (Keyboard.isKeyDown(GLFW_KEY_S)) moveZ += 1.0f;
        if (Keyboard.isKeyDown(GLFW_KEY_A)) moveX -= 1.0f;
        if (Keyboard.isKeyDown(GLFW_KEY_D)) moveX += 1.0f;

        Vector3f inputDir = new Vector3f(moveX, ZERO_VELOCITY, moveZ);

        if (inputDir.lengthSquared() > ZERO_VELOCITY) {
            inputDir.normalize();

            float yawRad = (float) Math.toRadians(cameraYaw);
            float sin = (float) Math.sin(yawRad);
            float cos = (float) Math.cos(yawRad);

            float worldX = inputDir.x * cos - inputDir.z * sin;
            float worldZ = inputDir.x * sin + inputDir.z * cos;

            Vector3f targetVelocity = new Vector3f(
                    worldX * getSpeed(),
                    getVelocity().y,
                    worldZ * getSpeed()
            );

            if (currentState instanceof SneakingState) {
                Vector3f nextPosition = new Vector3f(position).add(targetVelocity.x * delta,
                        ZERO_VELOCITY, targetVelocity.z * delta);

                int blockX = (int) Math.floor(nextPosition.x);
                int blockZ = (int) Math.floor(nextPosition.z);
                int blockY = (int) Math.floor(position.y - SNEAK_GROUND_OFFSET);

                if (world.getBlockTypeAt(blockX, blockY, blockZ) == BlockData.AIR.getId()) {
                    targetVelocity.x = ZERO_VELOCITY;
                    targetVelocity.z = ZERO_VELOCITY;
                }
            }

            collide(world, targetVelocity, delta);
        } else {
            Vector3f targetVelocity = new Vector3f(ZERO_VELOCITY, getVelocity().y, ZERO_VELOCITY);
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