package com.tilled.data;

@DataClass
public class UpgradeItem extends Item {

    public UpgradeItem(byte id, String name, int value) {
        super(id, name, 1, value);
    }
}
