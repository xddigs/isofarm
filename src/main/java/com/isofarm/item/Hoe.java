package com.isofarm.item;

import com.isofarm.data.*;
import com.isofarm.input.GameInteraction;
import com.isofarm.service.SoundService;
import com.isofarm.wrld.GameMaster;
import com.isofarm.wrld.World;

public class Hoe extends Tool {

    public Hoe(Tier tier) {
        super((byte) 3, tier.getDisplayName() + ToolType.HOE.getDisplayName(), 150, ToolType.HOE,
                tier, tier.getDurability() + ToolType.HOE.getBaseDurability());
    }

    public Hoe() {
        this(Tier.WOODEN);
    }

    public void use(GameMaster gameMaster, Block block) {
        setPlayer(gameMaster.getPlayer());
        super.use();
        World world = gameMaster.getWorld();
        GameInteraction interaction = gameMaster.getGameInteraction();
        Block target = world.getBlockAt(block.getX(), block.getY(), block.getZ());

        if (target == null) return;
        if (!target.getType().isTillable()) return;
        if (target.getType().equals(BlockData.TILLED_DIRT)) { return; }

        world.setBlockTypeAt(target.getX(), target.getY(),
                target.getZ(), BlockData.TILLED_DIRT.getId());

        gameMaster.rebuildChunkMeshAt(target.getX(), target.getZ());
        SoundService.fx.playPlaceSound(block.getType().getSoundGroup());
    }

    @Override
    public Item copy() {
        return new Hoe(getTier());
    }

    @Override
    public boolean enchanting(Enchantment enchantment) {
        return false;
    }
}
