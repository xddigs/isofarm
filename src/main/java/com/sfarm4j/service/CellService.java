package com.sfarm4j.service;

import com.sfarm4j.data.Cell;
import com.sfarm4j.data.CellType;
import com.sfarm4j.graphics.Mesh;
import com.sfarm4j.graphics.Shader;
import com.sfarm4j.graphics.Sunlight;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class CellService {
    public static final float TILE_SIZE = 1.0f;
    private final Map<String, Cell> cells = new HashMap<>();
    private final Map<CellType, Vector3f> colors = new EnumMap<>(CellType.class);

    public CellService() {
        colors.put(CellType.GRASS, new Vector3f(0.2f, 0.6f, 0.2f));
        colors.put(CellType.DIRT, new Vector3f(0.45f, 0.28f, 0.12f));
        colors.put(CellType.TILLED, new Vector3f(0.3f, 0.18f, 0.08f));
    }

    public void setCell(CellType type, int x, int z) {
        cells.put(getCellKey(x, z), new Cell(type, x, z));
    }

    public void renderAll(Shader shader, Mesh cellMesh, Matrix4f modelMatrix,
                          Sunlight sunlight) {
        shader.setUniform("uLightDirection", sunlight.getDirection());
        shader.setUniform("uLightColor", sunlight.getColor());
        shader.setUniform("uLightIntensity", sunlight.getIntensity());

        for (Cell cell : cells.values()) {
            boolean isEven = (cell.getX() + cell.getZ()) % 2 == 0;
            Vector3f color = isEven ? new Vector3f(0.4f, 0.25f, 0.1f) :
                    new Vector3f(0.37f, 0.23f, 0.09f);
            shader.setUniform("uBaseColor", color);

            float worldX = cell.getX() * TILE_SIZE;
            float worldZ = cell.getZ() * TILE_SIZE;

            modelMatrix.identity()
                    .translate(new Vector3f(worldX, 0.0f, worldZ))
                    .scale(TILE_SIZE);

            shader.setUniform("uModel", modelMatrix);
            cellMesh.render();
        }
    }

    public Cell getCell(int x, int z) {
        return cells.get(getCellKey(x, z));
    }

    private String getCellKey(int x, int z) {
        return x + "," + z;
    }
}