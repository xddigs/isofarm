package com.isofarm.entity.plyr;

import com.isofarm.data.Gamemode;
import com.isofarm.data.InventorySlot;
import com.isofarm.data.Reputation;
import com.isofarm.data.Seed;
import com.isofarm.data.SoundGroup;
import com.isofarm.data.StartingKit;
import com.isofarm.entity.Player;
import com.isofarm.entity.WorldItem;
import com.isofarm.item.*;
import com.isofarm.pathfinding.GridPos;
import com.isofarm.service.SoundService;
import com.isofarm.utils.Local;
import com.isofarm.utils.Settings;
import com.isofarm.utils.ToastFactory;
import com.isofarm.wrld.GameMaster;
import com.isofarm.wrld.World;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/** Owns player rules concerning life, attributes, inventory, loot and currency. */
public final class PlayerGameplay {
    private static final Logger log = LoggerFactory.getLogger(PlayerGameplay.class);
    private static final float WIDTH = 0.5f, HEIGHT = 2.0f, SPAWN_X = 0.5f, SPAWN_Z = 0.5f;
    private static final float SPEED = 6.0f, RESPAWN_DELAY = 5.0f;
    private static final int MAX_HITPOINTS = 20, MAX_STAMINA = 100;
    private Player player;
    private int damageSequence;
    private float respawnTimer = -1.0f;

    /** Creates the shared player's gameplay component. */
    public PlayerGameplay() {}

    /** Initializes the player from the shared world. */
    public void initialize() {
        player = Player.plyr;
        World world = World.wrld;
        GridPos altitude = world.getHighestY(SPAWN_X, SPAWN_Z);
        player.setPosition(new Vector3f(SPAWN_X, altitude.y(), SPAWN_Z));
        player.setVelocity(new Vector3f());
        player.setDimensions(new Vector3f(WIDTH, HEIGHT, WIDTH));
        player.setMaxHitpoints(MAX_HITPOINTS); player.setHitpoints(MAX_HITPOINTS);
        player.setMaxStamina(MAX_STAMINA); player.setStamina(MAX_STAMINA);
        player.setSpeed(SPEED); player.setReputation(Reputation.NEUTRAL);
        player.setGamemode(Gamemode.SURVIVAL);
        setUpInventory();
    }

    /**
     * Advances death and respawn handling.
     * @param delta frame time
     * @return whether normal player updates should continue
     */
    public boolean updateLifeCycle(float delta) {
        if (player.isAlive()) return true;
        if (respawnTimer <= 0.0f) {
            respawnTimer = RESPAWN_DELAY;
            dropLoot();
            GameMaster.game.toggleHUD();
            player.setGamemode(Gamemode.NO_CLIP);
        }
        respawnTimer -= delta;
        if (respawnTimer <= 0.0f) respawn();
        return false;
    }

    /** @param delta frame time */
    public void update(float delta) {
        player.setAnimTimer(player.getAnimTimer() + delta);
        player.heal(((0.5f + player.getLevel()) * delta) / getDifficultyRegen());
        checkDurability();
    }

    /** @param amount received damage */
    public void onDamageTaken(float amount) {
        damageSequence++;
        SoundService.fx.playEntitySound(SoundGroup.ENTITY);
    }

    /** Drops all droppable inventory stacks into the world. */
    public void dropLoot() {
        for (Item item : List.copyOf(player.getInventory().getItems().keySet())) {
            if (item == null || item instanceof Undroppable) continue;
            int amount = player.getInventory().getAmount(item);
            if (amount <= 0) continue;
            GameMaster.game.addEntity(new WorldItem(item, amount, new Vector3f(player.getPosition())));
            remove(item, amount);
        }
    }

    /** Restores spawn position, core stats and attributes. */
    public void respawn() {
        if (!Settings.doKeepInventory()) clear();
        GridPos altitude = GameMaster.game.getWorld().getHighestY(SPAWN_X, SPAWN_Z);
        player.setPosition(new Vector3f(SPAWN_X, altitude.y() + 1.0f, SPAWN_Z));
        player.setVelocity(new Vector3f()); player.setDimensions(new Vector3f(WIDTH, HEIGHT, WIDTH));
        player.setSpeed(SPEED); player.setReputation(Reputation.NEUTRAL); player.setGamemode(Gamemode.SURVIVAL);
        player.setIsOffGroundTimer(0.0f); player.setWasOnGround(false);
        player.setMaxHitpoints(MAX_HITPOINTS); player.setHitpoints(MAX_HITPOINTS);
        player.setMaxStamina(MAX_STAMINA); player.setStamina(MAX_STAMINA);
        player.setExperience(0); player.setLevel(1); resetAttributes();
        respawnTimer = -1.0f;
        GameMaster.game.toggleHUD();
    }

