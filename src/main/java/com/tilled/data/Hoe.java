package com.tilled.data;

import com.tilled.wrld.World;

public class Hoe extends Tool {

    public Hoe() {
        super((byte) 2, "Hoe", 150, 128);
    }

    public void use(World world, Block block) {
        super.use();

        Block target = world.getBlockAt(
                block.getX(),
                block.getY(),
                block.getZ()
        );

        if (target == null) return;
        if (!target.getType().isTillable()) return;

        world.setBlockTypeAt(
                target.getX(),
                target.getY(),
                target.getZ(),
                BlockData.TILLED_DIRT.getId()
        );
    }
}
