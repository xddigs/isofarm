package com.tilled.data;

public enum SoundGroup {
    SOIL(
        new String[]{"fx/steps/steps_grass_01.ogg", "fx/steps/steps_grass_02.ogg", "fx/steps/steps_grass_03.ogg", "fx/steps/steps_grass_04.ogg"},
        new String[]{"fx/blocks/dig.ogg", "fx/blocks/dirt.ogg"},
        new String[]{"fx/blocks/dirt.ogg"}),
    SILENT(new String[]{}, new String[]{}, new String[]{});

    private final String[] stepSounds;
    private final String[] breakSounds;
    private final String[] placeSounds;

    SoundGroup(String[] stepSounds, String[] breakSounds, String[] placeSounds) {
        this.stepSounds = stepSounds;
        this.breakSounds = breakSounds;
        this.placeSounds = placeSounds;
    }

    public String[] getStepSounds() { return stepSounds; }
    public String[] getBreakSounds() { return breakSounds; }
    public String[] getPlaceSounds() { return placeSounds; }
}