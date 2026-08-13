package com.sfarm4j.wrld;

import com.sfarm4j.service.CropService;
import com.sfarm4j.service.TimeService;

public class GameMaster {
    private final World world;
    private final CropService cropService;
    private final TimeService timeService;

    public GameMaster() {
        this.world = new World();
        this.cropService = new CropService(world);
        this.timeService = new TimeService(cropService);
    }

    public void update(float delta) {

    }

    public void render() {

    }
}
