package com.tilled.data;

import com.tilled.wrld.GameMaster;
import com.tilled.wrld.World;
import org.joml.Vector3i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DataClass
public class Bucket extends Tool {
    private static final Logger log = LoggerFactory.getLogger(Bucket.class);
    private BucketData type;

    public Bucket(BucketData type) {
        super(type.getId(), type.getName() + " Bucket", 1, 4);
        this.type = type;
    }

    public BucketData getType() {
        return type;
    }

    public void setType(BucketData type) {
        this.type = type;
    }

    public void use(GameMaster gameMaster, Vector3i cell) {
        super.use();
        World world = gameMaster.getWorld();
        WaterCell waterCell = world.getWaterCellAt(cell.x, cell.y, cell.z);

        if (getType().isWater()) {
            if (waterCell == null) {
                world.addWaterCell(new WaterCell(cell.x, cell.y, cell.z, 1.0f));
                setType(BucketData.EMPTY);
                gameMaster.getGameUIService().logAction(cell);
                log.info("Poured water at {}, {}", cell.x, cell.y);
            } else if (waterCell.getLevel() < 1.0f) {
                waterCell.setLevel(1.0f);
                setType(BucketData.EMPTY);
                gameMaster.getGameUIService().logAction(cell);
            }
        } else if (getType().isEmpty()) {
            if (waterCell != null) {
                world.removeWaterCell(waterCell);
                setType(BucketData.WATER);
                gameMaster.getGameUIService().logAction(cell);
                log.info("Collected water at {}, {}", cell.x, cell.y);
            }
        }
    }
}