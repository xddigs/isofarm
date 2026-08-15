package com.sfarm4j.data;

@DataClass
public class BlockItem extends Item {

    public BlockItem(byte id, String name, int value) {
        super(id, name, 1, value);
    }
}
