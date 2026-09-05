package com.isofarm.pathfinding;

import com.isofarm.data.DataClass;

/**
 * Immutable value object containing grid pos.
 */
@DataClass
public record GridPos(int x, int y, int z) {}