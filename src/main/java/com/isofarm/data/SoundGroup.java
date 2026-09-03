package com.isofarm.data;

/**
 * Enumerates the supported sound group values.
 */
public enum SoundGroup {
    SOIL(
        new String[]{"fx/steps/steps_grass_01.ogg", "fx/steps/steps_grass_02.ogg", "fx/steps/steps_grass_03.ogg", "fx/steps/steps_grass_04.ogg"},
        new String[]{"fx/blocks/dig.ogg", "fx/blocks/dirt.ogg"},
        new String[]{"fx/blocks/dirt.ogg"}, new String[]{}, new String[]{}, new String[]{"fx/blocks/dirt.ogg"}, new String[]{}),
    SNOW(
        new String[]{"fx/steps/steps_snow_01.ogg", "fx/steps/steps_snow_02.ogg", "fx/steps/steps_snow_03.ogg", "fx/steps/steps_snow_04.ogg"},
        new String[]{"fx/blocks/snow_break.ogg"},
        new String[]{"fx/blocks/snow_place.ogg"}, new String[]{}, new String[]{}, new String[]{}, new String[]{}),
    HARD(
        new String[]{},
        new String[]{"fx/blocks/wood.ogg"}, new String[]{}, new String[]{}, new String[]{},
        new String[]{"fx/blocks/mining.ogg", "fx/blocks/chop.ogg"}, new String[]{}),
    GLASS(new String[]{}, new String[]{"fx/blocks/fragile_break.ogg", "fx/blocks/fragile_break2.ogg"}, new String[]{}, new String[]{}, new String[]{}, new String[]{}, new String[]{}),
    ITEMS(new String[]{}, new String[]{"fx/items/broke.ogg"}, new String[]{}, new String[]{"fx/items/drop.ogg", "fx/items/pickup.ogg"}, new String[]{}, new String[]{}, new String[]{"fx/items/equip.ogg"}),
    BOOKS(new String[]{}, new String[]{}, new String[]{}, new String[]{}, new String[]{}, new String[]{}, new String[]{"fx/items/page.ogg"}),
    ENTITY(new String[]{}, new String[]{"fx/entity/fall.ogg"}, new String[]{}, new String[]{"fx/entity/hurt.ogg"}, new String[]{}, new String[]{}, new String[]{}),
    RAIN(new String[]{}, new String[]{}, new String[]{}, new String[]{}, new String[]{"fx/ambient/rain.ogg"}, new String[]{}, new String[]{}),
    NATURE(new String[]{}, new String[]{}, new String[]{}, new String[]{}, new String[]{"fx/ambient/nature.ogg"}, new String[]{}, new String[]{}),
    WATER(
        new String[]{"fx/steps/steps_water_01.ogg", "fx/steps/steps_water_02.ogg", "fx/steps/steps_water_03.ogg", "fx/steps/steps_water_04.ogg"},
        new String[]{}, new String[]{}, new String[]{}, new String[]{}, new String[]{}, new String[]{}),
    SILENT(new String[]{}, new String[]{}, new String[]{}, new String[]{}, new String[]{}, new String[]{}, new String[]{});

    private final String[] stepSounds;
    private final String[] breakSounds;
    private final String[] placeSounds;
    private final String[] entitySounds;
    private final String[] backgroundSounds;
    private final String[] loopingSounds;
    private final String[] useSounds;

    /**
     * Creates a new {@code SoundGroup} instance.
     * @param stepSounds the step sounds value
     * @param breakSounds the break sounds value
     * @param placeSounds the place sounds value
     * @param entitySounds the entity sounds value
     * @param backgroundSounds the background sounds value
     * @param loopingSounds the looping sounds value
     * @param useSounds the use sounds value
     */
    SoundGroup(String[] stepSounds, String[] breakSounds, String[] placeSounds,
               String[] entitySounds, String[] backgroundSounds, String[] loopingSounds, String[] useSounds) {
        this.stepSounds = stepSounds;
        this.breakSounds = breakSounds;
        this.placeSounds = placeSounds;
        this.entitySounds = entitySounds;
        this.backgroundSounds = backgroundSounds;
        this.loopingSounds = loopingSounds;
        this.useSounds = useSounds;
    }

    /**
     * Returns the step sounds.
     * @return the step sounds
     */
    public String[] getStepSounds() { return stepSounds; }
    /**
     * Returns the break sounds.
     * @return the break sounds
     */
    public String[] getBreakSounds() { return breakSounds; }
    /**
     * Returns the place sounds.
     * @return the place sounds
     */
    public String[] getPlaceSounds() { return placeSounds; }
    /**
     * Returns the entity sounds.
     * @return the entity sounds
     */
    public String[] getEntitySounds() { return entitySounds; }
    /**
     * Returns the background sounds.
     * @return the background sounds
     */
    public String[] getBackgroundSounds() { return backgroundSounds; }
    /**
     * Returns the looping sounds.
     * @return the looping sounds
     */
    public String[] getLoopingSounds() { return loopingSounds; }
    /**
     * Returns the use sounds.
     * @return the use sounds
     */
    public String[] getUseSounds() { return useSounds; }
}