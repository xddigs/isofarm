package com.tilled.utils;

import org.joml.Vector3f;

@Utils
public final class K {
    private K() {
    }

    public static final class Camera {
        public static final float DEFAULT_PITCH = 35.264f;
        public static final float DEFAULT_YAW = -45.0f;
        public static final float DEFAULT_WIDTH = 16.0f;
        public static final float DEFAULT_HEIGHT = 8.0f;
        public static final float MOVEMENT_SPEED = 6.0f;
    }

    public static final class World {
        public static final float GRAVITY = -25.0f;
        public static final float JUMP_FORCE = 8.0f;

        public static final int MAP_WORLD_SIZE = 96;
        public static final float TILE_SIZE = 1.0f;
        public static final float DEFAULT_BLOCK_DEPTH = 0.4f;
        public static final float WEATHER_CHANGE_PROBABILITY = 0.01f;

        public static final int STARTING_COINS = 100;
        public static final int TOTAL_SLOTS = 27;
        public static final int TOTAL_SLOTS_STOCK = 16;
        public static final int MAX_STACK = 9999;
        public static final int WATER_LEVEL_MAX = 100;

        public static final int RAIN_MAX_DROPS = 2000;
        public static final float RAIN_MIN_Y = 0.0f;
        public static final float RAIN_SLANT_X = 0.08f;
        public static final float RAIN_SLANT_Z = 0.08f;
        public static final float RAIN_SPAWN_RADIUS = 15.0f;
        public static final float RAIN_SPAWN_HEIGHT_OFFSET = 10.0f;
        public static final float RAIN_SPAWN_HEIGHT_VARIATION = 10.0f;
        public static final float RAIN_MIN_VELOCITY = 18.0f;
        public static final float RAIN_VELOCITY_VARIATION = 7.0f;
        public static final float RAIN_MIN_LENGTH = 0.35f;
        public static final float RAIN_LENGTH_VARIATION = 0.35f;
        public static final float RAIN_LINE_WIDTH = 1.5f;
        public static final float DEFAULT_TEXTURE_SCALE = 16.0f;
        public static final float SHORTER_BLOCK_HEIGHT = 0.9375f;
    }

    public static final class Sunlight {
        public static final Vector3f DEFAULT_DIRECTION = new Vector3f(-0.5f, -1.0f, -0.5f);
    }

    public static final class Window {
        public static final float DEFAULT_WIDTH = 1280.0f;
        public static final float DEFAULT_HEIGHT = 720.0f;
    }

    public static final class Style {
        public static final float WINDOW_ROUNDING = 12.0f;
        public static final float FRAME_ROUNDING = 8.0f;
        public static final float WINDOW_BORDER_SIZE = 0.0f;
        public static final float FRAME_BORDER_SIZE = 0.0f;

        public static final float WINDOW_PADDING_X = 16.0f;
        public static final float WINDOW_PADDING_Y = 12.0f;
        public static final float FRAME_PADDING_X = 12.0f;
        public static final float FRAME_PADDING_Y = 10.0f;
        public static final float ITEM_SPACING_X = 0.0f;
        public static final float ITEM_SPACING_Y = 14.0f;
        public static final float ITEM_SPACING = 4.0f;
        public static final float FONT_SIZE = 20.0f;

        public static final float[] COLOR_WINDOW_BG = {0.08f, 0.08f, 0.10f, 0.92f};
        public static final float[] COLOR_FRAME_BG = {0.15f, 0.15f, 0.18f, 1.00f};
        public static final float[] COLOR_FRAME_BG_HOVERED = {0.20f, 0.20f, 0.24f, 1.00f};
        public static final float[] COLOR_FRAME_BG_ACTIVE = {0.22f, 0.22f, 0.26f, 1.00f};
        public static final float[] COLOR_BUTTON = {0.45f, 0.55f, 0.12f, 1.00f};
        public static final float[] COLOR_BUTTON_HOVERED = {0.55f, 0.68f, 0.15f, 1.00f};
        public static final float[] COLOR_BUTTON_ACTIVE = {0.38f, 0.46f, 0.10f, 1.00f};
        public static final float[] COLOR_TEXT = {1.00f, 1.00f, 1.00f, 1.00f};

        public static final float[] COLOR_SLOT_BG = {0.10f, 0.10f, 0.12f, 0.40f};
        public static final float[] COLOR_SLOT_HOVERED = {0.20f, 0.20f, 0.24f, 0.60f};
        public static final float[] COLOR_SLOT_BORDER = {0.40f, 0.40f, 0.45f, 0.80f};
        public static final float[] COLOR_SLOT_BORDER_SEL = {0.65f, 0.80f, 0.20f, 1.00f};

        public static final float[] COLOR_TOAST_SUCCESS = {0.30f, 0.85f, 0.40f, 1.0f};
        public static final float[] COLOR_TOAST_SUCCESS_BG = {0.08f, 0.16f, 0.10f, 0.95f};
        public static final float[] COLOR_TOAST_INFO = {0.35f, 0.65f, 1.00f, 1.0f};
        public static final float[] COLOR_TOAST_INFO_BG = {0.07f, 0.11f, 0.18f, 0.95f};
        public static final float[] COLOR_TOAST_WARNING = {1.00f, 0.75f, 0.25f, 1.0f};
        public static final float[] COLOR_TOAST_WARNING_BG = {0.18f, 0.14f, 0.05f, 0.95f};
        public static final float[] COLOR_TOAST_ERROR = {1.00f, 0.35f, 0.35f, 1.0f};
        public static final float[] COLOR_TOAST_ERROR_BG = {0.20f, 0.07f, 0.07f, 0.95f};
        public static final float[] COLOR_TOAST_REWARD = {1.00f, 0.85f, 0.30f, 1.0f};
        public static final float[] COLOR_TOAST_REWARD_BG = {0.18f, 0.15f, 0.05f, 0.95f};
    }

