package com.tilled.data;

import com.tilled.input.GameInteraction;
import com.tilled.wrld.GameMaster;
import com.tilled.wrld.World;

import static com.tilled.input.GameInteraction.MAX_INTERACTION_DISTANCE;

public class Hoe extends Tool {

    public Hoe() {
        super((byte) 2, "Hoe", 150, 128);
    }

    public void use(GameMaster gameMaster, Block block) {
        super.use();
        World world = gameMaster.getWorld();
        GameInteraction interaction = gameMaster.getGameInteraction();
        Block target = world.getBlockAt(block.getX(), block.getY(), block.getZ());

        if (target == null) return;
        if (!target.getType().isTillable()) return;
        if (target.getType().equals(BlockData.TILLED_DIRT)) { return; }

        world.setBlockTypeAt(target.getX(), target.getY(),
                target.getZ(), BlockData.TILLED_DIRT.getId());

        gameMaster.getSoundService().playPlaceSound(block.getType().getSoundGroup(),
                interaction.getDistanceToBlock(gameMaster, interaction.getHoveredCell()),
                MAX_INTERACTION_DISTANCE);
    }

    @Override
    public Item copy(int newAmount) {
        Hoe hoe = new Hoe();
        hoe.addAmount(newAmount);
        return hoe;
    }
}
