package com.isofarm.item;

import com.isofarm.data.BlockData;
import com.isofarm.data.Tier;
import com.isofarm.data.ToolType;
import com.isofarm.entity.Player;
import com.isofarm.wrld.GameMaster;

public class Bucket extends Tool {
    private final ToolType tool;
    private final Tier tier;
    private BlockData type;

    public Bucket(BlockData blockData, ToolType tool, Tier tier) {
        super((byte) 7, tier.getName() + " " + blockData.getName() + tool.getName(),
                blockData.getValue(), tool, tier, tier.getDurability() + tool.getBaseDurability());
        this.type = blockData;
        this.tool = tool;
        this.tier = tier;
    }

    public Bucket() {
        this(BlockData.AIR, ToolType.BUCKET, Tier.WOOD);
    }

    public BlockData getBlockType() {
        return type;
    }

    public ToolType getTool() {
        return tool;
    }

    public Tier getTier() {
        return tier;
    }

    @Override
    public Item copy() {
        return new Bucket(getBlockType(), getTool(), getTier());
    }

    public boolean isFull() {
        return type.equals(BlockData.WATER);
    }

    public int getFrame() {
        return !isFull() ? 2 : 1;
    }

    public void use(GameMaster gameMaster) {
        Player player = gameMaster.getPlayer();

    }
}
