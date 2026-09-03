package com.isofarm.data;

/**
 * Stores block pos data.
 */
@DataClass
public record BlockPos(BlockData data, int x, int y, int z) {}