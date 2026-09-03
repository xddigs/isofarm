package com.isofarm.data;

/**
 * Enumerates the supported enchantments values.
 */
public enum Enchantments {
    EFFICIENCY(0, "Efficiency", "Efficient ", "Break blocks faster", new ToolType[]{ToolType.HOE, ToolType.AXE, ToolType.PICKAXE, ToolType.SHOVEL}, 3, false),
    DURABILITY(1, "Durability", "Long Lasting ", "Reduces durability loss", new ToolType[]{ToolType.HOE, ToolType.AXE , ToolType.PICKAXE, ToolType.SHOVEL, ToolType.SWORD}, 3, false),
    FORTUNE(2, "Fortune", "Fortunate ", "Gives a chance for extra drops", new ToolType[]{ToolType.HOE, ToolType.AXE, ToolType.PICKAXE, ToolType.SHOVEL}, 3, false),
    SHARPNESS(3, "Sharpness", "Sharp ", "Deals more damage to enemies", new ToolType[]{ToolType.SWORD, ToolType.AXE}, 3, false),
    HARVEST(4, "Harvest", "Harvesting ", "Increases crop yield when harvesting", new ToolType[]{ToolType.HOE}, 2, false),
    SWIFT(5, "Swift", "Swifted ", "Increases movement speed while equipped", new ToolType[]{ToolType.HOE, ToolType.AXE, ToolType.PICKAXE, ToolType.SHOVEL, ToolType.SWORD}, 2, false),
    REPAIR(6, "Repair", "Repairing ", "Restores a small amount of durability over time", new ToolType[]{ToolType.HOE, ToolType.AXE, ToolType.PICKAXE, ToolType.SHOVEL, ToolType.SWORD}, 1, true),
    LUCKY(7, "Lucky", "Lucky ", "Gives a small chance to avoid consuming durability", new ToolType[]{ToolType.HOE, ToolType.AXE, ToolType.PICKAXE, ToolType.SHOVEL, ToolType.SWORD}, 1, true);

    private static final Enchantments[] BY_ID;
    static {
        BY_ID = new Enchantments[values().length];
        for (Enchantments enchantment : values()) {
            BY_ID[enchantment.getId()] = enchantment;
        }
    }

    private final int id;
    private final String name;
    private final String displayName;
    private final String description;
    private final ToolType[] applicableTools;
    private final int maxLevel;
    private final boolean singleLevel;

    /**
     * Creates a new {@code Enchantments} instance.
     * @param id the id value
     * @param name the name value
     * @param displayName the display name value
     * @param description the description value
     * @param applicableTools the applicable tools value
     * @param maxLevel the max level value
     * @param singleLevel the single level value
     */
    Enchantments(int id, String name, String displayName, String description,
                 ToolType[] applicableTools, int maxLevel, boolean singleLevel) {
        this.id = id;
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.applicableTools = applicableTools;
        this.maxLevel = maxLevel;
        this.singleLevel = singleLevel;
    }

    /**
     * Returns the id.
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the name.
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the display name.
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the description.
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the applicable tools.
     * @return the applicable tools
     */
    public ToolType[] getApplicableTools() {
        return applicableTools;
    }

    /**
     * Returns the max level.
     * @return the max level
     */
    public int getMaxLevel() {
        return maxLevel;
    }

    /**
     * Checks whether the single level condition is met.
     * @return {@code true} if single level; otherwise {@code false}
     */
    public boolean isSingleLevel() {
        return singleLevel;
    }

    /**
     * Performs the from id operation.
     * @param id the id value
     * @return the from id result
     */
    public static Enchantments fromId(int id) {
        return BY_ID[id];
    }
}