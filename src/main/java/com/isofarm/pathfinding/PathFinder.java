package com.isofarm.pathfinding;

import com.isofarm.entity.Player;
import com.isofarm.wrld.World;

import java.util.List;

/**
 * Provides path finder behavior.
 */
public final class PathFinder {

    /**
     * Creates a new {@code PathFinder} instance.
     */
    private PathFinder() {}

    /**
     * Finds and returns the path.
     * @param world the world value
     * @param start the start value
     * @param goal the goal value
     * @return the located path
     */
    public static List<GridPos> findPath(World world, GridPos start, GridPos goal) {
        return AStar.findPath(world, start, goal);
    }

    /**
     * Returns the player grid position.
     * @return the player grid position
     */
    public static GridPos getPlayerGridPosition() {
        Player player = Player.plyr;
        return new GridPos(
                (int) Math.floor(player.getPosition().x),
                (int) Math.floor(player.getPosition().y),
                (int) Math.floor(player.getPosition().z)
        );
    }

    /**
     * Returns the walkable position.
     * @param world the world value
     * @param x the x value
     * @param z the z value
     * @return the walkable position
     */
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

    /**
     * Checks whether the stand condition is met.
     * @param world the world value
     * @param x the x value
     * @param y the y value
     * @param z the z value
     * @return {@code true} if stand; otherwise {@code false}
     */
    private static boolean canStand(World world, int x, int y, int z) {
        return world.isBlockSolid(x, y - 1, z)
                && !world.isBlockSolid(x, y, z)
                && !world.isBlockSolid(x, y + 1, z);
    }
}
