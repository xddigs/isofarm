package com.isofarm.data;

/**
 * Provides enchantment behavior.
 */
@DataClass
public class Enchantment {
    private final Enchantments type;
    private final String name;
    private final int cost;
    private int level;

    /**
     * Creates a new {@code Enchantment} instance.
     * @param type the type value
     * @param level the level value
     * @param cost the cost value
     */
    public Enchantment(Enchantments type, int level, int cost) {
        this.type = type;
        this.name = type.getName();
        this.level = level;
        this.cost = cost;
    }

    /**
     * Returns the type.
     * @return the type
     */
    public Enchantments getType() {
        return type;
    }

    /**
     * Returns the name.
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the level.
     * @return the level
     */
    public int getLevel() {
        return level;
    }

    /**
     * Performs the upgrade operation.
     */
    public void upgrade() {
        level++;
    }

    /**
     * Returns the cost.
     * @return the cost
     */
    public int getCost() {
        return cost;
    }
}
