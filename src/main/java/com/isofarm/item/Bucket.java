package com.isofarm.item;

import com.isofarm.data.*;
import com.isofarm.entity.Player;
import com.isofarm.utils.HoveredCell;
import com.isofarm.wrld.GameMaster;
import com.isofarm.wrld.World;

public class Bucket extends Usable {
    private BlockData type;

    public Bucket(BlockData type) {
        super(Usables.BUCKET, (type.equals(BlockData.AIR)) ? "Bucket" : type.getName() + " Bucket");
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

    @Override
    public boolean use(GameMaster gameMaster) {
        Player player = gameMaster.getPlayer();
        BlockPos targetBlock = HoveredCell.get(gameMaster);
        World world = gameMaster.getWorld();

        if (player == null || targetBlock == null) return false;
        if (isFull()) {
            BlockPos placePos = HoveredCell.get(gameMaster);
            if (placePos != null && world.getBlockTypeAt(placePos) == BlockData.AIR.getId()) {
                gameMaster.getWaterSimulation().addSource(placePos.x(), placePos.y(), placePos.z());
                empty();
                setName("Bucket");
                return true;
            }
        } else {
            if (world.getBlockTypeAt(targetBlock) == BlockData.WATER.getId()) {
                gameMaster.getWaterSimulation().remove(targetBlock.x(), targetBlock.y(), targetBlock.z());
                fill();
                setName(type.getName() + " Bucket");
                return true;
            }
        }
        return false;
    }

    @Override
    public void update() {}

    @Override
    public boolean enchanting(Enchantment enchantment) {
        return false;
    }

    public boolean isFull() {
        return type.equals(BlockData.WATER);
    }
}
