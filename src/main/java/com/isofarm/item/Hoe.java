package com.isofarm.item;

import com.isofarm.data.BlockData;
import com.isofarm.data.Enchantment;
import com.isofarm.data.Tier;
import com.isofarm.data.ToolType;
import com.isofarm.service.SoundService;
import com.isofarm.wrld.GameMaster;
import com.isofarm.wrld.World;

/**
 * Encapsulates the state and operations required by hoe within the game runtime.
 */
public class Hoe extends Tool {

    /**
     * Creates a new {@code Hoe} instance.
     * @param tier the {@link Tier} supplied as {@code tier}
     */
    public Hoe(Tier tier) {
        super((byte) 3, ToolType.HOE.getName(), 150, ToolType.HOE,
                tier, tier.getDurability() + ToolType.HOE.getBaseDurability());
    }

    /**
     * Creates a new {@code Hoe} instance.
     */
    public Hoe() {
        this(Tier.WOODEN);
    }

    /**
     * Handles use and applies its effect to the current interaction state.
     * @param gameMaster the {@link GameMaster} supplied as {@code gameMaster}
     * @param block the {@link Block} supplied as {@code block}
     */
    public void use(GameMaster gameMaster, Block block) {
        super.use();
        World world = gameMaster.getWorld();
        Block target = world.getBlockAt(block.getX(), block.getY(), block.getZ());

        if (target == null) return;
        if (!target.getType().isTillable()) return;
        if (target.getType().equals(BlockData.TILLED_DIRT)) { return; }

        world.setBlockTypeAt(target.getX(), target.getY(),
                target.getZ(), BlockData.TILLED_DIRT.getId());

        gameMaster.rebuildChunkMeshAt(target.getX(), target.getZ());
        SoundService.fx.playPlaceSound(block.getType().getSoundGroup());
    }

    /**
     * {@inheritDoc}
     * Creates an independent copy that preserves the relevant state of this object.
     * @return the {@link Item} representing the copy result
     */
    @Override
    public Item copy() {
        return new Hoe(getTier());
    }

    /**
     * {@inheritDoc}
     * Applies enchanting and updates the affected character or item state.
     * @param enchantment the {@link Enchantment} supplied as {@code enchantment}
     * @return {@code boolean}; the enchanting result
     */
    @Override
    public boolean enchanting(Enchantment enchantment) {
        return false;
    }
}
