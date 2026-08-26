package com.isofarm.data;

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

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public ToolType[] getApplicableTools() {
        return applicableTools;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public boolean isSingleLevel() {
        return singleLevel;
    }

    public static Enchantments fromId(int id) {
        return BY_ID[id];
    }
}