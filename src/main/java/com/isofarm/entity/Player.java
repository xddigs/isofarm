package com.isofarm.entity;

import com.isofarm.data.*;
import com.isofarm.entity.states.GroundedState;
import com.isofarm.entity.states.SneakingState;
import com.isofarm.graphics.*;
import com.isofarm.graphics.gltf.GLTFModel;
import com.isofarm.graphics.gltf.GLTFNode;
import com.isofarm.input.Keyboard;
import com.isofarm.input.Mouse;
import com.isofarm.item.*;
import com.isofarm.pathfinding.GridPos;
import com.isofarm.service.BookService;
import com.isofarm.service.SoundService;
import com.isofarm.service.TimeService;
import com.isofarm.utils.Local;
import com.isofarm.utils.Settings;
import com.isofarm.utils.ToastFactory;
import com.isofarm.wrld.GameMaster;
import com.isofarm.wrld.World;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

import static org.joml.Math.lerp;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL13.*;

/**
 * Provides player behavior.
 */
@GodObject
public class Player extends Character {
    private static final Logger log = LoggerFactory.getLogger(Player.class);
    private static final float PLAYER_WIDTH = 0.5f;
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
    private static final float ATTACK_ANIMATION_SPEED = 10.0f;
    private static final float ATTACK_ANGLE = 1.35f;
    private static final float COLLISION_STEP_HEIGHT = 1.05f;
    private static final float SNEAK_GROUND_OFFSET = 0.05f;
    private static final float PATH_REACHED_DISTANCE_SQUARED = 0.01f;
    private static final float ZERO_VELOCITY = 0.0f;
    private static final float DEATH_RESPAWN_TIME = 5.0f;
    private static final float RESPAWN_Y_OFFSET = 1.0f;
    private static final float EYE_HEIGHT_LERP_SPEED = 10.0f;
    private static final float SNEAK_WEIGHT_LERP_SPEED = 75.0f;
    private static final float STRIDE_AMPLITUDE = 0.45f;
    private static final float ANIMATION_IDLE_BREATH_SPEED = 2.5f;
    private static final float ANIMATION_SPEED_FACTOR = 10.0f;
    private static final float FULL_DEGREES = 360.0f;
    private static final float HALF_DEGREES = 180.0f;
    private static final float MAX_HEAD_YAW = (float) Math.toRadians(65.0f);
    private static final float MAX_HEAD_PITCH = (float) Math.toRadians(35.0f);
    private static final float HEAD_TRACKING_SPEED = 12.0f;

    private final Matrix4f modelMatrix;
    private final GLTFModel playerModel;
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
    private Quaternionf headRotation;
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

