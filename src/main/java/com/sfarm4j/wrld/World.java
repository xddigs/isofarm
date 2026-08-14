package com.sfarm4j.wrld;

import com.sfarm4j.data.Crop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class World {
    private final List<Crop> crops = new ArrayList<>();

    public void addCrop(Crop crop) {
        crops.add(crop);
    }

    public void removeCrop(Crop crop) {
        crops.remove(crop);
    }

    public List<Crop> getActiveCrops() {
        return Collections.unmodifiableList(crops);
    }

    public Crop getCropAt(int x, int z) {
        for (int cz = 0; cz < crops.size(); cz++) {
            for (Crop crop : crops) {
                if (crop.getX() == x &&
                    crop.getZ() == z) {
                    return crop;
                }
            }
        }
        return null;
    }
}