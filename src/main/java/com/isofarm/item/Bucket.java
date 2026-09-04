package com.isofarm.item;

import com.isofarm.data.BlockData;
import com.isofarm.data.BlockPos;
import com.isofarm.data.Enchantment;
import com.isofarm.data.Usables;
import com.isofarm.entity.Player;
import com.isofarm.utils.HoveredCell;
import com.isofarm.utils.Local;
import com.isofarm.wrld.GameMaster;
import com.isofarm.wrld.FluidSimulation;
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

            BlockData fluidType = BlockData.fromId(World.wrld.getBlockTypeAt(targetBlock));
            FluidSimulation simulation = FluidSimulation.forBlock(fluidType);
            if (simulation == null) {
                return false;
            }

            if (!simulation.isSource(
                    targetBlock.x(), targetBlock.y(), targetBlock.z())) {
                return false;
            }

            if (!simulation.removeFluid(
                    targetBlock.x(),
                    targetBlock.y(),
                    targetBlock.z())) {
                return false;
            }
            fill(fluidType);
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

        FluidSimulation simulation = FluidSimulation.forBlock(type);
        if (simulation == null || !simulation.addSource(placeX, placeY, placeZ)) {
            return false;
        }
        empty();
        return true;
    }

    /**
     * Updates the current state.
     */
    @Override
    public void update() {}

    @Override
    public String getName() {
        return getBucketDisplayName();
    }

    /**
     * Returns the localized name for the bucket's current contents.
     * @return {@link String} the localized display name
     */
    @Override
    public String getDisplayName() {
        return getBucketDisplayName();
    }

    /**
     * Returns the localized name for the bucket's current contents.
     * @return {@link String} the localized display name
     */
    private String getBucketDisplayName() {
        return switch (type) {
            case WATER -> Local.lang.t("item.usable.water_bucket");
            case LAVA -> Local.lang.t("item.usable.lava_bucket");
            case null, default -> Local.lang.t("item.usable.bucket");
        };
    }

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
     * Fills the bucket with a supported fluid.
     * @param fluidType the fluid block type
     */
    public void fill(BlockData fluidType) {
        FluidSimulation simulation = FluidSimulation.forBlock(fluidType);
        if (simulation != null) setBlockType(simulation.getFluidType());
    }

    /**
     * Performs the empty operation.
     */
    public void empty() {
        setBlockType(BlockData.AIR);
    }

    /**
     * Checks whether the full condition is met.
     * @return {@code true} if full; otherwise {@code false}
     */
    public boolean isFull() {
        return type != null && type.isFluid();
    }
}
