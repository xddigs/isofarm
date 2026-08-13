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
public class CellService {
    private final Map<String, Cell> cells = new HashMap<>();
    private final Map<CellType, Vector3f> colors = new EnumMap<>(CellType.class);

    public CellService() {
        colors.put(CellType.WATERED, K.Colors.WATERED);
        colors.put(CellType.DIRT, K.Colors.DIRT);
        colors.put(CellType.TILLED, K.Colors.TILLED);
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
            Vector3f color = isEven ? K.Colors.CELL_EVEN : K.Colors.CELL_ODD;
            shader.setUniform("uBaseColor", color);

            float worldX = cell.getX() * K.World.TILE_SIZE;
            float worldZ = cell.getZ() * K.World.TILE_SIZE;

            modelMatrix.identity()
                    .translate(new Vector3f(worldX, 0.0f, worldZ))
                    .scale(K.World.TILE_SIZE);

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