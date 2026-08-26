package com.isofarm.data;

@DataClass
public class Enchantment {
    private final Enchantments type;
    private final String name;
    private final int cost;
    private int level;

    public Enchantment(Enchantments type, int level, int cost) {
        this.type = type;
        this.name = type.getName();
        this.level = level;
        this.cost = cost;
    }

    public Enchantments getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }

    public void upgrade() {
        level++;
    }

    public int getCost() {
        return cost;
    }
}
