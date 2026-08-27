package com.isofarm.utils;

import org.joml.Vector3f;
import org.joml.Vector4f;

@Utils
public final class K {

    private K() {
    }

    public static final class Camera {
        public static final float DEFAULT_PITCH = 35.264f;
        public static final float DEFAULT_YAW = -45.0f;
        public static final float SPRINT_MULTIPLIER = 1.5f;
        public static final float BOB_FREQUENCY = 10.0f;
        public static final float BOB_AMOUNT = 0.08f;
        public static final float FULL_DEGREES = 360.0f;
        public static final float HALF_DEGREES = 180.0f;
    }

    public static final class World {
        public static final float GRAVITY = -25.0f;
        public static final float JUMP_FORCE = 9.0f;

        public static final int MAP_WORLD_SIZE = 96;
        public static final float TILE_SIZE = 1.0f;
        public static final float DEFAULT_BLOCK_DEPTH = 0.4f;
        public static final float WEATHER_CHANGE_PROBABILITY = 0.01f;

        public static final int STARTING_COINS = 100;
        public static final int MAX_STACK = 64;
        public static final int WATER_LEVEL_MAX = 100;

        public static final int MAX_PARTICLES = 24;
        public static final float DEFAULT_TEXTURE_SCALE = 16.0f;
        public static final float SHORTER_BLOCK_HEIGHT = 0.9375f;
    }

    public static final class Window {
        public static final float DEFAULT_WIDTH = 1280.0f;
        public static final float DEFAULT_HEIGHT = 720.0f;
    }

    public static final class Style {
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
        public static final int INVENTORY_COLUMNS = 9;
        public static final int INVENTORY_ROWS = 4;
        public static final int INVENTORY_SLOTS = INVENTORY_COLUMNS * INVENTORY_ROWS;
        public static final float INVENTORY_BACKPACK_OFFSET = 50f;

        public static final float SQUISH_DURATION = 0.25f;

        public static final float COORD_DISPLAY_DURATION = 3.0f;

        public static final int ICON_SEED_CROPS_COLS = 4;
        public static final int ICON_BLOCK_COLS = 10;
        public static final int ICON_TOOL_COLS = 9;
        public static final int ICON_TOOL_ROWS = 7;
        public static final int ICON_HEARTS_ROWS = 4;

        public static final int ICON_MATERIAL_COLS = 12;
        public static final int ICON_MATERIAL_ROWS = 2;
        public static final int ICON_INV_COLS = 4;
        public static final int ICON_STAMINA_COLS = 10;

        public static final int DESTROY_FRAMES = 9;

        public static final float TOAST_WIDTH = 400f;
        public static final float TOAST_HEIGHT = 64.0f;
        public static final float TOAST_MARGIN_TOP = 24.0f;
        public static final float TOAST_SPACING = 8.0f;
        public static final float TOAST_SLIDE_SPEED = 8.0f;
        public static final float TOAST_EXIT_SPEED = 10.0f;
        public static final float TOAST_DURATION = 4.0f;

        public static final float TOAST_ACCENT_WIDTH = 4.0f;
        public static final float TOAST_MESSAGE_OFFSET_X = 10.0f;
        public static final float TOAST_GAP_X = 8.0f;
        public static final float TOAST_PADDING_RIGHT = 10.0f;

        public static final float HOTBAR_LABEL_DURATION = 1.75f;
        public static final float HOTBAR_LABEL_OFFSET_Y = 20.0f;
        public static final float HOTBAR_OFFSET = 20;

        public static final float CHAT_HISTORY_X = 10.0f;
        public static final float CHAT_HISTORY_OFFSET_Y = 20.0f;
        public static final float CHAT_HISTORY_LINE_HEIGHT = 20.0f;
        public static final int CHAT_HISTORY_MAX_MESSAGES = 10;
        public static final Vector4f CHAT_HISTORY_TEXT_COLOR = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);

