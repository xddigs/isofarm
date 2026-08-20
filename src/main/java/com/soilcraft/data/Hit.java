package com.soilcraft.data;

@DataClass
public record Hit(int x, int y, int z,
                  int normalX, int normalY, int normalZ) {}