package com.isofarm.data;

public enum SoundGroup {
    SOIL(
        new String[]{"fx/steps/steps_grass_01.ogg", "fx/steps/steps_grass_02.ogg", "fx/steps/steps_grass_03.ogg", "fx/steps/steps_grass_04.ogg"},
        new String[]{"fx/blocks/dig.ogg", "fx/blocks/dirt.ogg"},
        new String[]{"fx/blocks/dirt.ogg"}, new String[]{}, new String[]{}, new String[]{"fx/blocks/dirt.ogg"}),
    SNOW(
        new String[]{"fx/steps/steps_snow_01.ogg", "fx/steps/steps_snow_02.ogg", "fx/steps/steps_snow_03.ogg", "fx/steps/steps_snow_04.ogg"},
        new String[]{"fx/blocks/snow_break.ogg"},
        new String[]{"fx/blocks/snow_place.ogg"}, new String[]{}, new String[]{}, new String[]{}),
    HARD(
        new String[]{},
        new String[]{"fx/blocks/wood.ogg"}, new String[]{}, new String[]{}, new String[]{},
        new String[]{"fx/blocks/mining.ogg", "fx/blocks/chop.ogg"}),
    GLASS(new String[]{}, new String[]{"fx/blocks/fragile_break.ogg", "fx/blocks/fragile_break2.ogg"}, new String[]{}, new String[]{}, new String[]{}, new String[]{}),
    ITEMS(new String[]{}, new String[]{"fx/items/broke.ogg"}, new String[]{}, new String[]{"fx/items/drop.ogg", "fx/items/pickup.ogg"}, new String[]{}, new String[]{}),
    ENTITY(new String[]{}, new String[]{}, new String[]{}, new String[]{"fx/entity/hurt.ogg"}, new String[]{}, new String[]{}),
    RAIN(new String[]{}, new String[]{}, new String[]{}, new String[]{}, new String[]{"fx/ambient/rain.ogg"}, new String[]{}),
    NATURE(new String[]{}, new String[]{}, new String[]{}, new String[]{}, new String[]{"fx/ambient/nature.ogg"}, new String[]{}),
    SILENT(new String[]{}, new String[]{}, new String[]{}, new String[]{}, new String[]{}, new String[]{});

    private final String[] stepSounds;
    private final String[] breakSounds;
    private final String[] placeSounds;
    private final String[] entitySounds;
    private final String[] backgroundSounds;
    private final String[] loopingSounds;

    SoundGroup(String[] stepSounds, String[] breakSounds, String[] placeSounds,
               String[] entitySounds, String[] backgroundSounds, String[] loopingSounds) {
        this.stepSounds = stepSounds;
        this.breakSounds = breakSounds;
        this.placeSounds = placeSounds;
        this.entitySounds = entitySounds;
        this.backgroundSounds = backgroundSounds;
        this.loopingSounds = loopingSounds;
    }

    public String[] getStepSounds() { return stepSounds; }
    public String[] getBreakSounds() { return breakSounds; }
    public String[] getPlaceSounds() { return placeSounds; }
    public String[] getEntitySounds() { return entitySounds; }
    public String[] getBackgroundSounds() { return backgroundSounds; }
    public String[] getLoopingSounds() { return loopingSounds; }
}