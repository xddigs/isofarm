package com.isofarm.item;

import com.isofarm.data.BlockData;
import com.isofarm.data.Enchantment;
import com.isofarm.data.Tier;
import com.isofarm.data.ToolType;
import com.isofarm.entity.Player;
import com.isofarm.wrld.GameMaster;

public class Bucket extends Tool {
    private final Tier tier;
    private BlockData type;

    public Bucket(BlockData blockData, Tier tier) {
        super((byte) 7, tier.getName() + " " + blockData.getName() + ToolType.BUCKET.getName(),
                blockData.getValue(), ToolType.BUCKET, tier, tier.getDurability() + ToolType.BUCKET.getBaseDurability());
        this.type = blockData;
        this.tier = tier;
    }

    public Bucket() {
        this(BlockData.AIR, Tier.WOOD);
    }

    public BlockData getBlockType() {
        return type;
    }

    public Tier getTier() {
        return tier;
    }

    @Override
    public Item copy() {
        return new Bucket(getBlockType(), getTier());
    }

    public boolean isFull() {
        return type.equals(BlockData.WATER);
    }

    public int getFrame() {
        return type.equals(BlockData.WATER) ? 1 : 0;
    }

    public void use(GameMaster gameMaster) {
        Player player = gameMaster.getPlayer();

    }

    @Override
    public boolean enchanting(Enchantment enchantment) {
        return false;
    }
}
