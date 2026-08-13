package com.sfarm4j;

import com.sfarm4j.input.Keyboard;
import com.sfarm4j.input.Mouse;
import com.sfarm4j.service.TimeService;
import com.sfarm4j.utils.K;
import com.sfarm4j.wrld.GameMaster;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

@SuppressWarnings("unused")
public class Game {
    private static final Logger log = LoggerFactory.getLogger(Game.class);
    private static final String WINDOW_TITLE = "SFARM4J";

    private static final int OPENGL_MAJOR_VERSION = 3;
    private static final int OPENGL_MINOR_VERSION = 3;
    private static final int VSYNC_INTERVAL = 1;

    private static final float FRAMES_PER_SECOND = 60.0f;

    private static final float CLEAR_COLOR_RED = 0.15f;
    private static final float CLEAR_COLOR_GREEN = 0.15f;
    private static final float CLEAR_COLOR_BLUE = 0.20f;
    private static final float CLEAR_COLOR_ALPHA = 1.0f;

    private long window;
    private GameMaster master;

    public void run() {
        log.info("Starting Java 25 / LWJGL 3 application...");
        init();
        loop();

        log.debug("Closing window and releasing native resources...");
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();
        glfwSetErrorCallback(null).free();
        log.info("Application terminated successfully.");
    }

    private void init() {
        GLFWErrorCallback.create((error, description) ->
                log.error("GLFW Error [0x{}]: {}", Integer.toHexString(error),
                        GLFWErrorCallback.getDescription(description))
        ).set();

        if (!glfwInit()) {
            throw new IllegalStateException("Failed to initialize GLFW");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, OPENGL_MAJOR_VERSION);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, OPENGL_MINOR_VERSION);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);

        window = glfwCreateWindow(
                (int) K.Window.DEFAULT_WIDTH,
                (int) K.Window.DEFAULT_HEIGHT,
                WINDOW_TITLE, NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create GLFW window");
        }

        Keyboard.init(window);
        Mouse.init(window);

        glfwMakeContextCurrent(window);
        GL.createCapabilities();

        glfwSetFramebufferSizeCallback(window, (
                windowHandle, width, height) -> {
            if (width > 0 && height > 0) {
                glViewport(0, 0, width, height);
                if (master != null) {
                    master.onResize(width, height);
                }
            }
        });

        log.info("OpenGL context loaded successfully.");
        log.info("GPU Renderer: {}", glGetString(GL_RENDERER));
        log.info("OpenGL Version: {}", glGetString(GL_VERSION));

        glfwSwapInterval(VSYNC_INTERVAL);
        glfwShowWindow(window);
        log.info("GLFW window successfully initialized.");
        master = new GameMaster(window);
    }

    private void loop() {
        double lastTime = glfwGetTime();

        while (!glfwWindowShouldClose(window)) {
            double currentTime = glfwGetTime();
            float delta = (float) (currentTime - lastTime);
            lastTime = currentTime;

            if (Keyboard.isKeyPressed(GLFW_KEY_ESCAPE)) {
                glfwSetWindowShouldClose(window, true);
            }

            Vector3f skyColor = TimeService.getSkyColor();
            glClearColor(skyColor.x, skyColor.y, skyColor.z, CLEAR_COLOR_ALPHA);
            master.update(delta);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            master.render();

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    static void main(String[] ignoredArgs) {
        new Game().run();
    }
}