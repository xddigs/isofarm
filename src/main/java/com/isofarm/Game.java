package com.isofarm;

import com.isofarm.graphics.Intro;
import com.isofarm.input.Keyboard;
import com.isofarm.input.Mouse;
import com.isofarm.utils.K;
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
    private static final String WINDOW_TITLE = "Isofarm";

    private static final int OPENGL_MAJOR_VERSION = 3;
    private static final int OPENGL_MINOR_VERSION = 3;
    private static final int VSYNC_INTERVAL = 0;

    private long window;

    public void run() {
        log.info("Starting LWJGL 3 application...");
        init();

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
        glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE);
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

        glfwWindowHint(GLFW_SAMPLES, 16);
        Keyboard.init(window);
        Mouse.init(window);

        glfwMakeContextCurrent(window);
        GL.createCapabilities();

        log.info("OpenGL context loaded successfully.");
        log.info("GPU Renderer: {}", glGetString(GL_RENDERER));
        log.info("OpenGL Version: {}", glGetString(GL_VERSION));

        glfwSwapInterval(VSYNC_INTERVAL);
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glfwSwapBuffers(window);
        glfwShowWindow(window);
        Intro screen = new Intro(window);
        screen.show();
        glfwSetCursorPos(window, K.Window.DEFAULT_WIDTH / 2, K.Window.DEFAULT_HEIGHT / 2);
        log.info("GLFW window successfully initialized.");
    }

    public static void main(String[] ignoredArgs) {
        new Game().run();
    }
}