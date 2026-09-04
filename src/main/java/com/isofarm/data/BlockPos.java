package com.isofarm.data;

/**
 * Stores block pos data.
 */
@DataClass
public record BlockPos(Blockable data, int x, int y, int z) {}