package com.isofarm.pathfinding;

import com.isofarm.data.DataClass;

/**
 * Stores node data.
 */
@DataClass
public record Node(GridPos position, Node parent, float gCost, float hCost) {

    /**
     * Performs the f cost operation.
     * @return the f cost result
     */
    public float fCost() {
        return gCost + hCost;
    }
}