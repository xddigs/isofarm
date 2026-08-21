package com.isofarm.paths;

import com.isofarm.data.DataClass;

@DataClass
public record Node(GridPos position, Node parent, float gCost, float hCost) {

    public float fCost() {
        return gCost + hCost;
    }
}