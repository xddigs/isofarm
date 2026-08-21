package com.isofarm.pathfinding;

import com.isofarm.wrld.World;

import java.util.*;

public class AStar {
    private static final float STRAIGHT_COST = 1.0f;
    private static final float UP_COST = 1.2f;
    private static final float DOWN_COST = 1.0f;

    public static List<GridPos> findPath(World world, GridPos start, GridPos goal) {
        if (start == null || goal == null) return List.of();
        if (start.equals(goal)) return List.of(start);
        if (!canStandAt(world, goal)) return List.of();

        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(Node::fCost));
        Map<GridPos, Node> nodes = new HashMap<>();
        Set<GridPos> closedSet = new HashSet<>();

        Node startNode = new Node(start, null, 0.0f, heuristic(start, goal));
        openSet.add(startNode);
        nodes.put(start, startNode);

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            if (closedSet.contains(current.position())) continue;
            if (current.position().equals(goal)) return reconstructPath(current);

            closedSet.add(current.position());
            for (Neighbor neighbor : getNeighbors(world, current.position())) {
                GridPos position = neighbor.position();
                if (closedSet.contains(position)) {
                    continue;
                }

                float tentativeG = current.gCost() + neighbor.cost();
                Node existing = nodes.get(position);
                if (existing == null || tentativeG < existing.gCost()) {
                    Node next = new Node(position, current, tentativeG, heuristic(position, goal));
                    nodes.put(position, next);
                    openSet.add(next);
                }
            }
        }

        return List.of();
    }

    private static List<Neighbor> getNeighbors(World world, GridPos current) {
        List<Neighbor> neighbors = new ArrayList<>(4);
        addHorizontalNeighbor(world, neighbors, current, 1, 0);
        addHorizontalNeighbor(world, neighbors, current, -1, 0);
        addHorizontalNeighbor(world, neighbors, current, 0, 1);
        addHorizontalNeighbor(world, neighbors, current, 0, -1);
        return neighbors;
    }

    private static void addHorizontalNeighbor(World world, List<Neighbor> neighbors,
                                              GridPos current, int dx, int dz) {
        int x = current.x() + dx;
        int z = current.z() + dz;
        GridPos sameLevel = new GridPos(x, current.y(), z);

        if (canStandAt(world, sameLevel)) {
            neighbors.add(new Neighbor(sameLevel, STRAIGHT_COST));

            return;
        }

        GridPos up = new GridPos(x, current.y() + 1, z);
        if (canStandAt(world, up)) {
            neighbors.add(new Neighbor(up, UP_COST));

            return;
        }

        GridPos down = new GridPos(x, current.y() - 1, z);
        if (canStandAt(world, down)) {
            neighbors.add(new Neighbor(down, DOWN_COST));
        }
    }

    private static boolean canStandAt(World world, GridPos position) {
        int x = position.x();
        int y = position.y();
        int z = position.z();

        if (y < 0) {
            return false;
        }

        byte feet = world.getBlockTypeAt(x, y, z);
        byte head = world.getBlockTypeAt(x, y + 1, z);
        byte ground = world.getBlockTypeAt(x, y - 1, z);
        return feet == 0 && head == 0 && ground != 0;
    }

    private static float heuristic(GridPos a, GridPos b) {
        return Math.abs(a.x() - b.x()) + Math.abs(a.y() - b.y()) + Math.abs(a.z() - b.z());
    }

    private static List<GridPos> reconstructPath(Node node) {
        LinkedList<GridPos> path = new LinkedList<>();

        Node current = node;

        while (current != null) {
            path.addFirst(current.position());
            current = current.parent();
        }

        return path;
    }

    private record Node(GridPos position, Node parent, float gCost, float hCost) {
        float fCost() {
            return gCost + hCost;
        }
    }

    private record Neighbor(GridPos position, float cost) {}
}