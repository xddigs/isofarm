package com.isofarm.data;

/**
 * Stores block node data.
 */
@DataClass
public record BlockNode(int x, int y, int z, int distance) {}