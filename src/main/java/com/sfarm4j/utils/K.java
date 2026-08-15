package com.sfarm4j.utils;

import org.joml.Vector3f;

public final class K {
    private K() {}

    public static final class Camera {
        public static final float DEFAULT_PITCH = 35.264f;
        public static final float DEFAULT_YAW = -45.0f;
        public static final float MIN_ZOOM = 0.3f;
        public static final float MAX_ZOOM = 2.5f;
        public static final float ZOOM_FACTOR = 1.15f;
        public static final float LERP_SPEED = 12.0f;
        public static final float ORTHO_NEAR = -100.0f;
        public static final float ORTHO_FAR = 100.0f;
        public static final float DEFAULT_WIDTH = 16.0f;
        public static final float DEFAULT_HEIGHT = 8.0f;
        public static final float PAN_SENSITIVITY = 0.015f;
        public static final float ROTATION_SENSITIVITY = 0.2f;
    }

    public static final class World {
        public static final int GRID_SIZE = 2;
        public static final float TILE_SIZE = 1.0f;
        public static final float DEFAULT_BLOCK_DEPTH = 0.4f;
        public static final float EPSILON_RAY_Y = 0.00001f;
        public static final float CROP_ELEVATION_Y = 0.0f;
        public static final int SHOP_STARTING_CREDIT = 800;
        public static final int TOTAL_SLOTS = 16;
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
        public static final float LINEHEIGHT = 0.8f;
        public static final float FONT_SIZE = 20.0f;

        public static final float[] COLOR_WINDOW_BG        = {0.08f, 0.08f, 0.10f, 0.92f};
        public static final float[] COLOR_FRAME_BG         = {0.15f, 0.15f, 0.18f, 1.00f};
        public static final float[] COLOR_FRAME_BG_HOVERED = {0.20f, 0.20f, 0.24f, 1.00f};
        public static final float[] COLOR_FRAME_BG_ACTIVE  = {0.22f, 0.22f, 0.26f, 1.00f};
        public static final float[] COLOR_BUTTON         = {0.45f, 0.55f, 0.12f, 1.00f};
        public static final float[] COLOR_BUTTON_HOVERED = {0.55f, 0.68f, 0.15f, 1.00f};
        public static final float[] COLOR_BUTTON_ACTIVE  = {0.38f, 0.46f, 0.10f, 1.00f};
        public static final float[] COLOR_TEXT            = {1.00f, 1.00f, 1.00f, 1.00f};

        public static final float[] COLOR_SLOT_BG         = {0.10f, 0.10f, 0.12f, 0.40f};
        public static final float[] COLOR_SLOT_HOVERED    = {0.20f, 0.20f, 0.24f, 0.60f};
        public static final float[] COLOR_SLOT_BORDER     = {0.40f, 0.40f, 0.45f, 0.80f};
        public static final float[] COLOR_SLOT_BORDER_SEL = {0.65f, 0.80f, 0.20f, 1.00f};
    }

    public static final class UI {
        public static final int PLAYER_NAME_MAX_LENGTH = 32;

        public static final float TOOLTIP_OFFSET_X = 15.0f;
        public static final float TOOLTIP_OFFSET_Y = 15.0f;
        public static final float TOOLTIP_ITEM_SPACING_X = 4.0f;

        public static final float HUD_PADDING = 20.0f;

        public static final float INVENTORY_WIDTH = 268.0f;
        public static final float INVENTORY_HEIGHT = 300.0f;

        public static final float NEW_PLAYER_WIDTH = 340.0f;
        public static final float NEW_PLAYER_HEIGHT = 200.0f;

        public static final float ICON_SIZE = 32.0f;

        public static final float LARGE_BUTTON_HEIGHT = 40.0f;
        public static final float CENTER_PIVOT = 0.5f;
        public static final float MATCH_PARENT_WIDTH = -1.0f;
        public static final float COORD_DISPLAY_DURATION = 3.0f;

        public static final int ICON_ATLAS_FRAMES = 3;
        public static final int ICON_BLOCK_ATLAS_FRAMES = 4;
        public static final int ICON_BLOCK_ATLAS_ROWS = 3;
    }

    public static final class Paths {
        public static final String DEFAULT_VERT_SHADER = "shaders/default.vert";
        public static final String DEFAULT_FRAG_SHADER = "shaders/default.frag";
        public static final String OUTLINE_VERT_SHADER = "shaders/outline.vert";
        public static final String OUTLINE_FRAG_SHADER = "shaders/outline.frag";
        public static final String WHEAT_TEXTURE = "assets/crops/wheat_crop.png";
        public static final String CARROT_TEXTURE = "assets/crops/carrot_crop.png";
        public static final String POTATO_TEXTURE  = "assets/crops/potato_crop.png";
        public static final String FONT = "C:/Windows/Fonts/arial.ttf";
        public static final String SEED_ICONS = "assets/icons/seed_icons.png";
        public static final String CROP_ICONS = "assets/icons/crop_icons.png";
        public static final String TOOL_ICONS = "assets/icons/tool_icons.png";
        public static final String BLOCK_ICONS = "assets/icons/block_icons.png";
        public static final String BLOCKS = "assets/textures/blocks.png";
    }

    public static final class Render {
        public static final float LINE_WIDTH = 2.5f;
        public static final float SELECTION_Y_OFFSET = 0.002f;
        public static final int CROP_TOTAL_FRAMES = 5;
        public static final int PRIMARY_TEXTURE_UNIT = 0;
        public static final String GLSL_VERSION = "#version 330 core";
    }

    public static final class Colors {
        public static final Vector3f DIRT =  new Vector3f(0.4f, 0.25f, 0.1f);
        public static final Vector3f STONE = new Vector3f(0.4f, 0.4f, 0.4f);
        public static final Vector3f GRASS = new Vector3f(0.0f, 0.6f, 0.3f);

        public static final Vector3f SUNLIGHT_DEFAULT = new Vector3f(1.0f, 1.0f, 1.0f);
        public static final Vector3f OUTLINE_DEFAULT = new Vector3f(0.0f, 0.0f, 0.0f);
        public static final Vector3f CELL_BLOCKED = new Vector3f(0.8f, 0.1f, 0.04f);
    }
}