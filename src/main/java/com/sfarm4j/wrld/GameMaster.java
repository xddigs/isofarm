package com.sfarm4j.wrld;

import com.sfarm4j.graphics.Texture;
import com.sfarm4j.service.CropService;
import com.sfarm4j.service.TimeService;

public class GameMaster {
    private final World world;
    private final CropService cropService;
    private final TimeService timeService;

    private Texture carrotTexture;

    public GameMaster() {
        this.world = new World();
        this.cropService = new CropService(world);
        this.timeService = new TimeService(cropService);
        this.carrotTexture = new Texture("src/main/resources/assets/carrot.png");
    }

    public void update(float delta) {
    }

    public void render() {
        carrotTexture.bind();
        carrotTexture.unbind();
    }

    public void cleanup() {
        carrotTexture.cleanup();
    }
}
