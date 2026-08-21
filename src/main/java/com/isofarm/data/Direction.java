package com.isofarm.data;

@DataClass
public enum Direction {
    SOUTH_WEST, SOUTH, SOUTH_EAST, EAST, NORTH_EAST, NORTH, NORTH_WEST, WEST;

    public int frame() {
        return ordinal();
    }

    public static Direction fromVector(float dx, float dz, float cameraYaw) {
        if (Math.abs(dx) < 0.0001f && Math.abs(dz) < 0.0001f) {
            return SOUTH;
        }

        double angleRad = Math.atan2(dz, dx);
        double worldDegrees = Math.toDegrees(angleRad);
        if (worldDegrees < 0) {
            worldDegrees += 360.0;
        }

        double screenDegrees = (worldDegrees - cameraYaw) % 360.0;
        if (screenDegrees < 0) {
            screenDegrees += 360.0;
        }

        int index = (int) Math.floor((screenDegrees + 22.5) / 45.0) % 8;
        return switch (index) {
            case 0 -> EAST;
            case 1 -> SOUTH_EAST;
            case 2 -> SOUTH;
            case 3 -> SOUTH_WEST;
            case 4 -> WEST;
            case 5 -> NORTH_WEST;
            case 6 -> NORTH;
            case 7 -> NORTH_EAST;
            default -> SOUTH;
        };
    }
}