        public static final Vector4f UI_BACKGROUND_COLOR = new Vector4f(0.25f, 0.25f, 0.25f, 1.0f);
        public static final Vector4f UI_BACKGROUND_COLOR_SLOT = new Vector4f(UI_BACKGROUND_COLOR.x,
                UI_BACKGROUND_COLOR.y, UI_BACKGROUND_COLOR.z, 0.6f);
        public static final Vector4f UI_BORDER_COLOR = new Vector4f(0.06f, 0.06f, 0.06f, 1.0f);
        public static final Vector4f UI_HOVERED_COLOR = new Vector4f(0.12f, 0.12f, 0.12f, 1.0f);
        public static final Vector4f UI_SELECTED_COLOR = new Vector4f(0.16f, 0.16f, 0.16f, 1.0f);
        public static final Vector4f UI_SELECTED_BORDER_COLOR = new Vector4f(0.0f);
        public static final Vector4f UI_ITEM_TINT = new Vector4f(1.0f);
        public static final Vector4f UI_TEXT_COLOR = new Vector4f(1.0f);
        public static final Vector4f UI_HOTBAR_SELECTED_COLOR = new Vector4f(0.35f, 0.9f, 0.35f, 1.0f);
    }

    public static final class Paths {
        public static final String FONT = "font/JetBrainsMonoNL-SemiBold.ttf";
        public static final String FONT_BOLD = "font/JetBrainsMonoNL-Bold.ttf";

        public static final String DEFAULT_VERT_SHADER = "shaders/default.vert";
        public static final String DEFAULT_FRAG_SHADER = "shaders/default.frag";

        public static final String MOTION_BLUR_VERT_SHADER = "shaders/motion_blur.vert";
        public static final String MOTION_BLUR_FRAG_SHADER = "shaders/motion_blur.frag";

        public static final String OUTLINE_VERT_SHADER = "shaders/outline.vert";
        public static final String OUTLINE_FRAG_SHADER = "shaders/outline.frag";

        public static final String UI_VERTEX_SHADER = "shaders/ui.vert";
        public static final String UI_FRAG_SHADER = "shaders/ui.frag";

        public static final String RAIN_VERT_SHADER = "shaders/rain.vert";
        public static final String RAIN_FRAG_SHADER = "shaders/rain.frag";

        public static final String SHADOW_FRAG_SHADER = "shaders/shadow.frag";
        public static final String SHADOW_VERT_SHADER = "shaders/shadow.vert";

        public static final String BLUR_VERT_SHADER = "shaders/blur.vert";
        public static final String BLUR_FRAG_SHADER = "shaders/blur.frag";

        public static final String WHEAT_TEXTURE = "assets/crops/wheat_crop.png";
        public static final String CARROT_TEXTURE = "assets/crops/carrot_crop.png";
        public static final String POTATO_TEXTURE = "assets/crops/potato_crop.png";
        public static final String BEETROOT_TEXTURE = "assets/crops/beetroot_crop.png";

        public static final String SEED_ICONS = "assets/gui/seed_icons.png";
        public static final String CROP_ICONS = "assets/gui/crop_icons.png";
        public static final String TOOL_ICONS = "assets/gui/tool_icons.png";
        public static final String BLOCK_ICONS = "assets/gui/block_icons.png";
        public static final String MATERIAL_ICONS = "assets/gui/material_icons.png";
        public static final String INVENTORY_ICONS = "assets/gui/inventory_icons.png";
        public static final String PLAYER_SPRITESHEET = "assets/sprites/gb.png";
        public static final String DESTROY_STAGES = "assets/textures/blocks/destroy.png";
        public static final String HEARTS_SPRITESHEET = "assets/gui/hearts.png";
        public static final String STAMINA_SPRITESHEET = "assets/gui/stamina.png";
    }

    public static final class Render {
        public static final float LINE_WIDTH = 2.0f;
        public static final int CROP_TOTAL_FRAMES = 5;
        public static final int PRIMARY_TEXTURE_UNIT = 0;
    }

    public static final class Colors {
        public static final Vector3f OUTLINE_DEFAULT = new Vector3f(0.0f, 0.0f, 0.0f);
        public static final Vector3f RAIN = new Vector3f(0.35f, 0.55f, 1.0f);

    }
}