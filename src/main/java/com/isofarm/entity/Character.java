package com.isofarm.entity;

import com.isofarm.data.*;
import com.isofarm.utils.ToastFactory;

/**
 * Provides character behavior.
 */
@DataClass
public abstract class Character extends Entity implements Levelable {
    private static final float FRAME_DURATION = 0.15f;
    private final Purse purse;
    private Inventory inventory;
    private Inventory backpack;
    private Reputation reputation;
    private Gamemode gamemode;
    private int level;
    private int experience;
    private int experienceForNextLevel;
    private float stamina;
    private float maxStamina;
    private int strength;
    private int intelligence;
    private int dexterity;
    private int constitution;
    private int wisdom;
    private int charisma;
    private int luck;
    private float isOffGroundTimer;
    private float animTimer = 0.0f;

    /**
     * Creates a new {@code Character} instance.
     * @param name the name value
     */
    public Character(String name) {
        super(name);
        this.inventory = new Inventory();
        this.backpack = new Inventory();
        this.purse = new Purse();
        this.reputation = Reputation.NEUTRAL;

        this.level = 1;
        this.experience = 0;
        this.experienceForNextLevel = 100;

        this.strength = 8;
        this.intelligence = 8;
        this.dexterity = 8;
        this.constitution = 8;
        this.wisdom = 8;
        this.charisma = 8;
        this.luck = 50;
    }

    /**
     * Returns the level.
     * @return the level
     */
    @Override
    public int getLevel() {
        return level;
    }

    /**
     * Sets the level.
     * @param level the level value
     */
    @Override
    public void setLevel(int level) {
        this.level = level;
    }

    /**
     * Returns the experience.
     * @return the experience
     */
    @Override
    public int getExperience() {
        return experience;
    }

    /**
     * Sets the experience.
     * @param experience the experience value
     */
    @Override
    public void setExperience(int experience) {
        this.experience = experience;
    }

    /**
     * Returns the experience for next level.
     * @return the experience for next level
     */
    @Override
    public int getExperienceForNextLevel() {
        return experienceForNextLevel;
    }

    /**
     * Sets the experience for next level.
     * @param experienceForNextLevel the experience for next level value
     */
    @Override
    public void setExperienceForNextLevel(int experienceForNextLevel) {
        this.experienceForNextLevel = experienceForNextLevel;
    }

    /**
     * Performs the gain operation.
     * @param experience the experience value
     */
    @Override
    public void gain(int experience) {
        if (experience <= 0) return;
        this.experience += experience;
        while (this.experience >= experienceForNextLevel) {
            this.experience -= experienceForNextLevel;
            levelUp();

            int levelUpScaling = (int) (level * 0.8f);
            scale(levelUpScaling);
            maxHitpoints += 1;
            maxStamina += 1;
            experienceForNextLevel = calcNextLevel();
        }
    }

    /**
     * Performs the calc next level operation.
     * @return the calc next level result
     */
    @Override
    public int calcNextLevel() {
        return (int) (100 * Math.pow(1.2, level - 1));
    }

    /**
     * Performs the level up operation.
     */
    @Override
    public void levelUp() {
        level++;
        ToastFactory.success("Level up! You're now level " + level);
    }

    /**
     * Returns the frame duration.
     * @return the frame duration
     */
    public float getFrameDuration() {
        return FRAME_DURATION;
    }

    /**
     * Returns the anim timer.
     * @return the anim timer
     */
    public float getAnimTimer() {
        return animTimer;
    }

    /**
     * Sets the anim timer.
     * @param animTimer the anim timer value
     */
    public void setAnimTimer(float animTimer) {
        this.animTimer = animTimer;
    }

    /**
     * Performs the scale operation.
     * @param amount the amount value
     */
    public void scale(int amount) {
        strength += (int) (amount * Math.random());
        intelligence += (int) (amount * Math.random());
        dexterity += (int) (amount * Math.random());
        constitution += (int) (amount * Math.random());
        wisdom += (int) (amount * Math.random());
        charisma += (int) (amount * Math.random());
    }

    /**
     * Performs the fall damage operation.
     * @param amount the amount value
     */
    public void fallDamage(float amount) {
        if (!isAlive() || amount <= 0) return;
        if (gamemode.isGodmode() || gamemode.isNoClip()) return;
        damage(amount);
    }

    /**
     * Performs the damage operation.
     * @param amount the amount value
     */
    public void damage(float amount) {
        if (!isAlive() || amount <= 0) return;
        if (gamemode.isGodmode() || gamemode.isNoClip()) return;
        float previousHitpoints = hitpoints;
        hitpoints = Math.max(0.0f, hitpoints - amount);
        if (hitpoints < previousHitpoints) {
            onDamageTaken(amount);
        }
    }

    /**
     * Performs the on damage taken operation.
     * @param amount the amount value
     */
    protected void onDamageTaken(float amount) {
    }

    /**
     * Performs the heal operation.
     * @param amount the amount value
     */
    public void heal(float amount) {
        if (amount <= 0 || !isAlive()) return;
        this.hitpoints = Math.min(getMaxHitpoints(), this.hitpoints + amount);
    }

    /**
     * Performs the restore stamina operation.
     * @param amount the amount value
     */
    public void restoreStamina(float amount) {
        if (amount <= 0) return;
        this.stamina = Math.min(getMaxStamina(), this.stamina + amount);
    }

