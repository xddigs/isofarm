package com.isofarm.item;

import com.isofarm.data.DataClass;

import java.util.Objects;

/**
 * Defines the item contract.
 */
@DataClass
public interface Item {
    /**
     * Returns the id.
     * @return the id
     */
    byte getId();
    /**
     * Returns the name.
     * @return the name
     */
    String getName();
    /**
     * Returns the display name.
     * @return the display name
     */
    String getDisplayName();
    /**
     * Returns the value.
     * @return the value
     */
    int getValue();
    /**
     * Performs the copy operation.
     * @return the copy result
     */
    Item copy();
}