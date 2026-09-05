package com.isofarm.data;

/**
 * Immutable value object containing chunk pos.
 */
@DataClass
public record ChunkPos(int x, int z) {}
