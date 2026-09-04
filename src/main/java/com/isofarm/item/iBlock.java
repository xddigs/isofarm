package com.isofarm.item;

import com.isofarm.data.BlockPos;
import com.isofarm.data.InteractiveBlocks;
import com.isofarm.graphics.gltf.GLTFModel;

/**
 * Provides iBlock behavior. Smart blocks that can be interacted with.
 * They must be craftable
 */
public class iBlock implements Craftable {
    private final InteractiveBlocks type;
    private GLTFModel blockModel;
    private int x, y, z;
    private boolean isActivated;

    public iBlock(InteractiveBlocks type, int x, int y, int z) {
        this.type = type;
        this.blockModel = new GLTFModel();
        this.x = x;
        this.y = y;
        this.z = z;
        this.isActivated = false;
    }

    public iBlock(InteractiveBlocks type, BlockPos pos) {
        this(type, pos.x(), 0, pos.y());
    }

    public iBlock(InteractiveBlocks type) {
        this.type = type;
        this.blockModel = new GLTFModel();
        this.isActivated = false;
    }

    /**
     * Returns the id of the block
     *
     * @return {@link Byte} the id of the block
     */
    @Override
    public byte getId() {
        return type.getId();
    }

    /**
     * Returns the name of the block
     *
     * @return {@link String} the name of the block
     */
    @Override
    public String getName() {
        return type.getName();
    }

    /**
     * Returns the display name of the block
     *
     * @return {@link String} the display name of the block
     */
    @Override
    public String getDisplayName() {
        return type.getDisplayName();
    }

    /**
     * Returns the value of the block
     *
     * @return {@link Integer} the value of the block
     */
    @Override
    public int getValue() {
        return type.getValue();
    }

    /**
     * Performs the copy operation.
     *
     * @return {@code this} the copy result
     */
    @Override
    public Item copy() {
        return new iBlock(getType());
    }

    /**
     * Returns the type of block
     *
     * @return {@link InteractiveBlocks} the type of block
     */
    public InteractiveBlocks getType() {
        return type;
    }

    /**
     * Returns the block model
     * @return {@link GLTFModel} the block model
     */
    public GLTFModel getBlockModel() {
        return blockModel;
    }

    /**
     * Returns if the block is activated
     *
     * @return {@link Boolean} if the block is activated
     */
    public boolean isActivated() {
        return isActivated;
    }

    /**
     * Sets the activated value
     *
     * @param isActivated the activated value
     */
    public void setActivated(boolean isActivated) {
        this.isActivated = isActivated;
    }

    /**
     * Returns the position of the block (x)
     *
     * @return {@link Integer} the position of the block (x)
     */
    public int getX() {
        return x;
    }

    /**
     * Returns the position of the block (y)
     *
     * @return {@link Integer} the position of the block (y)
     */
    public int getY() {
        return y;
    }

    /**
     * Returns the position of the block (z)
     *
     * @return {@link Integer} the position of the block (z)
     */
    public int getZ() {
        return z;
    }

    /**
     * Sets the position of the block
     *
     * @param x the position of the block (x)
     * @param y the position of the block (y)
     * @param z the position of the block (z)
     * @return {@link BlockPos} blockPos new object
     */
    public BlockPos setPosition(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return new BlockPos(type, x, y, z);
    }
}
