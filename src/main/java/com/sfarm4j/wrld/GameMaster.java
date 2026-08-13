package com.sfarm4j.wrld;

import com.sfarm4j.service.CropService;

public class GameMaster {
    private final World world;
    private final CropService cropService;

    public GameMaster() {
        this.world = new World();
        this.cropService = new CropService(world);
    }

    public void update(float delta) {

    }

    public void render() {

    }
}
