package com.sfarm4j.service;

import com.sfarm4j.data.Crop;
import com.sfarm4j.data.CropType;
import com.sfarm4j.data.Season;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CropService {
    private static final Logger log = LoggerFactory.getLogger(CropService.class);
    private final List<Crop> activeCrops = new ArrayList<>();

    public Crop plant(CropType type, Season currentSeason, int value) {
        Crop crop = new Crop(type, currentSeason, value);
        activeCrops.add(crop);
        log.info("Planted {} during season {}", type.getName(), currentSeason);
        return crop;
    }

    public void process(Season currentSeason) {
        log.info("Processing daily growth for {} active crops...", activeCrops.size());
        
        for (Crop crop : activeCrops) {
            if (crop.getSeason() == currentSeason) {
                crop.grow();
                if (crop.isReadyToHarvest()) {
                    log.info("Crop {} is now fully grown and ready for harvest!", crop.getType().getName());
                }
            } else {
                log.debug("Crop {} did not grow because season mismatch (Crop: {}, Current: {})",
                        crop.getType().getName(), crop.getSeason(), currentSeason);
            }
        }
    }

    public int harvest(Crop crop) {
        if (!crop.isReadyToHarvest()) {
            log.warn("Attempted to harvest {} before it was fully grown.", crop.getType().getName());
            return 0;
        }

        int yield = crop.getType().getYield();
        activeCrops.remove(crop);
        log.info("Successfully harvested {} giving {} items.", crop.getType().getName(), yield);
        return yield;
    }

    public List<Crop> getActiveCrops() {
        return Collections.unmodifiableList(activeCrops);
    }
}