package com.soilcraft.entity;

import com.soilcraft.data.*;
import com.soilcraft.graphics.CharacterModel;
import com.soilcraft.graphics.CharacterRenderer;
import com.soilcraft.service.SoundService;
import com.soilcraft.service.ToastService;
import com.soilcraft.utils.Settings;
import com.soilcraft.wrld.GameMaster;
import com.soilcraft.wrld.World;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DataClass
public class Player extends Character {
    private static final Logger log = LoggerFactory.getLogger(Player.class);
    private final String name;
    private final ToastService toastService;
    private final CharacterModel characterModel;
    private final CharacterRenderer characterRenderer;
    private final SoundService soundService;

    public Player(String name, World world, ToastService toastService,
                  SoundService soundService) {
        super(name, toastService);
        this.name = name;
        this.toastService = toastService;
        this.soundService = soundService;

        this.characterModel = new CharacterModel();
        this.characterRenderer = new CharacterRenderer();

        float spawnX = 0.5f;
        float spawnZ = 0.5f;
        float highestY = world.getHighestY(spawnX, spawnZ);
        setPosition(new Vector3f(spawnX, highestY, spawnZ));
        setVelocity(new Vector3f(0.0f, 0.0f, 0.0f));
        setDimensions(new Vector3f(0.6f, 2.0f, 0.6f));
        setCrouchingHeight(1.8f);
        setUpInventory();
    }

    @Override
    public void update(float delta) {
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
        if (gameMaster.isOrthographicCamera()) {
            characterRenderer.render(gameMaster, characterModel, position);
        }
    }

    public void move(World world, Vector3f direction, float delta) {
        moveAndCollide(world, direction, delta);
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
        float eyeHeight = isCrunching() ? 0.85f : 1.6f;
        return new Vector3f(
                position.x,
                position.y + eyeHeight,
                position.z
        );
    }

    public float getForward() {
        return (float) Math.atan2(velocity.z, velocity.x);
    }
}