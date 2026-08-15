package com.sfarm4j.service;

import com.sfarm4j.data.Cell;
import com.sfarm4j.data.CellType;
import com.sfarm4j.graphics.Mesh;
import com.sfarm4j.graphics.Shader;
import com.sfarm4j.graphics.Sunlight;
import com.sfarm4j.utils.K;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("all")
public class CellService implements Service<Cell> {
    private final Map<String, Cell> cells = new HashMap<>();

    public CellService() {}

    public void setCell(CellType type, int x, int z) {
        cells.put(getCellKey(x, z), new Cell(type, x, z));
    }

    public void renderAll(Shader shader, Mesh cellMesh, Matrix4f modelMatrix,
                          Sunlight sunlight) {
        for (Cell cell : cells.values()) {
            if (!cell.isUnlocked()) {
                shader.setUniform("uBaseColor", K.Colors.CELL_BLOCKED);
            } else {
                shader.setUniform("uLightDirection", sunlight.getDirection());
                shader.setUniform("uLightColor", sunlight.getColor());
                shader.setUniform("uLightIntensity", sunlight.getIntensity());
            }
            boolean isEven = (cell.getX() + cell.getZ()) % 2 == 0;
            Vector3f color = isEven ? K.Colors.CELL_EVEN : K.Colors.CELL_ODD;

            float worldX = cell.getX() * K.World.TILE_SIZE;
            float worldZ = cell.getZ() * K.World.TILE_SIZE;

            modelMatrix.identity()
                    .translate(new Vector3f(worldX, 0.0f, worldZ))
                    .scale(K.World.TILE_SIZE);

            shader.setUniform("uModel", modelMatrix);
            cellMesh.render();
        }
    }

    private Cell getCell(int x, int z) {
        return cells.get(getCellKey(x, z));
    }

    public Cell find(int x, int z) {
        return getCell(x, z);
    }

    private String getCellKey(int x, int z) {
        return x + "," + z;
    }

    public boolean isEmpty(int x, int z) {
        return getCell(x, z) == null;
    }

    public boolean isUnlocked(int x, int z) {
        Cell cell = getCell(x, z);
        return cell != null && cell.isUnlocked();
    }

    public boolean unlockCell(int x, int z) {
        Cell cell = getCell(x, z);
        if (cell == null || cell.isUnlocked()) return false;

        boolean isAdjacent = isUnlocked(x + 1, z) ||
                isUnlocked(x - 1, z) ||
                isUnlocked(x, z + 1) ||
                isUnlocked(x, z - 1);

        if (!isAdjacent) return false;
        cell.setUnlocked(true);
        return true;
    }

    public boolean expandCell(int x, int z) {
        if (cells.containsKey(getCellKey(x, z))) {
            return false;
        }

        boolean isAdjacent =
                isUnlocked(x + 1, z) ||
                        isUnlocked(x - 1, z) ||
                        isUnlocked(x, z + 1) ||
                        isUnlocked(x, z - 1);

        if (!isAdjacent) {
            return false;
        }

        Cell cell = new Cell(CellType.TILLED, x, z);
        cell.setUnlocked(true);
        cells.put(getCellKey(x, z), cell);
        return true;
    }
}