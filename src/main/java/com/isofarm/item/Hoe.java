package com.isofarm.item;

import com.isofarm.data.*;
import com.isofarm.input.GameInteraction;
import com.isofarm.utils.HoveredCell;
import com.isofarm.utils.Settings;
import com.isofarm.wrld.GameMaster;
import com.isofarm.wrld.World;

public class Hoe extends Tool {

    public Hoe(Tier tier) {
        super((byte) 2, tier.getName() + " Hoe", 150, ToolType.HOE,
                tier, tier.getDurability() + ToolType.HOE.getBaseDurability());
    }

    public Hoe() {
        this(Tier.WOOD);
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
        gameMaster.getItemRenderer().playPlaceAnimation();
        gameMaster.getSoundService().playPlaceSound(block.getType().getSoundGroup(),
                interaction.getDistanceToBlock(gameMaster, HoveredCell.get(gameMaster)),
                Settings.getMaxInteractionDistance());
    }

    @Override
    public Item copy() {
        return new Hoe(getTier());
    }
}
