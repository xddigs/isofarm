package com.isofarm.item;

import com.isofarm.data.BlockData;
import com.isofarm.data.BlockPos;
import com.isofarm.data.Enchantment;
import com.isofarm.data.Usables;
import com.isofarm.entity.Player;
import com.isofarm.utils.HoveredCell;
import com.isofarm.utils.Local;
import com.isofarm.wrld.GameMaster;
import com.isofarm.wrld.WaterSimulation;
import com.isofarm.wrld.World;

public class Bucket extends Usable {
    private BlockData type;

    public Bucket(BlockData type) {
        super(Usables.BUCKET, Local.lang.t("item.usable.bucket"));
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
    public boolean use(GameMaster gameMaster, boolean isCtrlHeld) {
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

            if (!WaterSimulation.ws.removeWater(
                    targetBlock.x(),
                    targetBlock.y(),
                    targetBlock.z())) {
                return false;
            }
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

        WaterSimulation.ws.addSource(placeX, placeY, placeZ);
        empty();
        return true;
    }

    @Override
    public void update() {}

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
        setBlockType(BlockData.WATER);
        setName(Local.lang.t("item.usable.water_bucket"));
    }

    public void empty() {
        setBlockType(BlockData.AIR);
        setName(Local.lang.t("item.usable.bucket"));
    }

    public boolean isFull() {
        return type.equals(BlockData.WATER);
    }
}
