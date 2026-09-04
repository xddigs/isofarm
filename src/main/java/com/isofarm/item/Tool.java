package com.isofarm.item;

import com.isofarm.data.DataClass;
import com.isofarm.data.Enchantment;
import com.isofarm.data.Tier;
import com.isofarm.data.ToolType;
import com.isofarm.entity.Player;
import com.isofarm.utils.Local;

/**
 * Provides tool behavior.
 */
@DataClass
public abstract class Tool implements Item,
        Enchantable, Equippable {
    private final byte id;
    private final String name;
    private final int value;

    private final ToolType type;
    private Tier tier;
    private int durability;
    private final Enchantment[] enchantments;
    private final float baseDamage;

    /**
     * Creates a new {@code Tool} instance.
     * @param id the id value
     * @param name the name value
     * @param value the value value
     * @param type the type value
     * @param tier the tier value
     * @param durability the durability value
     */
    public Tool(byte id, String name, int value,
                ToolType type, Tier tier, int durability) {
        this.id = id;
        this.name = name;
        this.value = value;
        this.type = type;
        this.tier = tier;
        this.durability = durability;
        this.baseDamage = type.getBaseDamage();
        this.enchantments = new Enchantment[4];
    }

    /**
     * Returns the id.
     * @return the id
     */
    @Override
    public byte getId() {
        return id;
    }

    /**
     * Returns the name.
     * @return the name
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Returns the display name.
     * @return the display name
     */
    @Override
    public String getDisplayName() {
        return Local.lang.item(type.getDisplayName(),
                tier == Tier.NONE ? null : tier.getDisplayName()
        );
    }

    /**
     * Returns the value.
     * @return the value
     */
    @Override
    public int getValue() {
        return value;
    }

    /**
     * Returns the col.
     * @return the col
     */
    public int getCol() {
        return getId();
    }

    /**
     * Returns the row.
     * @return the row
     */
    public int getRow() {
        return tier.getId();
    }

    /**
     * Returns the base damage.
     * @return the base damage
     */
    public float getBaseDamage() {
        return baseDamage;
    }

    /**
     * Returns the type.
     * @return the type
     */
    public ToolType getType() {
        return type;
    }

    /**
     * Returns the tier.
     * @return the tier
     */
    public Tier getTier() {
        return tier;
    }

    /**
     * Performs the upgrade operation.
     * @param tier the tier value
     */
    public void upgrade(Tier tier) {
        Tier[] tiers = Tier.values();
        int index = tier.ordinal();
        if (index < tiers.length - 1) {
            this.tier = tiers[index + 1];
        }
    }

    /**
     * Returns the durability.
     * @return the durability
     */
    public int getDurability() {
        return durability;
    }

    /**
     * Returns the max durability.
     * @return the max durability
     */
    public float getMaxDurability() {
        return tier.getDurability() + type.getBaseDurability();
    }

    /**
     * Sets the durability.
     * @param durability the durability value
     */
    public void setDurability(int durability) {
        this.durability = durability;
    }

    /**
     * Performs the use operation.
     */
    public void use() {
        if (Player.plyr.getGamemode().isGodmode()) return;
        if (canBeUsed()) {
            durability--;
        }
    }

    /**
     * Performs the misuse operation.
     */
    public void misuse() {
        if (Player.plyr.getGamemode().isGodmode()) return;
        if (canBeUsed()) {
            durability -= 2;
        }
    }

    /**
     * Performs the repair operation.
     */
    public void repair() {
        if (Player.plyr.getGamemode().isGodmode()) return;
        durability += Math.clamp(durability,
                0, tier.getDurability());
    }

    /**
     * Returns the enchantments.
     * @return the enchantments
     */
    public Enchantment[] getEnchantments() {
        return enchantments;
    }

    /**
     * Performs the enchant operation.
     * @param enchantment the enchantment value
     */
    public void enchant(Enchantment enchantment) {
        enchantments[enchantment.getType().ordinal()] = enchantment;
    }

    /**
     * Performs the unenchant operation.
     * @param enchantment the enchantment value
     */
    public void unenchant(Enchantment enchantment) {
        enchantments[enchantment.getType().ordinal()] = null;
    }

    /**
     * Checks whether the be used condition is met.
     * @return {@code true} if be used; otherwise {@code false}
     */
    public boolean canBeUsed() {
        return durability > 0;
    }
}
