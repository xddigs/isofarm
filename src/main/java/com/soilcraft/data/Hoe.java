package com.soilcraft.data;

import com.soilcraft.input.GameInteraction;
import com.soilcraft.utils.Settings;
import com.soilcraft.wrld.GameMaster;
import com.soilcraft.wrld.World;

public class Hoe extends Tool {
    private Tier tier;

    public Hoe(Tier tier) {
        super((byte) 2, tier.getName() + " Hoe", 150, ToolType.HOE,
                tier, tier.getDurability() + ToolType.HOE.getBaseDurability());
        this.tier = tier;
    }

    public Hoe() {
        this(Tier.COPPER);
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
        gameMaster.getItemRenderer().playPlaceAnimation();
        gameMaster.getSoundService().playPlaceSound(block.getType().getSoundGroup(),
                interaction.getDistanceToBlock(gameMaster, interaction.getHoveredCell()),
                Settings.getMaxInteractionDistance());
    }

    @Override
    public Item copy() {
        return new Hoe(tier);
    }

    public Tier getTier() {
        return tier;
    }
}
