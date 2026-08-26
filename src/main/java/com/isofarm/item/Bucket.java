package com.isofarm.item;

import com.isofarm.data.BlockData;
import com.isofarm.data.Tier;
import com.isofarm.data.ToolType;
import com.isofarm.wrld.GameMaster;

public class Bucket implements Craftable {
    private final BlockData type;
    private final ToolType tool;
    private final Tier tier;
    private final byte id;
    private final String name;
    private final int value;
    private final int durability;

    public Bucket(BlockData type, ToolType tool, Tier tier) {
        this.type = type;
        this.tool = tool;
        this.tier = tier;

        this.id = type.getId();
        this.name = tier.getName() + " " + type.getName() + tool.getName();
        this.value = type.getValue();
        this.durability = tier.getDurability() + tool.getBaseDurability();
    }

    public Bucket() {
        this(BlockData.WATER, ToolType.BUCKET, Tier.WOOD);
    }

    public BlockData getType() {
        return type;
    }

    public ToolType getTool() {
        return tool;
    }

    public Tier getTier() {
        return tier;
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

    @Override
    public Item copy() {
        return new Bucket(getType(), getTool(), getTier());
    }

    public int getDurability() {
        return durability;
    }

    public boolean isFull() {
        return type.equals(BlockData.WATER);
    }

    public int getFrame() {
        return isFull() ? 1 : 0;
    }

    public void use(GameMaster gameMaster) {

    }
}