    public static final class UI {
        public static final int PLAYER_NAME_MAX_LENGTH = 32;

        public static final float HUD_PADDING = 20.0f;

        public static final float INVENTORY_WIDTH = 268.0f;
        public static final float INVENTORY_HEIGHT = 300.0f;

        public static final float NEW_PLAYER_WIDTH = 340.0f;
        public static final float NEW_PLAYER_HEIGHT = 180.0f;

        public static final float INPUT_WIDTH = 340.0f;
        public static final float INPUT_HEIGHT = 120.0f;

        public static final float ICON_SIZE = 32.0f;

        public static final float LARGE_BUTTON_HEIGHT = 40.0f;
        public static final float CENTER_PIVOT = 0.5f;
        public static final float MATCH_PARENT_WIDTH = -1.0f;
        public static final float COORD_DISPLAY_DURATION = 3.0f;

        public static final int ICON_SEED_CROPS_FRAMES = 4;
        public static final int ICON_BLOCK_FRAMES = 7;
        public static final int ICON_TOOL_FRAMES = 3;

        public static final int BLOCK_ATLAS_FRAMES = 5;
        public static final int ICON_BLOCK_ATLAS_ROWS = 3;
        public static final int WATER_FRAMES = 8;
        public static final int COMMAND_MAX_LENGTH = 512;

        public static final float TOAST_WIDTH = 250.0f;
        public static final float TOAST_HEIGHT = 64.0f;
        public static final float TOAST_MARGIN_RIGHT = 24.0f;
        public static final float TOAST_MARGIN_TOP = 24.0f;
        public static final float TOAST_SPACING = 8.0f;
        public static final float TOAST_SLIDE_SPEED = 10.0f;
        public static final float TOAST_EXIT_SPEED = 12.0f;
        public static final float TOAST_DURATION = 3.0f;
        public static final float TOAST_PADDING_X = 14.0f;
        public static final float TOAST_PADDING_Y = 10.0f;
        public static final float TOAST_ICON_SIZE = 20.0f;
        public static final float TOAST_ROUNDING = 8.0f;

        public static final int HOTBAR_SLOTS = 9;

        public static final float HOTBAR_LABEL_DURATION = 1.75f;
        public static final float HOTBAR_LABEL_FADE_DURATION = 0.55f;
        public static final float HOTBAR_LABEL_PADDING_X = 10.0f;
        public static final float HOTBAR_LABEL_PADDING_Y = 5.0f;
        public static final float HOTBAR_LABEL_ROUNDING = 6.0f;
        public static final float HOTBAR_LABEL_BORDER = 1.0f;
        public static final float HOTBAR_LABEL_OFFSET_Y = 100.0f;
        public static final float CROSSHAIR_SIZE = 10.0f;
        public static final float CROSSHAIR_THICKNESS = 2.0f;
        public static final float SETTINGS_PANEL_WIDTH = 300f;
    }

    public static final class Paths {
        public static final String DEFAULT_VERT_SHADER = "shaders/default.vert";
        public static final String DEFAULT_FRAG_SHADER = "shaders/default.frag";
        public static final String OUTLINE_VERT_SHADER = "shaders/outline.vert";
        public static final String OUTLINE_FRAG_SHADER = "shaders/outline.frag";
        public static final String RAIN_VERT_SHADER = "shaders/rain.vert";
        public static final String RAIN_FRAG_SHADER = "shaders/rain.frag";

        public static final String WHEAT_TEXTURE = "assets/crops/wheat_crop.png";
        public static final String CARROT_TEXTURE = "assets/crops/carrot_crop.png";
        public static final String POTATO_TEXTURE = "assets/crops/potato_crop.png";
        public static final String BEETROOT_TEXTURE = "assets/crops/beetroot_crop.png";

        public static final String FONT = "C:/Windows/Fonts/arial.ttf";
        public static final String SEED_ICONS = "assets/icons/seed_icons.png";
        public static final String CROP_ICONS = "assets/icons/crop_icons.png";
        public static final String TOOL_ICONS = "assets/icons/tool_icons.png";
        public static final String BLOCK_ICONS = "assets/icons/block_icons.png";
        public static final String BLOCKS = "assets/textures/blocks.png";
        public static final String WATER = "assets/textures/water.png";
    }

    public static final class Render {
        public static final float LINE_WIDTH = 2.0f;
        public static final int CROP_TOTAL_FRAMES = 5;
        public static final int PRIMARY_TEXTURE_UNIT = 0;
        public static final String GLSL_VERSION = "#version 330 core";
    }

    public static final class Colors {
        public static final Vector3f SUNLIGHT_DEFAULT = new Vector3f(1.0f, 1.0f, 1.0f);
        public static final Vector3f OUTLINE_DEFAULT = new Vector3f(0.0f, 0.0f, 0.0f);
        public static final Vector3f RAIN = new Vector3f(0.35f, 0.55f, 1.0f);
        public static final float[] COLOR_HOTBAR_LABEL_BG = {0.08f, 0.08f, 0.10f, 0.90f};
        public static final float[] COLOR_HOTBAR_LABEL_BORDER = {0.65f, 0.80f, 0.20f, 0.85f};
        public static final float[] COLOR_HOTBAR_LABEL_TEXT = {1.00f, 1.00f, 1.00f, 1.00f};

    }
}