    /** Resets all six character attributes to their base value. */
    public void resetAttributes() {
        player.setStrength(1); player.setDexterity(1); player.setConstitution(1);
        player.setIntelligence(1); player.setWisdom(1); player.setCharisma(1);
    }

    private void setUpInventory() {
        if (player.getGamemode() == Gamemode.SURVIVAL)
            for (Item item : new StartingKit().getItems()) add(item);
    }

    private void checkDurability() {
        for (InventorySlot slot : player.getInventory().getSlots()) {
            if (slot.getItem() instanceof Tool tool) {
                if (tool.getDurability() == tool.getDurability() % 25) {
                    ToastFactory.warning(Local.lang.f("toast.item_warning", tool.getDisplayName()));
                    return;
                }
                if (tool.getDurability() <= 0) {
                    remove(tool);
                    SoundService.fx.playBreakSound(SoundGroup.ITEMS);
                }
            }
        }
    }

    /** @param item item @param amount quantity */
    public void sell(Item item, int amount) {
        if (item == null || amount <= 0) return;
        int current = player.getInventory().getAmount(item);
        if (current <= 0) { log.warn("No {} in inventory to sell", item.getName()); return; }
        int sold = Math.min(current, amount);
        player.getInventory().remove(item, sold);
        int earnings = sold * item.getValue();
        ToastFactory.sell(Local.lang.f("toast.item_sold", amount, item.getDisplayName(), earnings));
        earn(earnings);
    }

    /** @param item item @param amount quantity */ public void add(Item item, int amount) { player.getInventory().add(item, amount); log.info("Added x{} of {} to inventory", amount, item.getName()); }
    /** @param item item */ public void add(Item item) { add(item, 1); log.info("Added x1 of {} to inventory", item.getName()); }
    /** @param item item @param amount quantity */ public void addToBackpack(Item item, int amount) { if (player.getBackpack().hasBackpackEquipped()) player.getBackpack().add(item, amount); log.info("Added x{} of {} to backpack", amount, item.getName()); }
    /** @param item item */ public void addToBackpack(Item item) { addToBackpack(item, 1); log.info("Added x1 of {} to backpack", item.getName()); }
    /** @param item item @param amount quantity */ public void removeFromBackpack(Item item, int amount) { if (player.getBackpack().hasBackpackEquipped()) player.getBackpack().remove(item, amount); log.info("Removed x{} of {} from backpack", amount, item.getName()); }
    /** @param item item */ public void removeFromBackpack(Item item) { removeFromBackpack(item, 1); log.info("Removed x1 of {} from backpack", item.getName()); }
    /** Sorts both storage containers. */ public void sort() { player.getInventory().sort(); player.getBackpack().sort(); }
    /** @param item item @param amount quantity */ public void remove(Item item, int amount) { if (!player.getGamemode().isGodmode()) { player.getInventory().remove(item, amount); log.info("Removed x{} of {} to inventory", amount, item.getName()); } }
    /** @param item item */ public void remove(Item item) { if (!player.getGamemode().isGodmode()) { player.getInventory().remove(item, 1); log.info("Removed x1 of {} from inventory", item.getName()); } }
    /** Clears non-backpack inventory items. */ public void clear() { for (Item item : List.copyOf(player.getInventory().getItems().keySet())) if (item != null && !(item instanceof Backpack)) remove(item); log.info("Cleared inventory"); }
    /** @return whether inventory is empty */ public boolean isEmpty() { return player.getInventory().isEmpty(); }
    /** @return inventory size */ public int size() { return player.getInventory().size(); }
    /** @param index index @return indexed item */ public Item get(int index) { return player.getInventory().get(index); }
    /** @param item key @return matching item */ public Item get(Item item) { return player.getInventory().get(item); }
    /** @param item item @return quantity */ public int getAmount(Item item) { return player.getInventory().getAmount(item); }
    /** @param amount currency amount */ public void earn(int amount) { log.info("Earned ${}", amount); player.getPurse().add(amount); }
    /** @param amount currency amount */ public void spend(int amount) { if (amount > 0) { log.info("Spent ${}", amount); player.getPurse().remove(amount); } }
    /** @return whether storage has space */ public boolean hasSpace() { return !player.getInventory().isFull() || player.getBackpack().hasBackpackEquipped() && !player.getBackpack().isFull(); }
    /** @return whether seeds are present */ public boolean hasSeeds() { return player.getInventory().hasItemOfType(Seed.class); }
    /** @return damage event sequence */ public int getDamageSequence() { return damageSequence; }
    /** @return respawn timer */ public float getRespawnTimer() { return respawnTimer; }
    /** @param value respawn timer */ public void setRespawnTimer(float value) { respawnTimer = value; }
    /** @return regeneration multiplier */ public float getDifficultyRegen() { return GameMaster.game.getDifficulty().getMultiplier(); }
}
