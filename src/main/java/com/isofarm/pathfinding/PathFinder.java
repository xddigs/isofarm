package com.isofarm.pathfinding;

import com.isofarm.entity.Player;
import com.isofarm.wrld.World;

import java.util.List;

public final class PathFinder {

    private PathFinder() {}

    public static List<GridPos> findPath(World world, Player player,
                                         GridPos start, GridPos goal) {
        return AStar.findPath(world, start, goal);
    }

    public static GridPos getPlayerGridPosition(Player player) {
        return new GridPos(
                (int) Math.floor(player.getPosition().x),
                (int) Math.floor(player.getPosition().y),
                (int) Math.floor(player.getPosition().z)
        );
    }

    public static GridPos getWalkablePosition(World world, int x, int z) {
        GridPos highestY = world.getHighestY(
                x + 0.5f,
                z + 0.5f
        );

        int y = highestY.y();
        if (!canStand(world, x, y, z)) {
            return null;
        }

        return new GridPos(x, y, z);
    }

    private static boolean canStand(World world, int x, int y, int z) {
        return world.isBlockSolid(x, y - 1, z)
                && !world.isBlockSolid(x, y, z)
                && !world.isBlockSolid(x, y + 1, z);
    }
}