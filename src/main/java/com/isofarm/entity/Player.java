package com.isofarm.entity;

import com.isofarm.data.*;
import com.isofarm.graphics.CameraView;
import com.isofarm.graphics.ResourceManager;
import com.isofarm.graphics.Shader;
import com.isofarm.graphics.SpriteSheet;
import com.isofarm.item.*;
import com.isofarm.pathfinding.GridPos;
import com.isofarm.utils.K;
import com.isofarm.utils.Settings;
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

@SuppressWarnings("all")
@DataClass
public class Player extends Character {
    private static final Logger log = LoggerFactory.getLogger(Player.class);
    private final String name;
    private final Matrix4f modelMatrix;
    private final GameMaster gameMaster;

    private Direction direction = Direction.SOUTH;
    private List<GridPos> path;

    private int pathIndex = 0;
    private int damageSequence = 0;
    private float lastDamageAmount = 0.0f;
    private float fallStartY = 0.0f;
    private boolean isFalling = false;

    public Player(String name, World world, GameMaster gameMaster) {
        super(name, gameMaster.getToastService());
        this.name = name;
        this.gameMaster = gameMaster;
        this.modelMatrix = new Matrix4f();
        this.path = new LinkedList<>();

        float spawnX = 0.5f;
        float spawnZ = 0.5f;
        GridPos highestAltitude = world.getHighestY(spawnX, spawnZ);
        setPosition(new Vector3f(spawnX, highestAltitude.y(), spawnZ));
        setVelocity(new Vector3f(0.0f, 0.0f, 0.0f));
        setDimensions(new Vector3f(0.5f, 1.0f, 0.5f));
        setSpeed(6.0f);
        setReputation(Reputation.NEUTRAL);
        setGamemode(Gamemode.SURVIVAL);
        setUpInventory();
    }

    @Override
    public void update(Hit hit, float delta) {
        if (!isAlive()) {
            dropLoot();
            return;
        }

        setAnimTimer(getAnimTimer() + delta);
        updateCrouching(delta);
        heal((0.5f + getLevel()) * delta);

        if (!isOnGround()) {
            if (getVelocity().y < 0.0f && !isFalling) {
                isFalling = true;
                fallStartY = getPosition().y;
            }

        } else {
            if (isFalling) {
                float fallDistance = fallStartY - getPosition().y;
                if (fallDistance > 3.0f) {
                    float damage = (fallDistance - 3.0f) * 2.0f;
                    fallDamage(damage);
                    getSoundService().playBreakSound(SoundGroup.ENTITY, 1.0f, 1.0f);
                }
                isFalling = false;
            }
        }

        for (InventorySlot slot : getInventory().getSlots()) {
            if (slot.getItem() instanceof Tool tool && tool.getDurability() <= 0) {
                remove(tool);
                gameMaster.getToastService().error("Your " + tool.getName() + " broke!");
                getSoundService().playBreakSound(SoundGroup.ITEMS, 1.0f,
                        Settings.getMaxInteractionDistance());
            }
        }
    }

    @Override
    public void render(GameMaster gameMaster) {
        ResourceManager rm = gameMaster.getResourceManager();
        CameraView camera = gameMaster.getActiveCamera();
        SpriteSheet sheet = rm.getPlayerSpriteSheet();

        if (sheet == null || rm.getPlayerMesh() == null) {
            return;
        }

        Shader shader = rm.getDefaultShader();
        int textureUnit = K.Render.PRIMARY_TEXTURE_UNIT;
        boolean isMoving = getVelocity().lengthSquared() > 0.001f;
        boolean flipHorizontal = false;
        int baseRow = 0;
        if (direction != null) {
            switch (direction) {
                case NORTH_WEST, NORTH_EAST -> baseRow = 4;
                case WEST -> {
                    baseRow = 2;
                    flipHorizontal = true;
                }
                case EAST -> baseRow = 2;
                default -> baseRow = 0;
            }
        }

        int rowIndex = isMoving ? baseRow + 1 : baseRow;
        int totalFramesInRow = isMoving ? 8 : 4;
        int currentFrame = (int) (getAnimTimer() / getFrameDuration()) % totalFramesInRow;

        int sheetColumns = 8;
        int spriteIndex = (rowIndex * sheetColumns) + currentFrame;

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

        float aspect = sheet.getFrameHeight() > 0 ? (float) sheet.getFrameWidth() / (float) sheet.getFrameHeight() : 1.0f;
        float baseScaleY = dimensions == null || dimensions.y <= 0 ? 1.0f : dimensions.y;
        float baseScaleX = (baseScaleY * aspect) * (flipHorizontal ? -1.0f : 1.0f);
        float baseScaleZ = dimensions == null || dimensions.z <= 0 ? 1.0f : dimensions.z;

        float yawRad = (float) Math.toRadians(-camera.getYaw());
        modelMatrix.identity().translate(position).rotateY(yawRad).scale(baseScaleX, baseScaleY, baseScaleZ);
        shader.setUniform("uModel", modelMatrix);

        glDisable(GL_CULL_FACE);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_DEPTH_TEST);
        glDepthMask(true);

