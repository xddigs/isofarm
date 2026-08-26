package com.isofarm.item;

import com.isofarm.data.DataClass;
import com.isofarm.data.Enchantment;
import com.isofarm.data.Tier;
import com.isofarm.data.ToolType;
import com.isofarm.entity.Player;

@DataClass
public abstract class Tool implements Item, Enchantable {
    private final byte id;
    private final String name;
    private final int value;

    private final ToolType type;
    private Tier tier;
    private int durability;
    private Player player;
    private final Enchantment[] enchantments;
    private final float baseDamage;

    public Tool(byte id, String name, int value,
                ToolType type, Tier tier, int durability) {
        this.id = id;
        this.name = name;
        this.value = value;
        this.type = type;
        this.tier = tier;
        this.durability = durability;
        this.baseDamage = type.getBaseDamage();
        this.enchantments = new Enchantment[3];
    }

    @Override
    public byte getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getValue() {
        return value;
    }

    public int getRow() {
        return tier.getId();
    }

    public float getBaseDamage() {
        return baseDamage;
    }

    public ToolType getType() {
        return type;
    }

    public Tier getTier() {
        return tier;
    }

    public void upgrade(Tier tier) {
        Tier[] tiers = Tier.values();
        int index = tier.ordinal();
        if (index < tiers.length - 1) {
            this.tier = tiers[index + 1];
        }
    }

    public int getDurability() {
        return durability;
    }

    public float getMaxDurability() {
        return tier.getDurability() + type.getBaseDurability();
    }

    public void setDurability(int durability) {
        this.durability = durability;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void use() {
        if (player.getGamemode().isGodmode()) return;
        if (canBeUsed()) {
            durability--;
        }
    }

    public void misuse() {
        if (player.getGamemode().isGodmode()) return;
        if (canBeUsed()) {
            durability -= 2;
        }
    }

    public void repair() {
        if (player.getGamemode().isGodmode()) return;
        durability += Math.clamp(durability,
                0, tier.getDurability());
    }

    public Enchantment[] getEnchantments() {
        return enchantments;
    }

    public void enchant(Enchantment enchantment) {
        enchantments[enchantment.getType().ordinal()] = enchantment;
    }

    public void unenchant(Enchantment enchantment) {
        enchantments[enchantment.getType().ordinal()] = null;
    }

    public boolean canBeUsed() {
        return durability > 0;
    }
}
