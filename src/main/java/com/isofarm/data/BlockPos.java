package com.isofarm.data;

/**
 * Immutable value object containing block pos.
 */
@DataClass
public record BlockPos(Blockable data, int x, int y, int z) {}