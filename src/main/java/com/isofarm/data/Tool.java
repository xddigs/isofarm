package com.isofarm.data;

import com.isofarm.entity.Player;

@DataClass
public abstract class Tool extends Item {
    private final ToolType type;
    private Tier tier;
    private int durability;
    private Player player;

    public Tool(byte id, String name, int value,
                ToolType type, Tier tier, int durability) {
        super(id, name, value);
        this.type = type;
        this.tier = tier;
        this.durability = durability;
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
            durability *= -2;
        }
    }

    public void repair() {
        if (player.getGamemode().isGodmode()) return;
        durability += Math.clamp(durability,
                0, tier.getDurability());
    }

    public boolean canBeUsed() {
        return durability > 0;
    }
}
