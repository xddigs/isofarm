package com.isofarm.data;

@DataClass
public record BlockPos(BlockData data, int x, int y, int z) {}