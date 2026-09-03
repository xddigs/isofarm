package com.isofarm.item;

import com.isofarm.data.DataClass;

import java.util.Objects;

@DataClass
public interface Item {
    byte getId();
    String getName();
    String getDisplayName();
    int getValue();
    Item copy();
}