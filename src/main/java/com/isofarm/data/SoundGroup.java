package com.isofarm.data;

public enum SoundGroup {
    SOIL(
        new String[]{"fx/steps/steps_grass_01.ogg", "fx/steps/steps_grass_02.ogg", "fx/steps/steps_grass_03.ogg", "fx/steps/steps_grass_04.ogg"},
        new String[]{"fx/blocks/dig.ogg", "fx/blocks/dirt.ogg"},
        new String[]{"fx/blocks/dirt.ogg"}, new String[]{}),
    HARD(new String[]{}, new String[]{"fx/blocks/wood.ogg"}, new String[]{}, new String[]{}),
    GLASS(new String[]{}, new String[]{"fx/blocks/fragile_break.ogg", "fx/blocks/fragile_break2.ogg"}, new String[]{}, new String[]{}),
    ITEMS(new String[]{}, new String[]{"fx/items/broke.ogg"}, new String[]{}, new String[]{"fx/items/drop.ogg", "fx/items/pickup.ogg"}),
    SILENT(new String[]{}, new String[]{}, new String[]{}, new String[]{});

    private final String[] stepSounds;
    private final String[] breakSounds;
    private final String[] placeSounds;
    private final String[] useSounds;

    SoundGroup(String[] stepSounds, String[] breakSounds, String[] placeSounds, String[] useSounds) {
        this.stepSounds = stepSounds;
        this.breakSounds = breakSounds;
        this.placeSounds = placeSounds;
        this.useSounds = useSounds;
    }

    public String[] getStepSounds() { return stepSounds; }
    public String[] getBreakSounds() { return breakSounds; }
    public String[] getPlaceSounds() { return placeSounds; }
    public String[] getUseSounds() { return useSounds; }
}