    /**
     * Performs the consume stamina operation.
     * @param amount the amount value
     */
    public void consumeStamina(float amount) {
        if (amount <= 0) return;
        if (gamemode.isGodmode() || gamemode.isNoClip()) return;
        this.stamina = Math.max(0.0f, this.stamina - amount);
    }

    /**
     * Updates the current state.
     */
    public void update() {
        if (!isAlive()) return;
    }

    /**
     * Returns the inventory.
     * @return the inventory
     */
    public Inventory getInventory() {
        return inventory;
    }

    /**
     * Sets the inventory.
     * @param inventory the inventory value
     */
    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    /**
     * Returns the backpack.
     * @return the backpack
     */
    public Inventory getBackpack() {
        return backpack;
    }

    /**
     * Sets the backpack.
     * @param backpack the backpack value
     */
    public void setBackpack(Inventory backpack) {
        this.backpack = backpack;
    }

    /**
     * Returns the purse.
     * @return the purse
     */
    public Purse getPurse() {
        return purse;
    }

    /**
     * Performs the purse operation.
     * @return the purse result
     */
    public int purse() {
        return purse.getBalance();
    }

    /**
     * Returns the hitpoints.
     * @return the hitpoints
     */
    public float getHitpoints() {
        return hitpoints;
    }

    /**
     * Sets the hitpoints.
     * @param hitpoints the hitpoints value
     */
    public void setHitpoints(float hitpoints) {
        this.hitpoints = hitpoints;
    }

    /**
     * Returns the max hitpoints.
     * @return the max hitpoints
     */
    public float getMaxHitpoints() {
        return maxHitpoints * level;
    }

    /**
     * Sets the max hitpoints.
     * @param maxHitpoints the max hitpoints value
     */
    public void setMaxHitpoints(int maxHitpoints) {
        this.maxHitpoints = maxHitpoints;
    }

    /**
     * Returns the stamina.
     * @return the stamina
     */
    public float getStamina() {
        return stamina;
    }

    /**
     * Sets the stamina.
     * @param stamina the stamina value
     */
    public void setStamina(float stamina) {
        this.stamina = stamina;
    }

    /**
     * Returns the max stamina.
     * @return the max stamina
     */
    public float getMaxStamina() {
        return maxStamina * level;
    }

    /**
     * Sets the max stamina.
     * @param maxStamina the max stamina value
     */
    public void setMaxStamina(float maxStamina) {
        this.maxStamina = maxStamina;
    }

    /**
     * Returns the strength.
     * @return the strength
     */
    public int getStrength() {
        return strength;
    }

    /**
     * Sets the strength.
     * @param strength the strength value
     */
    public void setStrength(int strength) {
        this.strength = strength;
    }

    /**
     * Returns the intelligence.
     * @return the intelligence
     */
    public int getIntelligence() {
        return intelligence;
    }

    /**
     * Sets the intelligence.
     * @param intelligence the intelligence value
     */
    public void setIntelligence(int intelligence) {
        this.intelligence = intelligence;
    }

    /**
     * Returns the dexterity.
     * @return the dexterity
     */
    public int getDexterity() {
        return dexterity;
    }

    /**
     * Sets the dexterity.
     * @param dexterity the dexterity value
     */
    public void setDexterity(int dexterity) {
        this.dexterity = dexterity;
    }

    /**
     * Returns the constitution.
     * @return the constitution
     */
    public int getConstitution() {
        return constitution;
    }

    /**
     * Sets the constitution.
     * @param constitution the constitution value
     */
    public void setConstitution(int constitution) {
        this.constitution = constitution;
    }

    /**
     * Returns the wisdom.
     * @return the wisdom
     */
    public int getWisdom() {
        return wisdom;
    }

    /**
     * Sets the wisdom.
     * @param wisdom the wisdom value
     */
    public void setWisdom(int wisdom) {
        this.wisdom = wisdom;
    }

    /**
     * Returns the charisma.
     * @return the charisma
     */
    public int getCharisma() {
        return charisma;
    }

    /**
     * Sets the charisma.
     * @param charisma the charisma value
     */
    public void setCharisma(int charisma) {
        this.charisma = charisma;
    }

    /**
     * Returns the luck.
     * @return the luck
     */
    public int getLuck() {
        return luck;
    }

    /**
     * Sets the luck.
     * @param luck the luck value
     */
    public void setLuck(int luck) {
        this.luck = luck;
    }

    /**
     * Returns the reputation.
     * @return the reputation
     */
    public Reputation getReputation() {
        return reputation;
    }

    /**
     * Sets the reputation.
     * @param reputation the reputation value
     */
    public void setReputation(Reputation reputation) {
        this.reputation = reputation;
    }

    /**
     * Returns the gamemode.
     * @return the gamemode
     */
    public Gamemode getGamemode() {
        return gamemode;
    }

    /**
     * Sets the gamemode.
     * @param gamemode the gamemode value
     */
    public void setGamemode(Gamemode gamemode) {
        this.gamemode = gamemode;
    }

    /**
     * Checks whether the no clip condition is met.
     * @return {@code true} if no clip; otherwise {@code false}
     */
    public boolean isNoClip() {
        return gamemode.isNoClip();
    }

    /**
     * Returns the is off ground timer.
     * @return the is off ground timer
     */
    public float getIsOffGroundTimer() {
        return isOffGroundTimer;
    }

    /**
     * Sets the is off ground timer.
     * @param isOffGroundTimer the is off ground timer value
     */
    public void setIsOffGroundTimer(float isOffGroundTimer) {
        this.isOffGroundTimer = isOffGroundTimer;
    }
}
