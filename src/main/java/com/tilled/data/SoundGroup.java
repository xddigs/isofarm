package com.tilled.data;

public enum SoundGroup {
    GRASS(
        new String[]{"fx/steps/steps_grass_01.ogg", "fx/steps/steps_grass_02.ogg", "fx/steps/steps_grass_03.ogg", "fx/steps/steps_grass_04.ogg"},
        new String[]{"fx/blocks/dig.ogg", "fx/blocks/dirt.ogg"}
    ),
    SILENT(new String[]{}, new String[]{});

    private final String[] stepSounds;
    private final String[] breakSounds;

    SoundGroup(String[] stepSounds, String[] breakSounds) {
        this.stepSounds = stepSounds;
        this.breakSounds = breakSounds;
    }

    public String[] getStepSounds() { return stepSounds; }
    public String[] getBreakSounds() { return breakSounds; }
}