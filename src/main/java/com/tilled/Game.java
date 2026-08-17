package com.tilled;

import com.tilled.input.Keyboard;
import com.tilled.input.Mouse;
import com.tilled.service.TimeService;
import com.tilled.utils.K;
import com.tilled.wrld.GameMaster;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

@SuppressWarnings("unused")
public class Game {
    private static final Logger log = LoggerFactory.getLogger(Game.class);
    private static final String WINDOW_TITLE = "Tilled";

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
        log.info("Starting LWJGL 3 application...");
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

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);

            glfwGetWindowSize(window, pWidth, pHeight);
            GLFWVidMode videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            if (videoMode != null) {
                glfwSetWindowPos(window,
                        (videoMode.width() - pWidth.get(0)) / 2,
                        (videoMode.height() - pHeight.get(0)) / 2
                );
            }
        }

        Keyboard.init(window);
        Mouse.init(window);

        glfwMakeContextCurrent(window);
        GL.createCapabilities();

        glfwSetFramebufferSizeCallback(window, (windowHandle, width, height) -> {
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
        master = new GameMaster(window);
        Vector3f skyColor = TimeService.getSkyColor();
        glClearColor(skyColor.x, skyColor.y, skyColor.z, CLEAR_COLOR_ALPHA);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        master.render();
        glfwSwapBuffers(window);
        glfwShowWindow(window);
        glfwSetCursorPos(window, K.Window.DEFAULT_WIDTH / 2, K.Window.DEFAULT_HEIGHT / 2);
        log.info("GLFW window successfully initialized.");
    }

    private void loop() {
        double lastTime = glfwGetTime();
        while (!glfwWindowShouldClose(window)) {
            double currentTime = glfwGetTime();
            float delta = (float) (currentTime - lastTime);
            lastTime = currentTime;
            glfwPollEvents();
            if (Keyboard.isKeyPressed(GLFW_KEY_ESCAPE)) {
                glfwSetWindowShouldClose(window, true);
            }
            Vector3f skyColor = TimeService.getSkyColor();
            glClearColor(skyColor.x, skyColor.y, skyColor.z, CLEAR_COLOR_ALPHA);
            master.update(delta);

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            master.render();
            glfwSwapBuffers(window);
        }
    }

    public static void main(String[] ignoredArgs) {
        new Game().run();
    }
}