package com.isofarm.item;

import com.isofarm.data.BlockData;
import com.isofarm.data.BlockPos;
import com.isofarm.data.Enchantment;
import com.isofarm.data.Usables;
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

    @Override
    public Item copy() {
        return new Bucket(getBlockType());
    }

    @Override
    public boolean use(GameMaster gameMaster) {
        Player player = gameMaster.getPlayer();
        World world = gameMaster.getWorld();

        if (player == null) {
            return false;
        }

        if (!isFull()) {
            BlockPos targetBlock = HoveredCell.get(gameMaster, false);
            if (targetBlock == null) {
                return false;
            }

            if (world.getBlockTypeAt(targetBlock) != BlockData.WATER.getId()) {
                return false;
            }

            gameMaster.getWaterSimulation().remove(targetBlock.x(), targetBlock.y(), targetBlock.z());
            gameMaster.rebuildChunkMeshAt(targetBlock.x(), targetBlock.z());
            fill();
            return true;
        }

        BlockPos targetBlock = HoveredCell.get(gameMaster, false);
        if (targetBlock == null) {
            return false;
        }

        int normalX = gameMaster.getOrthoCamera().getLastHitNormalX();
        int normalY = gameMaster.getOrthoCamera().getLastHitNormalY();
        int normalZ = gameMaster.getOrthoCamera().getLastHitNormalZ();

        int placeX = targetBlock.x() + normalX;
        int placeY = targetBlock.y() + normalY;
        int placeZ = targetBlock.z() + normalZ;

        if (world.getBlockTypeAt(placeX, placeY, placeZ) != BlockData.AIR.getId()) {
            return false;
        }

        gameMaster.getWaterSimulation().addSource(placeX, placeY, placeZ);
        empty();
        return true;
    }

    @Override
    public void update() {
    }

    @Override
    public boolean enchanting(Enchantment enchantment) {
        return false;
    }

    public BlockData getBlockType() {
        return type;
    }

    public void setBlockType(BlockData type) {
        this.type = type;
    }

    public void fill() {
        this.type = BlockData.WATER;
        setName(type.getName() + " Bucket");
    }

    public void empty() {
        this.type = BlockData.AIR;
        setName("Bucket");
    }

    public boolean isFull() {
        return type.equals(BlockData.WATER);
    }
}
