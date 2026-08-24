package com.isofarm.graphics;

import com.isofarm.gui.GUI;
import com.isofarm.gui.UIFont;
import com.isofarm.gui.UIManager;
import com.isofarm.gui.UIProgressBar;
import com.isofarm.input.Keyboard;
import com.isofarm.service.TimeService;
import com.isofarm.utils.K;
import com.isofarm.utils.Settings;
import com.isofarm.wrld.GameMaster;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Intro {
    private final long window;
    private GameMaster gameMaster;
    private UIProgressBar progressBar;
    private UIManager uiManager;

    private static final float CLEAR_COLOR_ALPHA = 1.0f;
    private int framebufferWidth;
    private int framebufferHeight;

    private boolean fullscreen = false;

    private int windowedX;
    private int windowedY;
    private int windowedWidth;
    private int windowedHeight;

    public Intro(long window) {
        this.window = window;
    }

    public void setupUI() {
        updateFramebufferSize();
        float barWidth = 500f;
        float barHeight = 25f;

        float x = (framebufferWidth - barWidth) / 2.0f;
        float y = (framebufferHeight - barHeight) / 2.0f;

        Vector4f foreground = new Vector4f(0.0f, 0.90f, 0.4f, 1.0f);
        Vector4f background = new Vector4f(0.2f, 0.2f, 0.2f, 1.0f);
        progressBar = new UIProgressBar(x, y, barWidth, barHeight, 0.0f, 100.0f, false);
        uiManager = new UIManager(framebufferWidth, framebufferHeight);
        progressBar.setColors(foreground, background);
        progressBar.show();
        uiManager.getRoot().show();
        uiManager.getRoot().addChild(progressBar);
        uiManager.resize(framebufferWidth, framebufferHeight);
    }

    public void show() {
        setupUI();
        toggleFullscreen();
        gameMaster = new GameMaster(window, uiManager);
        updateFramebufferSize();
        gameMaster.onResize(framebufferWidth, framebufferHeight);
        setupCallbacks();

        int r = Settings.getRenderDistance();
        int totalChunks = (2 * r + 1) * (2 * r + 1);
        int resourceSteps = 10;
        int postProcessingSteps = 2;
        int totalTasks = resourceSteps + (totalChunks * 2) + postProcessingSteps;

        final int[] completedTasks = {0};

        gameMaster.loadResources(progress -> {
            completedTasks[0]++;
            float overallProgress = ((float) completedTasks[0] / totalTasks) * 100.0f;
            progressBar.setValue(overallProgress);
            renderLoadingFrame("Loading engine resources...");
        });

        int minZ = -r;
        int currentChunkX = -r, currentChunkZ = minZ;
        while (!glfwWindowShouldClose(window) && completedTasks[0] < (resourceSteps + totalChunks)) {
            glfwPollEvents();

            gameMaster.getChunkManager().getGenerator()
                    .generateChunk(currentChunkX, currentChunkZ);

            completedTasks[0]++;
            currentChunkZ++;
            if (currentChunkZ > r) {
                currentChunkZ = minZ;
                currentChunkX++;
            }

            float overallProgress = ((float) completedTasks[0] / totalTasks) * 100.0f;
            progressBar.setValue(overallProgress);

            String stepText = String.format("Generating terrain [%d,%d]...", currentChunkX, currentChunkZ);
            renderLoadingFrame(stepText);
        }

        currentChunkX = -r;
        currentChunkZ = minZ;

        while (!glfwWindowShouldClose(window) && currentChunkX <= r) {
            glfwPollEvents();
            gameMaster.getChunkManager()
                    .buildSingleChunkMesh(currentChunkX, currentChunkZ);
            completedTasks[0]++;
            currentChunkZ++;
            if (currentChunkZ > r) {
                currentChunkZ = minZ;
                currentChunkX++;
            }

            float overallProgress = ((float) completedTasks[0] / totalTasks) * 100.0f;
            progressBar.setValue(overallProgress);

            String stepText = String.format("Building meshes [%d,%d]...", currentChunkX, currentChunkZ);
            renderLoadingFrame(stepText);
        }

        gameMaster.getChunkManager().setLastPlayerChunkX(0);
        gameMaster.getChunkManager().setLastPlayerChunkZ(0);

        gameMaster.spawn();
        completedTasks[0]++;
        progressBar.setValue(((float) completedTasks[0] / totalTasks) * 100.0f);
        renderLoadingFrame("Spawning player...");

        gameMaster.initUI();
        completedTasks[0]++;
        progressBar.setValue(100.0f);
        renderLoadingFrame("Setting up the UI...");

        progressBar.hide();
        loop();
    }

    private void renderLoadingFrame(String statusText) {
        updateFramebufferSize();
        glViewport(0, 0, framebufferWidth, framebufferHeight);
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glClearColor(0.15f, 0.15f, 0.15f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        uiManager.update(0.016f);
        GUI.begin(framebufferWidth, framebufferHeight);
        uiManager.render();

        if (statusText != null) {
            float textX = (framebufferWidth - 500f) / 2.0f;
            float textY = ((framebufferHeight - 25f) / 2.0f) - 30.0f;
            UIFont font = GUI.getNormalFont();
            GUI.drawString(statusText, textX, textY, font, K.UI.UI_TEXT_COLOR);
        }

        GUI.end();
        glfwSwapBuffers(window);
        glFlush();
    }

    public void setupCallbacks() {
        glfwSetFramebufferSizeCallback(window, (windowHandle, width, height) -> {
            if (width <= 0 || height <= 0) {
                return;
            }

            framebufferWidth = width;
            framebufferHeight = height;

            glViewport(0, 0, width, height);

            if (uiManager != null) {
                uiManager.resize(width, height);
                GUI.resize(width, height);
            }

            if (gameMaster != null) {
                gameMaster.onResize(width, height);
            }

            repositionProgressBar();
        });
    }

    private void repositionProgressBar() {
        if (progressBar == null) return;

        float barWidth = 500f;
        float barHeight = 25f;

        float x = (framebufferWidth - barWidth) / 2.0f;
        float y = (framebufferHeight - barHeight) / 2.0f;

        progressBar.setPosition(x, y);
    }

    private void loop() {
        double lastTime = glfwGetTime();
        while (!glfwWindowShouldClose(window)) {
            double currentTime = glfwGetTime();
            float delta = (float) (currentTime - lastTime);
            lastTime = currentTime;

            glfwPollEvents();

            if (Keyboard.isKeyDown(GLFW_KEY_LEFT_SHIFT) &&
                    Keyboard.isKeyPressed(GLFW_KEY_ESCAPE)) {
                gameMaster.getChunkManager().shutdown();
                glfwSetWindowShouldClose(window, true);
            }

            if (Keyboard.isKeyPressed(GLFW_KEY_F11)) {
                toggleFullscreen();
            }

            Vector3f skyColor = TimeService.getSkyColor();
            glClearColor(skyColor.x, skyColor.y, skyColor.z, CLEAR_COLOR_ALPHA);
            gameMaster.update(delta);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            glEnable(GL_DEPTH_TEST);
            gameMaster.render();
            glfwSwapBuffers(window);
        }
    }

    private void toggleFullscreen() {
        fullscreen = !fullscreen;
        if (fullscreen) {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer x = stack.mallocInt(1);
                IntBuffer y = stack.mallocInt(1);
                IntBuffer width = stack.mallocInt(1);
                IntBuffer height = stack.mallocInt(1);

                glfwGetWindowPos(window, x, y);
                glfwGetWindowSize(window, width, height);

                windowedX = x.get(0);
                windowedY = y.get(0);
                windowedWidth = width.get(0);
                windowedHeight = height.get(0);
            }

            long monitor = glfwGetPrimaryMonitor();

            if (monitor == 0) {
                fullscreen = false;
                return;
            }

            GLFWVidMode videoMode = glfwGetVideoMode(monitor);
            if (videoMode == null) {
                fullscreen = false;
                return;
            }

            glfwSetWindowAttrib(window, GLFW_DECORATED, GLFW_FALSE);
            glfwSetWindowPos(window, 0, 0);
            glfwSetWindowSize(window, videoMode.width(), videoMode.height());

        } else {
            glfwSetWindowAttrib(window, GLFW_DECORATED, GLFW_TRUE);
            glfwSetWindowPos(window, windowedX, windowedY);
            glfwSetWindowSize(window, windowedWidth, windowedHeight);
        }

        updateFramebufferSize();
        glViewport(0, 0, framebufferWidth, framebufferHeight);
        if (uiManager != null) {
            uiManager.resize(framebufferWidth, framebufferHeight);
        }

        GUI.resize(framebufferWidth, framebufferHeight);
        if (gameMaster != null) {
            gameMaster.onResize(framebufferWidth, framebufferHeight);
        }

        repositionProgressBar();
    }

    private void updateFramebufferSize() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            glfwGetFramebufferSize(window, width, height);
            framebufferWidth = width.get(0);
            framebufferHeight = height.get(0);
        }
    }
}