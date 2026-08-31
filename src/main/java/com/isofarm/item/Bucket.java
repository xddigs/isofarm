package com.isofarm.item;

import com.isofarm.data.*;
import com.isofarm.entity.Player;
import com.isofarm.utils.HoveredCell;
import com.isofarm.wrld.GameMaster;
import com.isofarm.wrld.World;

public class Bucket extends Usable {
    private BlockData type;

    public Bucket(BlockData type) {
        super(Usables.BUCKET, type.getName() + " Bucket");
        this.type = type;
    }

    public Bucket() {
        this(BlockData.AIR);
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

    @Override
    public Item copy() {
        return new Bucket(getBlockType());
    }

    public boolean isFull() {
        return type.equals(BlockData.WATER);
    }

    public boolean use(GameMaster gameMaster) {
        Player player = gameMaster.getPlayer();
        BlockPos hoveredCell = HoveredCell.get(gameMaster);
        World world = gameMaster.getWorld();

        byte waterID = BlockData.WATER.getId();
        byte airID = BlockData.AIR.getId();

        if (player == null || hoveredCell == null) return false;
        if (isFull()) {
            if (world.getBlockTypeAt(hoveredCell) == airID) {
                world.setWaterLevelAt(hoveredCell, (byte) 8);
                empty();
                world.setBlockTypeAt(hoveredCell, waterID);
                gameMaster.rebuildChunkMeshAt(hoveredCell.x(), hoveredCell.z());
            }
        } else {
            world.setWaterLevelAt(hoveredCell, (byte) 0);
            world.setBlockTypeAt(hoveredCell, airID);
            fill();
            gameMaster.rebuildChunkMeshAt(hoveredCell);
        }
        return true;
    }

    @Override
    public void update() {}

    @Override
    public boolean enchanting(Enchantment enchantment) {
        return false;
    }
}
