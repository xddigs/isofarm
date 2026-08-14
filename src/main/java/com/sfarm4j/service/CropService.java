package com.sfarm4j.service;

import com.sfarm4j.data.*;
import com.sfarm4j.wrld.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CropService implements Service<Crop> {
    private static final Logger log = LoggerFactory.getLogger(CropService.class);
    private final World world;

    public CropService(World world) {
        this.world = world;
    }

    public Crop plant(int x, int z, Player player, Cell cell, CropType type,
                      Season currentSeason) {
        if (cell.hasCrop()) {
            Crop crop = world.getCropAt(x, z);
            if (crop != null) {
                if (!crop.isReadyToHarvest()) {
                    log.warn("Attempted to plant {} at ({}, {}) but cell already has an growing crop!",
                            type.getName(), x, z);
                    return null;
                }

                if (crop.isReadyToHarvest()) {
                    harvest(player, crop);
                }
            }
        }

        Crop newCrop = new Crop(x, z, type, cell, currentSeason);
        cell.setCrop(true);
        world.addCrop(newCrop);

        log.info("Planted {} at ({}, {}) during season {}",
                type.getName(), x, z, currentSeason.getName());

        return newCrop;
    }

    public void process(Season currentSeason) {
        log.info("Processing daily growth for {} active crops...", world.getActiveCrops().size());

        for (Crop crop : world.getActiveCrops()) {
            float multiplier = (crop.getSeason() == currentSeason)
                    ? (float) currentSeason.getValueMultiplier() : 1.0f;
            crop.grow(multiplier);
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    public int harvest(Player player, Crop crop) {
        if (!crop.isReadyToHarvest()) {
            log.warn("Attempted to harvest {} " +
                    "before it was fully grown.", crop.getType().getName());
            return 0;
        }

        int yield = crop.getType().getYield();
        player.add(crop, yield);
        world.removeCrop(crop);
        log.info("Successfully harvested {}" +
                " giving {} items.", crop.getType().getName(), yield);
        return yield;
    }

    public void rip(Crop crop) {
        world.removeCrop(crop);
        log.info("Ripped {} from the ground.", crop.getType().getName());
    }
}