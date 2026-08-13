package com.sfarm4j.service;

import com.sfarm4j.data.Cell;
import com.sfarm4j.data.Crop;
import com.sfarm4j.data.CropType;
import com.sfarm4j.data.Season;
import com.sfarm4j.wrld.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CropService implements Service<Crop> {
    private static final Logger log = LoggerFactory.getLogger(CropService.class);
    private final World world;

    public CropService(World world) {
        this.world = world;
    }

    public Crop plant(int x, int z, Cell cell, CropType type, Season currentSeason, int value) {
        Crop crop = new Crop(x, z, type, cell, currentSeason, value);
        if (crop.getCell().hasCrop()) {
            log.warn("Attempted to plant {} at ({}, {}) " +
                    "but cell already has a crop!", type.getName(), x, z);
            return crop;
        }

        log.info("Planted {} at ({}, {}) during season {}",
                type.getName(), x, z, currentSeason.getName());
        cell.setCrop(true);
        world.addCrop(crop);
        return crop;
    }

    public void process(Season currentSeason) {
        log.info("Processing daily growth for {} active crops...",
                world.getActiveCrops().size());

        for (Crop crop : world.getActiveCrops()) {
            if (crop.getSeason() == currentSeason) {
                crop.grow();
                if (crop.isReadyToHarvest()) {
                    log.info("Crop {} is now fully grown " +
                            "and ready for harvest!", crop.getType().getName());
                }
            } else {
                log.debug("Crop {} did not grow due to season mismatch (Crop: {}, Current: {})",
                        crop.getType().getName(), crop.getSeason(), currentSeason);
            }
        }
    }

    public int harvest(Crop crop) {
        if (!crop.isReadyToHarvest()) {
            log.warn("Attempted to harvest {} " +
                    "before it was fully grown.", crop.getType().getName());
            return 0;
        }

        int yield = crop.getType().getYield();
        world.removeCrop(crop);
        log.info("Successfully harvested {}" +
                " giving {} items.", crop.getType().getName(), yield);
        return yield;
    }
}