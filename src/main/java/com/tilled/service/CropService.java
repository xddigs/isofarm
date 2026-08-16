package com.tilled.service;

import com.tilled.data.*;
import com.tilled.wrld.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CropService implements Service<Crop> {
    private static final Logger log = LoggerFactory.getLogger(CropService.class);
    private final World world;

    public CropService(World world) {
        this.world = world;
    }

    public Crop plant(int x, int z, Player player, Block block, CropType type,
                      Season currentSeason) {
        if (block == null || block.getType() != BlockData.TILLED_DIRT) {
            log.warn("Attempted to plant {} at ({}, {}) but block is not TILLED_DIRT!",
                    type.getName(), x, z);
            return null;
        }

        if (!player.hasSeeds()) return null;

        if (block.hasCrop()) {
            Crop crop = world.getCropAt(x, z);
            if (crop != null && !crop.isReadyToHarvest()) {
                log.warn("Attempted to plant {} at ({}, {}) " +
                                "but block already has a growing crop!",
                        type.getName(), x, z);
                return null;
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

        Crop newCrop = new Crop(x, z, type, block, currentSeason);
        block.setCrop(true);
        world.addCrop(newCrop);

        log.info("Planted {} at ({}, {}) during season {}",
                type.getName(), x, z, currentSeason.getName());

        return newCrop;
    }

    public void update(float delta, WeatherType weather) {
        log.trace("Processing daily growth for {} active crops...", world.getActiveCrops().size());
        for (Crop crop : world.getActiveCrops()) {
            crop.update(delta, weather);
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
        if (crop.getBlock() != null) {
            crop.getBlock().setCrop(false);
        }
        log.info("Successfully harvested {}" +
                " giving {} items.", crop.getType().getName(), yield);
        return yield;
    }

    public void rip(Crop crop) {
        world.removeCrop(crop);
        if (crop.getBlock() != null) {
            crop.getBlock().setCrop(false);
        }
    }
}