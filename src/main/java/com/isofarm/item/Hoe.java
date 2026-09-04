package com.isofarm.item;

import com.isofarm.data.BlockData;
import com.isofarm.data.Enchantment;
import com.isofarm.data.Tier;
import com.isofarm.data.ToolType;
import com.isofarm.service.SoundService;
import com.isofarm.wrld.GameMaster;
import com.isofarm.wrld.World;

/**
 * Provides hoe behavior.
 */
public class Hoe extends Tool {

    /**
     * Creates a new {@code Hoe} instance.
     * @param tier the tier value
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
     * Performs the use operation.
     * @param gameMaster the game master value
     * @param block the block value
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
     * Performs the copy operation.
     * @return the copy result
     */
    @Override
    public Item copy() {
        return new Hoe(getTier());
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
}