    /**
     * Creates a new {@code Player} instance.
     * @param name the name value
     * @param world the world value
     */
    public Player(String name, World world) {
        super(name);
        this.modelMatrix = new Matrix4f();
        this.path = new LinkedList<>();

        this.playerModel = ResourceManager.rem.getPlayerModel();

        if (this.playerModel != null) {
            this.headNode = playerModel.findNode("Head");
            this.torsoNode = playerModel.findNode("Body");
            this.backpackNode = playerModel.findNode("Backpack");
            this.rightArmNode = playerModel.findNode("Right Arm");
            this.leftArmNode = playerModel.findNode("Left Arm");
            this.rightLegNode = playerModel.findNode("Right Leg");
            this.leftLegNode = playerModel.findNode("Left Leg");
            EquipmentController.ec.init(this.playerModel);
        }

        if (headNode != null) {
            headTranslation = new Vector3f(headNode.getTranslation());
            headRotation = new Quaternionf(headNode.getRotation());
        }

        if (torsoNode != null) {
            torsoTranslation = new Vector3f(torsoNode.getTranslation());
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

    /**
     * Updates the current state.
     * @param blockPos the block pos value
     * @param delta the delta value
     */
    @Override
    public void update(BlockPos blockPos, float delta) {
        if (!this.isAlive()) {
            if (respawnTimer <= ZERO_VELOCITY) {
                respawnTimer = DEATH_RESPAWN_TIME;
                dropLoot();
                GameMaster.game.toggleHUD();
                setGamemode(Gamemode.NO_CLIP);
            }

            respawnTimer -= delta;
            if (respawnTimer <= ZERO_VELOCITY) {
                respawn();
            }

            return;
        }

        currentState.input(this, GameMaster.game);
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

        float attackAngleX = 0.0f;
        float attackAngleY = 0.0f;
        float attackAngleZ = 0.0f;

        if (isAttacking) {
            attackAnimationTime += delta * ATTACK_ANIMATION_SPEED;
            float progress = (float) Math.min(attackAnimationTime / Math.PI, 1.0f);
            float swingCurve = (float) Math.sin(Math.sqrt(progress) * Math.PI);
            float crossCurve = (float) Math.sin(progress * Math.PI);
            attackAngleX = swingCurve * ATTACK_ANGLE;
            attackAngleY = swingCurve * (ATTACK_ANGLE * 0.25f);
            attackAngleZ = crossCurve * -(ATTACK_ANGLE * 0.4f);

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
                .rotateX(swingAngle + breathAngle - sneakArmBend + attackAngleX)
                .rotateY(attackAngleY)
                .rotateZ(breathSwayZ + attackAngleZ);

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
        updateHeadTracking(delta);

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
        if (!(currentState instanceof SneakingState)) {
            autoJump(GameMaster.game.getWorld(), getVelocity(), delta);
        }
        updateEquipmentVisual();
        checkDurability();
    }

    /**
     * Renders render.
     * @param game the game value
     * @param pass the pass value
     */
    @Override
    public void render(GameMaster game, RenderPass pass) {
        CameraView camera = game.getActiveCamera();

        if (pass.equals(RenderPass.SHADOW)) {
            Shader shadowShader = ResourceManager.rem.getShadowMapShader();
            if (shadowShader == null) return;
            shadowShader.bind();
            shadowShader.setUniform("uLightSpaceMatrix",
                    ShadowSystem.sys.getLightSpaceMatrix());

            float globalScale = Settings.getScaledEntity();

            modelMatrix.identity()
                    .translate(position.x, position.y, position.z)
                    .rotateY((float) Math.toRadians(modelYaw))
                    .scale(globalScale);

            shadowShader.setUniform("uModel", modelMatrix);

            glEnable(GL_DEPTH_TEST);
            glDepthFunc(GL_LESS);
            glDepthMask(true);

            playerModel.render(shadowShader, modelMatrix);

            glDepthMask(true);
            glDepthFunc(GL_LESS);
            shadowShader.unbind();
            return;
        }

        if (playerModel == null) return;
        Shader defaultShader = ResourceManager.rem.getDefaultShader();
        if (defaultShader == null) return;

        CelestialLighting lighting = GameMaster.game.getCelestialLighting();
        defaultShader.bind();
        defaultShader.setUniform("uProjection", camera.getProjectionMatrix());
        defaultShader.setUniform("uView", camera.getViewMatrix());

        defaultShader.setUniform("uLightIntensity", lighting.getIntensity());
        defaultShader.setUniform("uLightDirection", lighting.getDirection());
        defaultShader.setUniform("uAmbientIntensity", lighting.getAmbientIntensity());
        defaultShader.setUniform("uSkyColor", TimeService.getSkyColor());
        defaultShader.setUniform("uBaseColor", new Vector3f(1.0f));

        defaultShader.setUniform("uIsSprite", false);
        defaultShader.setUniform("uUseTexture", true);
        defaultShader.setUniform("uParticleAlpha", 1.0f);
        defaultShader.setUniform("uIsMaskPass", false);
        defaultShader.setUniform("uEnableShadows", Settings.doEnableShadows());
        defaultShader.setUniform("uLightSpaceMatrix", ShadowSystem.sys.getLightSpaceMatrix());
        defaultShader.setUniform("uIsSubmergedEntity", pass == RenderPass.SUBMERGED);

        float globalScale = Settings.getScaledEntity();
        float idleBobbingY = (float) Math.sin(idleAnimationTime) * IDLE_BOBBING_AMPLITUDE * idleWeight;

        modelMatrix.identity()
                .translate(position.x, position.y + idleBobbingY, position.z)
                .rotateY((float) Math.toRadians(modelYaw))
                .scale(globalScale);

        defaultShader.setUniform("uModel", modelMatrix);

        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        if (pass == RenderPass.NORMAL) {
            glDepthFunc(GL_LESS);
            glDepthMask(true);
            defaultShader.setUniform("uIsSubmergedEntity", false);

        } else {
            glDepthFunc(GL_GREATER);
            glDepthMask(false);
            defaultShader.setUniform("uIsSubmergedEntity", true);
        }

        playerModel.render(defaultShader, modelMatrix);
        glDepthFunc(GL_LESS);
        glDepthMask(true);
        glBindTexture(GL_TEXTURE_2D, 0);
        glDisable(GL_BLEND);
        defaultShader.unbind();
    }

    /**
     * Performs the on damage taken operation.
     * @param amount the amount value
     */
    @Override
    public void onDamageTaken(float amount) {
        damageSequence++;
        SoundService.fx.playEntitySound(SoundGroup.ENTITY);
    }

    /**
     * Performs the drop loot operation.
     */
    @Override
    protected void dropLoot() {
        for (Item i : List.copyOf(getInventory()
                .getItems().keySet())) {
            if (i == null) continue;
            if (i instanceof Undroppable) continue;
            int amount = getInventory().getAmount(i);
            if (amount <= 0) continue;
            WorldItem item = new WorldItem(i, amount, new Vector3f(position.x,
                    position.y, position.z));
            remove(i, amount);
            GameMaster.game.addEntity(item);
        }
    }

    /**
     * Returns the equipment name.
     * @param item the item value
     * @return the equipment name
     */
    private String getEquipmentName(Item item) {
        if (!(item instanceof Tool)) {
            return null;
        }

        return switch (item) {
            case Sword ignored -> "sword";
            case Pickaxe ignored -> "pickaxe";
            case Axe ignored -> "axe";
            case Hoe ignored -> "hoe";
            case Shovel ignored -> "shovel";
            default -> null;
        };
    }

    /**
     * Updates the equipment visual.
     */
    private void updateEquipmentVisual() {
        Item item = Settings.selectedItem;
        if (!(item instanceof Tool tool)) {
            EquipmentController.ec.equip(null, null);
            return;
        }

        String type = getEquipmentName(item);
        String material = tool.getTier().getName();
        EquipmentController.ec.equip(material, type);
    }

    /**
     * Updates the rotation.
     * @param delta the delta value
     */
    private void updateRotation(float delta) {
        float difference = targetModelYaw - modelYaw;

        while (difference > HALF_DEGREES) {
            difference -= FULL_DEGREES;
        }

        while (difference < -HALF_DEGREES) {
            difference += FULL_DEGREES;
        }

        float maxRotation = ROTATION_SPEED * delta;
        if (Math.abs(difference) <= maxRotation) {
            modelYaw = targetModelYaw;
        } else {
            modelYaw += Math.copySign(maxRotation, difference);
        }

        modelYaw %= FULL_DEGREES;
        if (modelYaw < 0.0f) {
            modelYaw += FULL_DEGREES;
        }
    }

    /**
     * Updates the head tracking.
     * @param delta the delta value
     */
    private void updateHeadTracking(float delta) {
        if (headNode == null || headRotation == null) return;

        GameMaster game = GameMaster.game;
        float screenWidth = Math.max(game.getWindowWidth(), 1.0f);
        float screenHeight = Math.max(game.getWindowHeight(), 1.0f);

        Ray mouseRay = game.getOrthoCamera().getMouseRay(
                Mouse.getX(), Mouse.getY(), screenWidth, screenHeight);
        Vector3f mouseWorldPosition = new Vector3f(mouseRay.origin());

        if (Math.abs(mouseRay.direction().y) > 0.0001f) {
            float t = (position.y - mouseRay.origin().y) / mouseRay.direction().y;
            mouseWorldPosition.fma(t, mouseRay.direction());
        }

        Vector3f toMouse = mouseWorldPosition.sub(position);
        float targetWorldYaw = (float) Math.atan2(toMouse.x, toMouse.z) + (float) Math.PI;
        float bodyYaw = (float) Math.toRadians(modelYaw);
        float targetYaw = Math.clamp(wrapRadians(targetWorldYaw - bodyYaw),
                -MAX_HEAD_YAW, MAX_HEAD_YAW);

        float normalizedMouseY = Math.clamp(
                (Mouse.getY() - screenHeight * 0.5f) / (screenHeight * 0.5f), -1.0f, 1.0f);
        float targetPitch = -normalizedMouseY * MAX_HEAD_PITCH;

        Quaternionf targetRotation = new Quaternionf(headRotation)
                .rotateY(targetYaw)
                .rotateX(targetPitch);
        float blend = 1.0f - (float) Math.exp(-HEAD_TRACKING_SPEED * delta);
        Quaternionf currentRotation = new Quaternionf(headNode.getRotation());
        currentRotation.slerp(targetRotation, Math.clamp(blend, 0.0f, 1.0f));
        headNode.setRotation(currentRotation);
    }

    /**
     * Performs the wrap radians operation.
     * @param angle the angle value
     * @return the wrap radians result
     */
    private static float wrapRadians(float angle) {
        while (angle > Math.PI) angle -= (float) (Math.PI * 2.0);
        while (angle < -Math.PI) angle += (float) (Math.PI * 2.0);
        return angle;
    }

    /**
     * Performs the interact operation.
     */
    public void interact() {
        this.isAttacking = true;
        this.attackAnimationTime = 0.0f;
    }

    /**
     * Checks whether the attacking condition is met.
     * @return {@code true} if attacking; otherwise {@code false}
     */
    public boolean isAttacking() {
        return isAttacking;
    }

    /**
     * Performs the change state operation.
     * @param newState the new state value
     */
    public void changeState(PlayerState newState) {
        if (currentState != null) {
            currentState.exit(this);
        }

        currentState = newState;
        currentState.enter(this);
    }

    /**
     * Performs the auto jump operation.
     * @param world the world value
     * @param velocity the velocity value
     * @param delta the delta value
     */
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

    /**
     * Performs the check durability operation.
     */
    private void checkDurability() {
        for (InventorySlot slot : getInventory().getSlots()) {
            if (slot.getItem() instanceof Tool tool) {
                if (tool.getDurability() == tool.getDurability() % 25) {
                    ToastFactory.warning(Local.lang.f("toast.item_warning", tool.getDisplayName()));
                    return;
                }

                if (tool.getDurability() <= 0) {
                    remove(tool);
                    SoundService.fx.playBreakSound(SoundGroup.ITEMS
                    );
                }
            }
        }
    }

    /**
     * Returns the current state.
     * @return the current state
     */
    public PlayerState getCurrentState() {
        return currentState;
    }

    /**
     * Sets the current state.
     * @param currentState the current state value
     */
    public void setCurrentState(PlayerState currentState) {
        this.currentState = currentState;
    }

    /**
     * Performs the respawn operation.
     */
    public void respawn() {
        if (!Settings.doKeepInventory()) {
            clear();
        }

        float spawnX = SPAWN_X;
        float spawnZ = SPAWN_Z;

        GridPos highestAltitude = GameMaster.game.getWorld().getHighestY(spawnX, spawnZ);
        GameMaster.game.addEntity(this);
        setPosition(new Vector3f(spawnX, highestAltitude.y() + RESPAWN_Y_OFFSET, spawnZ));
        setVelocity(new Vector3f(INITIAL_HORIZONTAL_VELOCITY, INITIAL_Y_VELOCITY, INITIAL_HORIZONTAL_VELOCITY));
        setDimensions(new Vector3f(PLAYER_WIDTH, PLAYER_HEIGHT, PLAYER_WIDTH));
        setSpeed(PLAYER_SPEED);

        setReputation(Reputation.NEUTRAL);
        setGamemode(Gamemode.SURVIVAL);

        setIsOffGroundTimer(0.0f);
        setWasOnGround(false);

        setMaxHitpoints(MAX_HITPOINTS);
        setHitpoints(getMaxHitpoints());

        setMaxStamina(MAX_STAMINA);
        setStamina(getMaxStamina());

        setExperience(0);
        setLevel(1);
        resetAttributes();
        respawnTimer = -1.0f;
        GameMaster.game.toggleHUD();
    }

    /**
     * Performs the reset attributes operation.
     */
    public void resetAttributes() {
        setStrength(1);
        setDexterity(1);
        setConstitution(1);
        setIntelligence(1);
        setWisdom(1);
        setCharisma(1);
    }

    /**
     * Returns the damage sequence.
     * @return the damage sequence
     */
    public int getDamageSequence() {
        return damageSequence;
    }

    /**
     * Performs the move operation.
     * @param world the world value
     * @param delta the delta value
     */
    public void move(World world, float delta) {
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

    /**
     * Performs the wasd operation.
     * @param world the world value
     * @param delta the delta value
     * @param cameraYaw the camera yaw value
     * @param isFlying the is flying value
     */
    public void wasd(World world, float delta, float cameraYaw,
                     boolean isFlying) {
        if (GameMaster.game.isChatOpen() || GameMaster.game.isInventoryOpen() ||
                BookService.bs.isOpen()) return;

        if (isFollowingPath()) {
            move(world, delta);
            return;
        }

        float moveX = ZERO_VELOCITY;
        float moveZ = ZERO_VELOCITY;

        if (Keyboard.isKeyDown(Keyboard.KEY_W)) moveZ -= 1.0f;
        if (Keyboard.isKeyDown(Keyboard.KEY_S)) moveZ += 1.0f;
        if (Keyboard.isKeyDown(Keyboard.KEY_A)) moveX -= 1.0f;
        if (Keyboard.isKeyDown(Keyboard.KEY_D)) moveX += 1.0f;

        Vector3f inputDir = new Vector3f(moveX, ZERO_VELOCITY, moveZ);

        if (inputDir.lengthSquared() > ZERO_VELOCITY) {
            inputDir.normalize();

            float yawRad = (float) Math.toRadians(cameraYaw);
            float sin = (float) Math.sin(yawRad);
            float cos = (float) Math.cos(yawRad);

            float worldX = inputDir.x * cos - inputDir.z * sin;
            float worldZ = inputDir.x * sin + inputDir.z * cos;

            Vector3f targetVelocity = new Vector3f(worldX * getSpeed(), getVelocity().y, worldZ * getSpeed());
            if (!isFlying) collide(world, targetVelocity, delta);
        } else {
            Vector3f targetVelocity = new Vector3f(ZERO_VELOCITY, getVelocity().y, ZERO_VELOCITY);
            if (!isFlying) collide(world, targetVelocity, delta);
        }
    }

    /**
     * Performs the fly operation.
     * @param delta the delta value
     * @param yaw the yaw value
     * @param isFlying the is flying value
     */
    public void fly(float delta, float yaw, boolean isFlying) {
        if (isOnGround()) return;
        wasd(GameMaster.game.getWorld(), delta, yaw, isFlying);
    }

    /**
     * Checks whether the ground below condition is met.
     * @param world the world value
     * @param testX the test x value
     * @param testZ the test z value
     * @return {@code true} if ground below; otherwise {@code false}
     */
    public boolean hasGroundBelow(World world, float testX, float testZ) {
        float epsilon = 0.001f;
        float halfWidth = dimensions.x / 2.0f - epsilon;
        float halfDepth = dimensions.z / 2.0f - epsilon;
        int blockY = (int) Math.floor(position.y - 0.2f);

        float[] minMaxX = { testX - halfWidth, testX + halfWidth };
        float[] minMaxZ = { testZ - halfDepth, testZ + halfDepth };

        for (float x : minMaxX) {
            for (float z : minMaxZ) {
                int bx = (int) Math.floor(x);
                int bz = (int) Math.floor(z);
                if (world.isBlockSolid(bx, blockY, bz)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Performs the adjust velocity operation.
     * @param world the world value
     * @param delta the delta value
     */
    @Override
    protected void adjustVelocity(World world, float delta) {
        if (!(currentState instanceof SneakingState) || delta <= ZERO_VELOCITY) return;
        if (!isOnGround() && !hasGroundBelow(world, position.x, position.z)) return;

        float moveX = velocity.x * delta;
        float moveZ = velocity.z * delta;
        float edgeStep = 0.05f;

        while (moveX != ZERO_VELOCITY &&
                !hasGroundBelow(world, position.x + moveX, position.z)) {
            moveX = moveTowardZero(moveX, edgeStep);
        }

        while (moveZ != ZERO_VELOCITY &&
                !hasGroundBelow(world, position.x, position.z + moveZ)) {
            moveZ = moveTowardZero(moveZ, edgeStep);
        }

        while (moveX != ZERO_VELOCITY && moveZ != ZERO_VELOCITY &&
                !hasGroundBelow(world, position.x + moveX, position.z + moveZ)) {
            moveX = moveTowardZero(moveX, edgeStep);
            moveZ = moveTowardZero(moveZ, edgeStep);
        }

        velocity.x = moveX / delta;
        velocity.z = moveZ / delta;
    }

    /**
     * Performs the move toward zero operation.
     * @param value the value value
     * @param amount the amount value
     * @return the move toward zero result
     */
    private static float moveTowardZero(float value, float amount) {
        if (Math.abs(value) <= amount) return ZERO_VELOCITY;
        return value - Math.copySign(amount, value);
    }

    /**
     * Sets the up inventory.
     */
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

    /**
     * Performs the sell operation.
     * @param item the item value
     * @param amount the amount value
     */
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
        ToastFactory.sell(Local.lang.f("toast.item_sold", amount, item.getDisplayName(), earnings));
        earn(earnings);
    }

    /**
     * Adds add.
     * @param item the item value
     * @param amount the amount value
     */
    public void add(Item item, int amount) {
        getInventory().add(item, amount);
        log.info("Added x{} of {} to inventory", amount, item.getName());
    }

    /**
     * Adds add.
     * @param item the item value
     */
    public void add(Item item) {
        add(item, 1);
        log.info("Added x1 of {} to inventory", item.getName());
    }

    /**
     * Adds the to backpack.
     * @param item the item value
     * @param amount the amount value
     */
    public void addToBackpack(Item item, int amount) {
        if (getBackpack().hasBackpackEquipped()) getBackpack().add(item, amount);
        log.info("Added x{} of {} to backpack", amount, item.getName());
    }

    /**
     * Adds the to backpack.
     * @param item the item value
     */
    public void addToBackpack(Item item) {
        addToBackpack(item, 1);
        log.info("Added x1 of {} to backpack", item.getName());
    }

    /**
     * Removes the from backpack.
     * @param item the item value
     * @param amount the amount value
     */
    public void removeFromBackpack(Item item, int amount) {
        if (getBackpack().hasBackpackEquipped()) getBackpack().remove(item, amount);
        log.info("Removed x{} of {} from backpack", amount, item.getName());
    }

    /**
     * Removes the from backpack.
     * @param item the item value
     */
    public void removeFromBackpack(Item item) {
        removeFromBackpack(item, 1);
        log.info("Removed x1 of {} from backpack", item.getName());
    }

    /**
     * Performs the sort operation.
     */
    public void sort() {
        getInventory().sort();
        getBackpack().sort();
    }

    /**
     * Removes remove.
     * @param item the item value
     * @param amount the amount value
     */
    public void remove(Item item, int amount) {
        if (getGamemode().isGodmode()) return;
        getInventory().remove(item, amount);
        log.info("Removed x{} of {} to inventory", amount, item.getName());
    }

    /**
     * Removes remove.
     * @param item the item value
     */
    public void remove(Item item) {
        if (getGamemode().isGodmode()) return;
        getInventory().remove(item, 1);
        log.info("Removed x1 of {} from inventory", item.getName());
    }

    /**
     * Removes clear.
     */
    public void clear() {
        for (Item item : getInventory().getItems().keySet()) {
            if (item == null) continue;
            if (item instanceof Backpack) continue;
            remove(item);
        }
        log.info("Cleared inventory");
    }

    /**
     * Checks whether the empty condition is met.
     * @return {@code true} if empty; otherwise {@code false}
     */
    public boolean isEmpty() {
        return getInventory().isEmpty();
    }

    /**
     * Performs the size operation.
     * @return the size result
     */
    public int size() {
        return getInventory().size();
    }

    /**
     * Returns get.
     * @param index the index value
     * @return the get result
     */
    public Item get(int index) {
        return getInventory().get(index);
    }

    /**
     * Returns get.
     * @param item the item value
     * @return the get result
     */
    public Item get(Item item) {
        return getInventory().get(item);
    }

    /**
     * Returns the amount.
     * @param item the item value
     * @return the amount
     */
    public int getAmount(Item item) {
        return getInventory().getAmount(item);
    }

    /**
     * Performs the earn operation.
     * @param amount the amount value
     */
    public void earn(int amount) {
        log.info("Earned ${}", amount);
        getPurse().add(amount);
    }

    /**
     * Performs the spend operation.
     * @param amount the amount value
     */
    public void spend(int amount) {
        if (amount <= 0) return;
        log.info("Spent ${}", amount);
        getPurse().remove(amount);
    }

    /**
     * Checks whether the space condition is met.
     * @return {@code true} if space; otherwise {@code false}
     */
    public boolean hasSpace() {
        return !getInventory().isFull() ||
                (getBackpack().hasBackpackEquipped()
                        && !getBackpack().isFull());
    }

    /**
     * Checks whether the seeds condition is met.
     * @return {@code true} if seeds; otherwise {@code false}
     */
    public boolean hasSeeds() {
        return getInventory().hasItemOfType(Seed.class);
    }

    /**
     * Returns the current eye height.
     * @return the current eye height
     */
    public float getCurrentEyeHeight() {
        return currentEyeHeight;
    }

    /**
     * Returns the forward.
     * @return the forward
     */
    public float getForward() {
        return (float) Math.atan2(velocity.z, velocity.x);
    }

    /**
     * Returns the direction.
     * @return the direction
     */
    public Direction getDirection() {
        return direction;
    }

    /**
     * Checks whether the following path condition is met.
     * @return {@code true} if following path; otherwise {@code false}
     */
    public boolean isFollowingPath() {
        return pathIndex < path.size();
    }

    /**
     * Returns the path.
     * @return the path
     */
    public List<GridPos> getPath() {
        return path;
    }

    /**
     * Sets the path.
     * @param path the path value
     */
    public void setPath(List<GridPos> path) {
        this.path = path != null ? path : List.of();
        this.pathIndex = 0;
    }

    /**
     * Returns the path index.
     * @return the path index
     */
    public int getPathIndex() {
        return pathIndex;
    }

    /**
     * Sets the path index.
     * @param pathIndex the path index value
     */
    public void setPathIndex(int pathIndex) {
        this.pathIndex = Math.max(0, pathIndex);
    }

    /**
     * Clears the path.
     */
    public void clearPath() {
        this.path = List.of();
        this.pathIndex = 0;
    }

    /**
     * Returns the respawn timer.
     * @return the respawn timer
     */
    public float getRespawnTimer() {
        return respawnTimer;
    }

    /**
     * Sets the respawn timer.
     * @param respawnTimer the respawn timer value
     */
    public void setRespawnTimer(float respawnTimer) {
        this.respawnTimer = respawnTimer;
    }

    /**
     * Returns the target eye height.
     * @return the target eye height
     */
    public float getTargetEyeHeight() {
        return targetEyeHeight;
    }

    /**
     * Sets the target eye height.
     * @param targetEyeHeight the target eye height value
     */
    public void setTargetEyeHeight(float targetEyeHeight) {
        this.targetEyeHeight = targetEyeHeight;
    }

    /**
     * Checks whether the falling condition is met.
     * @return {@code true} if falling; otherwise {@code false}
     */
    public boolean isFalling() {
        return isFalling;
    }

    /**
     * Sets the falling.
     * @param falling the falling value
     */
    public void setFalling(boolean falling) {
        isFalling = falling;
    }

    /**
     * Returns the difficulty regen.
     * @return the difficulty regen
     */
    public float getDifficultyRegen() {
        return GameMaster.game.getDifficulty().getMultiplier();
    }
}
