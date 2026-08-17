package com.tilled.data;

public enum StepSoundGroup {
    GRASS("fx/steps/steps_grass_01.ogg", "fx/steps/steps_grass_02.ogg",
            "fx/steps/steps_grass_03.ogg", "fx/steps/steps_grass_04.ogg"),

    SILENT(null, null, null, null),;

    private final String sound1;
    private final String sound2;
    private final String sound3;
    private final String sound4;

    StepSoundGroup(String sound1, String sound2, String sound3, String sound4) {
        this.sound1 = sound1;
        this.sound2 = sound2;
        this.sound3 = sound3;
        this.sound4 = sound4;
    }

    public String sound1() {
        return sound1;
    }

    public String sound2() {
        return sound2;
    }

    public String sound3() {
        return sound3;
    }

    public String sound4() {
        return sound4;
    }
}