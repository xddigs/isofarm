package com.isofarm.pathfinding;

import com.isofarm.data.DataClass;

/**
 * Stores grid pos data.
 */
@DataClass
public record GridPos(int x, int y, int z) {}