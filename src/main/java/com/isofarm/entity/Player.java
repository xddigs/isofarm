package com.isofarm.entity;

import com.isofarm.data.*;
import com.isofarm.graphics.*;
import com.isofarm.service.SoundService;
import com.isofarm.service.TimeService;
import com.isofarm.service.ToastService;
import com.isofarm.utils.Settings;
import com.isofarm.wrld.GameMaster;
import com.isofarm.wrld.World;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.lwjgl.opengl.GL13.*;

@DataClass
public class Player extends Character {
    private static final Logger log = LoggerFactory.getLogger(Player.class);
    private final String name;
    private final ToastService toastService;
    private final SoundService soundService;
    private final Matrix4f modelMatrix;

    private Direction direction = Direction.SOUTH_WEST;

    public Player(String name, World world, ToastService toastService,
                  SoundService soundService) {
        super(name, toastService);
        this.name = name;
        this.toastService = toastService;
        this.soundService = soundService;
        this.modelMatrix = new Matrix4f();

        float spawnX = 0.5f;
        float spawnZ = 0.5f;
        float highestY = world.getHighestY(spawnX, spawnZ);
        setPosition(new Vector3f(spawnX, highestY, spawnZ));
        setVelocity(new Vector3f(0.0f, 0.0f, 0.0f));
        setDimensions(new Vector3f(1.0f, 1.0f, 1.0f));
        setUpInventory();
        setReputation(Reputation.NEUTRAL);
    }

    @Override
    public void update(Hit hit, float delta) {
        updateCrouching(delta);
        for (InventorySlot slot : getInventory().getSlots()) {
            if (slot.getItem() instanceof Tool tool) {
                if (tool.getDurability() <= 0) {
                    remove(tool);
                    toastService.error("Your " + tool.getName() + " broke!");
                    soundService.playBreakSound(SoundGroup.ITEMS,
                            1.0f, Settings.maxInteractionDistance);
                }
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

        CelestialLighting lighting = gameMaster.getCelestialLighting();
        if (lighting != null) {
            shader.setUniform("uSunColor", lighting.getColor());
            shader.setUniform("uLightIntensity", lighting.getIntensity());
            shader.setUniform("uLightDirection", lighting.getDirection());
            shader.setUniform("uAmbientIntensity", Math.max(0.6f, lighting.getAmbientIntensity()));
        } else {
            shader.setUniform("uSunColor", new Vector3f(1.0f, 1.0f, 1.0f));
            shader.setUniform("uLightIntensity", 1.0f);
            shader.setUniform("uLightDirection", new Vector3f(-0.5f, -1.0f, -0.5f));
            shader.setUniform("uAmbientIntensity", 0.8f);
        }

        shader.setUniform("uSkyColor", TimeService.getSkyColor());
        shader.setUniform("uParticleAlpha", 1.0f);
        shader.setUniform("uIsMaskPass", false);
        shader.setUniform("uEnableShadows", false);

        shader.setUniform("uLightSpaceMatrix", new Matrix4f());
        shader.setUniform("uProjection", camera.getProjectionMatrix());
        shader.setUniform("uView", camera.getViewMatrix());

        float scaleX = (dimensions == null || dimensions.x <= 0) ? 1.0f : dimensions.x;
        float scaleY = (dimensions == null || dimensions.y <= 0) ? 1.0f : dimensions.y;
        float scaleZ = (dimensions == null || dimensions.z <= 0) ? 1.0f : dimensions.z;

        modelMatrix.identity()
                .translate(position)
                .scale(scaleX, scaleY, scaleZ);

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

    public void lookAt(float targetX, float targetZ) {
        float dx = targetX - position.x;
        float dz = targetZ - position.z;

        if (dx * dx + dz * dz < 0.0001f) {
            return;
        }
        direction = Direction.fromVector(dx, dz);
    }

    public String getName() {
        return name;
    }

    private void setUpInventory() {
        add(new Seed(), 4);
        add(new Seed(CropType.CARROT), 4);
        add(new Hoe(), 1);
        add(new Pickaxe(), 1);
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

    public void remove(Item item, int amount) {
        getInventory().remove(item, amount);
        log.info("Removed x{} of {} to inventory", amount, item.getName());
    }

    public void remove(Item item) {
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
}