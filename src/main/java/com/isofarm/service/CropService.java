package com.isofarm.service;

import com.isofarm.data.*;
import com.isofarm.entity.Player;
import com.isofarm.graphics.ParticleEngine;
import com.isofarm.graphics.SpriteSheet;
import com.isofarm.item.Block;
import com.isofarm.wrld.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CropService implements Service<Crop> {
    private static final Logger log = LoggerFactory.getLogger(CropService.class);
    private final World world;
    private final ParticleEngine particles;

    public CropService(World world, ParticleEngine particles) {
        this.world = world;
        this.particles = particles;
    }

    public Crop plant(int x, int y, int z, Player player, Block block,
                      CropType type, Season currentSeason, ToastService toastService) {

        if (block == null || block.getType() != BlockData.TILLED_DIRT) {
            log.warn("Attempted to plant {} at ({}, {}) but block is not tilled!",
                    type.getName(), x, z);
            toastService.warning("You can only plant crops on tilled dirt");
            return null;
        }

        if (!player.hasSeeds()) {
            return null;
        }

        Crop existingCrop = world.getCropAt(x, y, z);
        if (existingCrop != null) {
            log.warn("Attempted to plant {} at ({}, {}) but a crop already exists!",
                    type.getName(), x, z);
            toastService.warning("A crop is already growing here!");
            return null;
        }

        var seedOpt = player.getInventory().getItems().keySet().stream().filter(
                Seed.class::isInstance).map(Seed.class::cast).filter(
                        seed -> seed.getType() == type).findFirst();

        if (seedOpt.isEmpty()) {
            log.warn("You don't have seeds of {}", type.getName());
            toastService.error("You don't have seeds of " + type.getName());
            return null;
        }

        player.remove(seedOpt.get(), 1);
        Crop newCrop = new Crop(x, y, z, type, block, currentSeason);
        world.addCrop(newCrop);
        log.info("Planted {} at ({}, {}) during season {}", type.getName(), x, z, currentSeason.getName());
        return newCrop;
    }

    public void update(float delta, WeatherType weather) {
        world.forEach(b -> {
            if (b instanceof Crop crop) {
                crop.update(delta, weather);
            }
        });
    }

    @SuppressWarnings("UnusedReturnValue")
    public int harvest(Player player, Crop crop, ToastService toastService, SpriteSheet cropSheet) {
        if (!crop.isReadyToHarvest()) {
            log.warn("Attempted to harvest {} " +
                    "before it was fully grown.", crop.getCropType().getName());
            return 1;
        }

        int yield = crop.getCropType().getYield();
        int cropValue = crop.getValue();
        int seeds = crop.getCropType().getSeeds();

        if (player.getInventory().isFull()) {
            player.addToBackpack(new Produce(crop.getCropType()), yield);
            player.addToBackpack(new Seed(crop.getCropType()), seeds);
        } else {
            player.add(new Produce(crop.getCropType()), yield);
            player.add(new Seed(crop.getCropType()), seeds);
        }

        crop.setHarvested(true);
        player.gain(cropValue);
        world.removeCrop(crop);
        log.info("Successfully harvested {}" +
                " giving {} items.", crop.getCropType().getName(), yield);
        toastService.success("You harvested " + yield + " " + crop.getCropType().getName());
        return yield;
    }

    public void rip(Crop crop) {
        world.removeCrop(crop);
        log.info("Ripped {} from the ground.", crop.getCropType().getName());
    }
}