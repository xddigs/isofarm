package com.isofarm.item;

import com.isofarm.data.*;
import com.isofarm.entity.Player;
import com.isofarm.utils.HoveredCell;
import com.isofarm.wrld.GameMaster;
import com.isofarm.wrld.World;

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

    public void fill() {
        this.type = BlockData.WATER;
    }

    public void empty() {
        this.type = BlockData.AIR;
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
        return type.equals(BlockData.WATER) ? 8 : 7;
    }

    public void use(GameMaster gameMaster) {
        Player player = gameMaster.getPlayer();
        BlockPos hoveredCell = HoveredCell.get(gameMaster);
        World world = gameMaster.getWorld();

        if (player == null || hoveredCell == null) return;
        byte waterLevel = world.getWaterLevelAt(hoveredCell);
        if (isFull()) {
            if (waterLevel == 0 && world.getBlockTypeAt(hoveredCell) == BlockData.AIR.getId()) {
                world.setWaterLevelAt(hoveredCell, (byte) 8);
                empty();
                gameMaster.rebuildChunkMeshAt(hoveredCell.x(), hoveredCell.z());
            }
        } else {
            if (waterLevel > 0) {
                world.setWaterLevelAt(hoveredCell, (byte) 0);
                fill();
                gameMaster.rebuildChunkMeshAt(hoveredCell);
            }
        }
    }

    @Override
    public boolean enchanting(Enchantment enchantment) {
        return false;
    }
}
