package com.isofarm.service;

import com.isofarm.data.BlockData;
import com.isofarm.data.TreeSapling;
import com.isofarm.utils.Settings;
import com.isofarm.wrld.World;
import com.isofarm.wrld.WorldGenerator;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TreeService {
    private final World world;
    private final List<TreeSapling> saplings = new ArrayList<>();
    private final Random random = new Random();

    public TreeService(World world) {
        this.world = world;
    }

    public void plantSapling(int x, int y, int z, BlockData saplingBlock) {
        world.setBlockTypeAt(x, y, z, saplingBlock.getId());
        saplings.add(new TreeSapling(x, y, z, saplingBlock, (int) Settings.getTicks()));
    }

    public void update(float delta) {
        for (int i = saplings.size() - 1; i >= 0; i--) {
            TreeSapling sapling = saplings.get(i);
            
            if (sapling.tick()) {
                growTree(sapling);
                saplings.remove(i);
            }
        }
    }

    private void growTree(TreeSapling sapling) {
        int x = sapling.getX();
        int y = sapling.getY();
        int z = sapling.getZ();

        world.setBlockTypeAt(x, y, z, BlockData.AIR.getId());
        WorldGenerator.generateTree(x, z, random);
    }
}