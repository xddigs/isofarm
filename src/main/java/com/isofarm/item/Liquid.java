package com.isofarm.item;

import com.isofarm.data.BlockData;
import com.isofarm.data.DataClass;

@DataClass
public abstract class Liquid extends Block {

    public Liquid(BlockData type, int x, int y, int z) {
        super(type, x, y, z);
    }

    public Liquid(BlockData type) {
        super(type);
    }
}
