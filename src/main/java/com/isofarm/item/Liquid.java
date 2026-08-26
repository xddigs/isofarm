package com.isofarm.item;

import com.isofarm.data.BlockData;

public abstract class Liquid extends Block {

    public Liquid(BlockData type, int x, int y, int z) {
        super(type, x, y, z);
    }

    public Liquid(BlockData type) {
        super(type);
    }
}
