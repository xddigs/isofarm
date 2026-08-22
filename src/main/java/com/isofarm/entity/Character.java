package com.isofarm.entity;

import com.isofarm.data.*;
import com.isofarm.service.ToastService;

@DataClass
public abstract class Character extends Entity implements Levelable {
    private final ToastService toastService;
    private final Inventory inventory;
    private final Purse purse;
    private Reputation reputation;
    private Gamemode gamemode;

    private int level;
    private int experience;
    private int experienceForNextLevel;

    private int hitpoints;
    private int maxHitpoints;

    private int stamina;
    private int maxStamina;

    private int strength;
    private int intelligence;
    private int dexterity;
    private int constitution;
    private int wisdom;
    private int charisma;
    private int luck;

    private float isOffGroundTimer;

    public Character(String name, ToastService toastService) {
        super(name);
        this.toastService = toastService;
        this.inventory = new Inventory();
        this.purse = new Purse(inventory, new Coin());
        this.reputation = Reputation.NEUTRAL;

        this.hitpoints = 100;
        this.maxHitpoints = 100;

        this.stamina = 100;
        this.maxStamina = 100;

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

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public int getExperience() {
        return experience;
    }

    @Override
    public void setExperience(int experience) {
        this.experience = experience;
    }

    @Override
    public int getExperienceForNextLevel() {
        return experienceForNextLevel;
    }

    @Override
    public void setExperienceForNextLevel(int experienceForNextLevel) {
        this.experienceForNextLevel = experienceForNextLevel;
    }

    @Override
    public void gain(int experience) {
        if (experience <= 0) return;
        this.experience += experience;
        while (this.experience >= experienceForNextLevel) {
            this.experience -= experienceForNextLevel;
            levelUp();

            int levelUpScaling = (int) (level * 0.8f);
            scale(levelUpScaling);
            experienceForNextLevel = calcNextLevel();
        }
    }

    @Override
    public int calcNextLevel() {
        return (int) (level * 1.2f);
    }

    @Override
    public void levelUp() {
        level++;
        toastService.success("Level up! You're now level " + level);
    }

    public void scale(int amount) {
        strength += (int) (amount * Math.random());
        intelligence += (int) (amount * Math.random());
        dexterity += (int) (amount * Math.random());
        constitution += (int) (amount * Math.random());
        wisdom += (int) (amount * Math.random());
        charisma += (int) (amount * Math.random());
    }

    public void fallDamage(float amount) {
        if (!isAlive() || amount <= 0) return;
        if (!isOnGround() && isOffGroundTimer < 3.0f) return;
        damage(amount);
    }

    public void damage(float amount) {
        if (!isAlive() || amount <= 0) return;
        this.hitpoints = (int) Math.max(0.0f, this.hitpoints - amount);
    }

    public void heal(float amount) {
        this.hitpoints = (int) Math.min(this.hitpoints, this.hitpoints + amount);
    }

    public void consumeStamina(float amount) {
        this.stamina = (int) Math.max(0.0f, this.stamina - amount);
    }

    public boolean isAlive() {
        return hitpoints > 0;
    }

    public void update() {
        if (!isAlive()) return;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public Purse getPurse() {
        return purse;
    }

    public int purse() {
        return purse.getBalance();
    }

    public int getHitpoints() {
        return hitpoints;
    }

    public void setHitpoints(int hitpoints) {
        this.hitpoints = hitpoints;
    }

    public int getMaxHitpoints() {
        return maxHitpoints;
    }

    public void setMaxHitpoints(int maxHitpoints) {
        this.maxHitpoints = maxHitpoints;
    }

    public int getStamina() {
        return stamina;
    }

    public void setStamina(int stamina) {
        this.stamina = stamina;
    }

    public int getMaxStamina() {
        return maxStamina;
    }

    public void setMaxStamina(int maxStamina) {
        this.maxStamina = maxStamina;
    }

    public int getStrength() {
        return strength;
    }

    public void setStrength(int strength) {
        this.strength = strength;
    }

    public int getIntelligence() {
        return intelligence;
    }

    public void setIntelligence(int intelligence) {
        this.intelligence = intelligence;
    }

    public int getDexterity() {
        return dexterity;
    }

    public void setDexterity(int dexterity) {
        this.dexterity = dexterity;
    }

    public int getConstitution() {
        return constitution;
    }

    public void setConstitution(int constitution) {
        this.constitution = constitution;
    }

    public int getWisdom() {
        return wisdom;
    }

    public void setWisdom(int wisdom) {
        this.wisdom = wisdom;
    }

    public int getCharisma() {
        return charisma;
    }

    public void setCharisma(int charisma) {
        this.charisma = charisma;
    }

    public int getLuck() {
        return luck;
    }

    public void setLuck(int luck) {
        this.luck = luck;
    }

    public Reputation getReputation() {
        return reputation;
    }

    public void setReputation(Reputation reputation) {
        this.reputation = reputation;
    }

    public Gamemode getGamemode() {
        return gamemode;
    }

    public void setGamemode(Gamemode gamemode) {
        this.gamemode = gamemode;
    }

    public boolean isNoClip() {
        return gamemode.isNoClip();
    }

    public float getIsOffGroundTimer() {
        return isOffGroundTimer;
    }

    public void setIsOffGroundTimer(float isOffGroundTimer) {
        this.isOffGroundTimer = isOffGroundTimer;
    }
}
