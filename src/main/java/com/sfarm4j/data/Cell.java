package com.sfarm4j.data;

@DataClass
public record Cell(CellType type, int x, int z) {}