        rm.getPlayerMesh().render();

        sheet.unbind();
        shader.unbind();
    }

    @Override
    protected void onDamageTaken(float amount) {
        lastDamageAmount = amount;
        damageSequence++;
        getSoundService().playEntitySound(SoundGroup.ENTITY);
    }

    @Override
    protected void dropLoot() {
        for (Item i : getInventory().getItems().keySet()) {
            switch (i) {
                case Backpack ignored -> { continue; }
                case CraftingKit ignored -> { continue; }
                default -> {}
            }
            WorldItem item = new WorldItem(i, getInventory().getAmount(i),
                    new Vector3f(position.x, position.y, position.z));
            gameMaster.addEntity(item);
        }
    }

    public void respawn() {
        if (!Settings.doKeepInventory()) clear();
        float spawnX = 0.5f;
        float spawnZ = 0.5f;
        GridPos highestAltitude = gameMaster.getWorld().getHighestY(spawnX, spawnZ);
        setPosition(new Vector3f(spawnX, highestAltitude.y() + 1.0f, spawnZ));
        setVelocity(new Vector3f(0.0f, 0.0f, 0.0f));
        setDimensions(new Vector3f(0.5f, 1.0f, 0.5f));
        setSpeed(6.0f);
        setReputation(Reputation.NEUTRAL);
        setGamemode(Gamemode.SURVIVAL);
        setIsOffGroundTimer(0.0f);
        setWasOnGround(false);

        setMaxHitpoints(100);
        setHitpoints(getMaxHitpoints());

        setMaxStamina(100);
        setStamina(getMaxStamina());

        setExperience(0);
        setLevel(1);
        resetAttributes();
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
                .mul(this.getSpeed());

        setVelocity(velocity);
        move(world, velocity, delta);
        position.y = targetY;
    }

    public String getName() {
        return name;
    }

    private void setUpInventory() {
        switch (getGamemode()) {
            case SURVIVAL -> {
                Kit kit = new StartingKit();
                for (Item item : kit.getItems()) { add(item, 1); }
            }
            case GODMODE -> {}
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
        gameMaster.getToastService().sell("You successfully sold " + item.getName() + " for " + earnings + " coins");
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

    public boolean craft(Recipe recipe) {
        if (!hasIngredients(recipe)) return false;
        if (!hasSpace()) return false;

        for (Ingredient ingredient : recipe.ingredients()) {
            Craftable materialToRemove = new Material(ingredient.craftable());
            getInventory().remove(materialToRemove, ingredient.amount());
        }

        if (!getInventory().isFull()) {
            add(recipe.result(), recipe.resultAmount());
        } else if (getBackpack().hasBackpackEquipped() && !getBackpack().isFull()) {
            addToBackpack(recipe.result(), recipe.resultAmount());
        }

        gameMaster.getToastService().success("You crafted " +
                recipe.resultAmount() + " " + recipe.result().getName());

        return true;
    }

    public boolean hasIngredients(Recipe recipe) {
        for (Ingredient ingredient : recipe.ingredients()) {
            Craftable requiredMaterial = new Material(ingredient.craftable());
            if (getInventory().getAmount(requiredMaterial) < ingredient.amount()) {
                return false;
            }
        }
        return true;
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