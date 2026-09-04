package com.isofarm.service;

import com.isofarm.data.*;
import com.isofarm.entity.Player;
import com.isofarm.item.Block;
import com.isofarm.utils.Local;
import com.isofarm.utils.ToastFactory;
import com.isofarm.wrld.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides crop service behavior.
 */
@Singleton
public class CropService implements Service<Crop> {
    public static final CropService cs = new CropService();
    private static final Logger log = LoggerFactory.getLogger(CropService.class);

    /**
     * Creates a new {@code CropService} instance.
     */
    private CropService() {}

    /**
     * Performs the plant operation.
     * @param x the x value
     * @param y the y value
     * @param z the z value
     * @param type the type value
     * @param currentSeason the current season value
     * @return the plant result
     */
    public Crop plant(int x, int y, int z, CropType type,
                      Season currentSeason) {
        Player player = Player.plyr;
        World world = World.wrld;

        if (type == null || !hasValidSubstrate(world, x, y, z, type)) {
            log.warn("Attempted to plant {} at ({}, {}, {}) on invalid terrain",
                    type == null ? "unknown crop" : type.getName(), x, y, z);
            ToastFactory.warning(type == CropType.SUGAR_CANE_CROP
                    ? "toast.sugar_cane_terrain_warn"
                    : "toast.tilled_dirt_warn");
            return null;
        }

        if (!player.hasSeeds()) {
            return null;
        }

        Crop existingCrop = world.getCropAt(x, y, z);
        if (existingCrop != null) {
            log.warn("Attempted to plant {} at ({}, {}) but a crop already exists!",
                    type.getName(), x, z);
            ToastFactory.warning("toast.crop_already_exists");
            return null;
        }

        var plantableOptional = player.getInventory().getItems().keySet().stream().filter(
                Plantable.class::isInstance).map(Plantable.class::cast).filter(
                        seed -> seed.getType() == type).findFirst();

        if (plantableOptional.isEmpty()) {
            log.warn("You don't have seeds of {}", type.getName());
            ToastFactory.error(Local.lang.f("toast.not_enough_seeds", type.getDisplayName()));
            return null;
        }

        player.remove(plantableOptional.get(), 1);
        int baseY = findColumnBase(world, x, y, z, type);
        BlockData substrate = BlockData.fromId(world.getBlockTypeAt(x, baseY, z));
        Crop newCrop = new Crop(x, y, z, type,
                new Block(substrate, x, baseY, z), currentSeason);
        world.addCrop(newCrop);
        log.info("Planted {} at ({}, {}) during season {}", type.getName(), x, z, currentSeason.getName());
        return newCrop;
    }

    /**
     * Validates the terrain rules for a crop at the requested position.
     */
    private boolean hasValidSubstrate(World world, int x, int y, int z,
                                      CropType type) {
        if (type != CropType.SUGAR_CANE_CROP) {
            return world.getBlockTypeAt(x, y, z) == BlockData.TILLED_DIRT.getId();
        }

        int baseY = findColumnBase(world, x, y, z, type);
        if (y > baseY && world.getBlockTypeAt(x, y, z) != BlockData.AIR.getId()) {
            return false;
        }
        if (world.getBlockTypeAt(x, baseY, z) != BlockData.SAND.getId()) {
            return false;
        }

        return hasAdjacentWater(world, x, baseY, z);
    }

    /** Returns the terrain level beneath a potentially stacked crop column. */
    private int findColumnBase(World world, int x, int y, int z,
                               CropType type) {
        int baseY = y;
        Crop below = world.getCropAt(x, baseY - 1, z);
        while (below != null && below.getCropType() == type) {
            baseY--;
            below = world.getCropAt(x, baseY - 1, z);
        }
        return baseY;
    }

    /** Checks the four horizontal cells beside the sugar-cane substrate. */
    private boolean hasAdjacentWater(World world, int x, int y, int z) {
        int[][] offsets = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        for (int[] offset : offsets) {
            int waterX = x + offset[0];
            int waterZ = z + offset[1];
            if (world.getBlockTypeAt(waterX, y, waterZ) == BlockData.WATER.getId()
                    || world.getWaterLevelAt(waterX, y, waterZ) > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Updates the current state.
     * @param delta the delta value
     * @param weather the weather value
     */
    public void update(float delta, WeatherType weather) {
        World.wrld.forEach(b -> {
            if (b instanceof Crop crop) {
                crop.update(delta, weather);
            }
        });
    }

    /**
     * Performs the harvest operation.
     * @param crop the crop value
     * @return the harvest result
     */
    @SuppressWarnings("UnusedReturnValue")
    public int harvest(Crop crop) {
        Player player = Player.plyr;
        if (!crop.isReadyToHarvest()) {
            log.warn("Attempted to harvest {} " +
                    "before it was fully grown.", crop.getCropType().getName());
            ToastFactory.warning("toast.crop_not_ready");
            return 1;
        }

        int yield = crop.getCropType().getYield();
        int cropValue = crop.getValue();
        int seeds = crop.getCropType().getSeeds();

        if (player.hasSpace()) {
            player.add(new Produce(crop.getCropType()), yield);
            if (!crop.getCropType().equals(CropType.SUGAR_CANE_CROP)) {
                player.add(new Seed(crop.getCropType()), seeds);
            }
        } else {
            player.addToBackpack(new Produce(crop.getCropType()), yield);
            if (!crop.getCropType().equals(CropType.SUGAR_CANE_CROP)) {
                player.addToBackpack(new Seed(crop.getCropType()), seeds);
            }
        }

        crop.setHarvested(true);
        player.gain(cropValue);
        World.wrld.removeCrop(crop);
        log.info("Successfully harvested {}" +
                " giving {} items.", crop.getCropType().getName(), yield);
        ToastFactory.success(Local.lang.f("toast.harvest", yield,
                crop.getCropType().getDisplayName()));
        return yield;
    }

    /**
     * Performs the rip operation.
     * @param crop the crop value
     */
    public void rip(Crop crop) {
        World.wrld.removeCrop(crop);
        log.info("Ripped {} from the ground.", crop.getCropType().getName());
    }
}
