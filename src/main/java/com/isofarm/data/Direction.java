package com.isofarm.data;

@DataClass
public enum Direction {
    SOUTH_WEST, SOUTH, SOUTH_EAST, EAST, NORTH_EAST, NORTH, NORTH_WEST, WEST;

    private static final float DIAGONAL_THRESHOLD = 0.41421356f;

    public int frame() {
        return ordinal();
    }

    public static Direction fromVector(float dx, float dz) {
        float absX = Math.abs(dx);
        float absZ = Math.abs(dz);

        if (absX < 0.0001f && absZ < 0.0001f) {
            return SOUTH;
        }

        if (absZ / absX < DIAGONAL_THRESHOLD) {
            return dx > 0.0f ? EAST : WEST;
        }

        if (absX / absZ < DIAGONAL_THRESHOLD) {
            return dz > 0.0f ? SOUTH : NORTH;
        }

        if (dx > 0.0f) {
            return dz > 0.0f ? SOUTH_EAST : NORTH_EAST;
        }

        return dz > 0.0f ? SOUTH_WEST : NORTH_WEST;
    }
}