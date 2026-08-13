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
    }

    public static final class Window {
        public static final float DEFAULT_WIDTH = 1280.0f;
        public static final float DEFAULT_HEIGHT = 720.0f;
    }

    public static final class Paths {
        public static final String DEFAULT_VERT_SHADER = "shaders/default.vert";
        public static final String DEFAULT_FRAG_SHADER = "shaders/default.frag";
        public static final String OUTLINE_VERT_SHADER = "shaders/outline.vert";
        public static final String OUTLINE_FRAG_SHADER = "shaders/outline.frag";
        public static final String WHEAT_TEXTURE = "assets/crops/wheat_crop.png";
        public static final String FONT = "C:/Windows/Fonts/arial.ttf";
    }

    public static final class Render {
        public static final float LINE_WIDTH = 2.5f;
        public static final float SELECTION_Y_OFFSET = 0.002f;
        public static final int WHEAT_TOTAL_FRAMES = 5;
        public static final String GLSL_VERSION = "#version 330 core";
    }

    public static final class Colors {
        public static final Vector3f CELL_EVEN = new Vector3f(0.4f, 0.25f, 0.1f);
        public static final Vector3f CELL_ODD = new Vector3f(0.37f, 0.23f, 0.09f);
        public static final Vector3f WATERED = new Vector3f(0.45f, 0.28f, 0.24f);
        public static final Vector3f DIRT = new Vector3f(0.45f, 0.28f, 0.12f);
        public static final Vector3f TILLED = new Vector3f(0.3f, 0.18f, 0.08f);
        public static final Vector3f SUNLIGHT_DEFAULT = new Vector3f(1.0f, 1.0f, 1.0f);
        public static final Vector3f OUTLINE_DEFAULT = new Vector3f(0.0f, 0.0f, 0.0f);
    }
}