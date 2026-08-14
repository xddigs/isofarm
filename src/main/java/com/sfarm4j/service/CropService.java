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
        if (!player.hasSeeds()) return null;
        if (cell.hasCrop()) {
            Crop crop = world.getCropAt(x, z);
            if (crop != null) {
                if (!crop.isReadyToHarvest()) {
                    log.warn("Attempted to plant {} at ({}, {}) " +
                                    "but cell already has a growing crop!",
                            type.getName(), x, z);
                    return null;
                }
            }
        }

        var seedOpt = player.getInventory().getItems().keySet().stream()
                .filter(Seed.class::isInstance)
                .map(Seed.class::cast)
                .filter(seed -> seed.getType() == type)
                .findFirst();

        if (seedOpt.isEmpty()) {
            log.warn("You don't have seeds of {}", type.getName());
            return null;
        }
        player.remove(seedOpt.get(), 1);

        Crop newCrop = new Crop(x, z, type, cell, currentSeason);
        cell.setCrop(true);
        world.addCrop(newCrop);

        log.info("Planted {} at ({}, {}) during season {}",
                type.getName(), x, z, currentSeason.getName());

        return newCrop;
    }

    public void update(float delta) {
        log.trace("Processing daily growth for {} active crops...", world.getActiveCrops().size());
        for (Crop crop : world.getActiveCrops()) {
            crop.update(delta);
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    public int harvest(Player player, Crop crop) {
        if (!crop.isReadyToHarvest()) {
            log.warn("Attempted to harvest {} " +
                    "before it was fully grown.", crop.getType().getName());
            return 1;
        }

        int yield = crop.getType().getYield();
        int cropValue = crop.getValue();
        int seeds = crop.getType().getSeeds();
        player.add(crop, yield);
        player.add(new Seed(crop.getType()), seeds);
        crop.setHarvested(true);
        player.gain(cropValue);
        world.removeCrop(crop);
        crop.getCell().setCrop(false);
        log.info("Successfully harvested {}" +
                " giving {} items.", crop.getType().getName(), yield);
        return yield;
    }

    public void rip(Crop crop) {
        world.removeCrop(crop);
        crop.getCell().setCrop(false);
        log.info("Ripped {} from the ground.", crop.getType().getName());
    }
}