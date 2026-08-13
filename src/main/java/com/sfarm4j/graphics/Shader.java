package com.sfarm4j.graphics;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.*;

public class Shader {
    private static final Logger log = LoggerFactory.getLogger(Shader.class);
    private final int programId;

    public Shader(String vertexPath, String fragmentPath) {
        String vertexSource = load(vertexPath);
        String fragmentSource = load(fragmentPath);

        int vertexId = compile(GL_VERTEX_SHADER, vertexSource, vertexPath);
        int fragmentId = compile(GL_FRAGMENT_SHADER, fragmentSource, fragmentPath);

        this.programId = glCreateProgram();
        glAttachShader(programId, vertexId);
        glAttachShader(programId, fragmentId);
        glLinkProgram(programId);

        if (glGetProgrami(programId, GL_LINK_STATUS) == GL_FALSE) {
            String logInfo = glGetProgramInfoLog(programId);
            throw new RuntimeException("Error linking shader program: " + logInfo);
        }

        glDetachShader(programId, vertexId);
        glDetachShader(programId, fragmentId);
        glDeleteShader(vertexId);
        glDeleteShader(fragmentId);
        log.info("Shader program successfully compiled and linked [ID: {}]", programId);
    }

    private String load(String resourcePath) {
        try (var inputStream = Shader.class.getClassLoader()
                .getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Resource not" +
                        " found on classpath: " + resourcePath);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read shader " +
                    "resource [" + resourcePath + "]", e);
        }
    }

    private int compile(int type, String source, String path) {
        int shaderId = glCreateShader(type);
        glShaderSource(shaderId, source);
        glCompileShader(shaderId);

        if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == GL_FALSE) {
            String logInfo = glGetShaderInfoLog(shaderId);
            String shaderType = (type == GL_VERTEX_SHADER) ? "Vertex" : "Fragment";
            throw new RuntimeException("Error compiling " +
                    shaderType + " shader [" + path + "]: " + logInfo);
        }

        return shaderId;
    }

    public void bind() {
        glUseProgram(programId);
    }

    public void unbind() {
        glUseProgram(0);
    }

    public void dispose() {
        glUnbind();
        glDeleteProgram(programId);
        log.info("Shader program deleted [ID: {}]", programId);
    }

    private void glUnbind() {
        glUseProgram(0);
    }

    public void setUniform(String name, int value) {
        int location = glGetUniformLocation(programId, name);
        glUniform1i(location, value);
    }

    public void setUniform(String name, float value) {
        int location = glGetUniformLocation(programId, name);
        glUniform1f(location, value);
    }

    public void setUniform(String name, Vector3f value) {
        int location = glGetUniformLocation(programId, name);
        glUniform3f(location, value.x, value.y, value.z);
    }

    public void setUniform(String name, Matrix4f matrix) {
        int location = glGetUniformLocation(programId, name);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(16);
            matrix.get(buffer);
            glUniformMatrix4fv(location, false, buffer);
        }
    }

    public void setUniform(String name, boolean value) {
        int location = glGetUniformLocation(programId, name);
        glUniform1i(location, value ? 1 : 0);
    }
}