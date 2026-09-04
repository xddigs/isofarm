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

/**
 * Provides bucket behavior.
 */
public class Bucket extends Usable {
    private BlockData type;

    /**
     * Creates a new {@code Bucket} instance.
     * @param type the type value
     */
    public Bucket(BlockData type) {
        super(Usables.BUCKET, Local.lang.t("item.usable.bucket"));
        this.type = type;
    }

    /**
     * Creates a new {@code Bucket} instance.
     */
    public Bucket() {
        this(BlockData.AIR);
    }

    /**
     * Performs the copy operation.
     * @return the copy result
     */
    @Override
    public Item copy() {
        return new Bucket(getBlockType());
    }

    /**
     * Performs the use operation.
     * @param gameMaster the game master value
     * @param isCtrlHeld the is ctrl held value
     * @return the use result
     */
    @Override
    public boolean use(GameMaster gameMaster, boolean isCtrlHeld) {
        Player player = Player.plyr;
        if (player == null) {
            return false;
        }

        if (!isFull()) {
            BlockPos targetBlock = HoveredCell.get(gameMaster, false);
            if (targetBlock == null) {
                return false;
            }

            if (World.wrld.getBlockTypeAt(targetBlock) != BlockData.WATER.getId()) {
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

    /**
     * Updates the current state.
     */
    @Override
    public void update() {}

    /**
     * Performs the enchanting operation.
     * @param enchantment the enchantment value
     * @return the enchanting result
     */
    @Override
    public boolean enchanting(Enchantment enchantment) {
        return false;
    }

    /**
     * Returns the block type.
     * @return the block type
     */
    public BlockData getBlockType() {
        return type;
    }

    /**
     * Sets the block type.
     * @param type the type value
     */
    public void setBlockType(BlockData type) {
        this.type = type;
    }

    /**
     * Performs the fill operation.
     */
    public void fill() {
        setBlockType(BlockData.WATER);
        setName(Local.lang.t("item.usable.water_bucket"));
    }

    /**
     * Performs the empty operation.
     */
    public void empty() {
        setBlockType(BlockData.AIR);
        setName(Local.lang.t("item.usable.bucket"));
    }

    /**
     * Checks whether the full condition is met.
     * @return {@code true} if full; otherwise {@code false}
     */
    public boolean isFull() {
        return type.equals(BlockData.WATER);
    }
}
