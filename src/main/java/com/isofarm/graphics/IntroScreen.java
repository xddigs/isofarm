package com.isofarm.graphics;

import com.isofarm.gui.GUI;
import com.isofarm.gui.UIManager;
import com.isofarm.gui.UIProgressBar;
import com.isofarm.input.Keyboard;
import com.isofarm.service.TimeService;
import com.isofarm.utils.K;
import com.isofarm.wrld.GameMaster;
import org.joml.Vector3f;
import org.joml.Vector4f;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class IntroScreen {
    private final long window;
    private GameMaster gameMaster;

    private UIProgressBar progressBar;
    private UIManager uiManager;

    private static final float CLEAR_COLOR_ALPHA = 1.0f;

    public IntroScreen(long window) {
        this.window = window;
    }

    public void setupUI() {
        float barWidth = 300f;
        float barHeight = 25f;
        float x = (K.Window.DEFAULT_WIDTH - barWidth) / 2;
        float y = (K.Window.DEFAULT_HEIGHT - barHeight) / 2;

        Vector4f foreground = new Vector4f(0.0f, 0.90f, 0.4f, 1.0f);
        Vector4f background = new Vector4f(0.2f, 0.2f, 0.2f, 1.0f);

        progressBar = new UIProgressBar(x, y, barWidth, barHeight,
                0.0f, 100.0f, false);

        uiManager = new UIManager(K.Window.DEFAULT_WIDTH, K.Window.DEFAULT_HEIGHT);
        progressBar.setColors(foreground, background);
        progressBar.show();
        uiManager.getRoot().show();
        uiManager.getRoot().addChild(progressBar);
        uiManager.resize(K.Window.DEFAULT_WIDTH, K.Window.DEFAULT_HEIGHT);
    }

    public void show() {
        setupUI();
        gameMaster = new GameMaster(window, uiManager);

        final float[] progress = {0.0f};
        final boolean[] isLoaded = {false};

        new Thread(() -> {
            gameMaster.initWorld(p -> progress[0] = p);
            isLoaded[0] = true;
        }).start();

        while (!isLoaded[0] && !glfwWindowShouldClose(window)) {
            glfwPollEvents();
            progressBar.setValue(progress[0]);
            glClearColor(0.15f, 0.15f, 0.15f, 1.0f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            glDisable(GL_DEPTH_TEST);
            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            uiManager.update(0.016f);
            GUI.begin(K.Window.DEFAULT_WIDTH, K.Window.DEFAULT_HEIGHT);
            uiManager.render();
            GUI.end();

            glfwSwapBuffers(window);
        }

        gameMaster.spawn();
        progressBar.hide();
        setupCallbacks();
        loop();
    }

    public void setupCallbacks() {
        glfwSetFramebufferSizeCallback(window, (windowHandle, width, height) -> {
            if (width > 0 && height > 0) {
                glViewport(0, 0, width, height);
                if (gameMaster != null) {
                    gameMaster.onResize(width, height);
                }
            }
        });
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

            Vector3f skyColor = TimeService.getSkyColor();
            glClearColor(skyColor.x, skyColor.y, skyColor.z, CLEAR_COLOR_ALPHA);

            gameMaster.update(delta);

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            glEnable(GL_DEPTH_TEST);
            gameMaster.render();

            glfwSwapBuffers(window);
        }
    }
}