package com.isofarm.graphics;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.*;

/**
 * Provides shader behavior.
 */
public class Shader {
    private static final Logger log = LoggerFactory.getLogger(Shader.class);
    private final int programId;

    /**
     * Creates a new {@code Shader} instance.
     * @param vertexPath the vertex path value
     * @param fragmentPath the fragment path value
     */
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

    /**
     * Loads load.
     * @param resourcePath the resource path value
     * @return the load result
     */
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

    /**
     * Performs the compile operation.
     * @param type the type value
     * @param source the source value
     * @param path the path value
     * @return the compile result
     */
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

    /**
     * Performs the bind operation.
     */
    public void bind() {
        glUseProgram(programId);
    }

    /**
     * Performs the unbind operation.
     */
    public void unbind() {
        glUseProgram(0);
    }

    /**
     * Performs the dispose operation.
     */
    public void dispose() {
        glUnbind();
        glDeleteProgram(programId);
        log.info("Shader program deleted [ID: {}]", programId);
    }

    /**
     * Performs the gl unbind operation.
     */
    private void glUnbind() {
        glUseProgram(0);
    }

    /**
     * Sets the uniform.
     * @param name the name value
     * @param value the value value
     */
    public void setUniform(String name, int value) {
        int location = glGetUniformLocation(programId, name);
        glUniform1i(location, value);
    }

    /**
     * Sets the uniform.
     * @param name the name value
     * @param value the value value
     */
    public void setUniform(String name, float value) {
        int location = glGetUniformLocation(programId, name);
        glUniform1f(location, value);
    }

    /**
     * Sets the uniform.
     * @param name the name value
     * @param value the value value
     */
    public void setUniform(String name, Vector4f value) {
        int location = glGetUniformLocation(programId, name);
        glUniform4f(location, value.x, value.y, value.z, value.w);
    }

    /**
     * Sets the uniform.
     * @param name the name value
     * @param value the value value
     */
    public void setUniform(String name, Vector3f value) {
        int location = glGetUniformLocation(programId, name);
        glUniform3f(location, value.x, value.y, value.z);
    }

    /**
     * Sets the uniform.
     * @param name the name value
     * @param value the value value
     */
    public void setUniform(String name, Vector2f value) {
        int location = glGetUniformLocation(programId, name);
        glUniform2f(location, value.x, value.y);
    }

    /**
     * Sets the uniform.
     * @param name the name value
     * @param x the x value
     * @param y the y value
     */
    public void setUniform(String name, float x, float y) {
        int location = glGetUniformLocation(programId, name);
        glUniform2f(location, x, y);
    }

    /**
     * Sets the uniform.
     * @param name the name value
     * @param x the x value
     * @param y the y value
     * @param z the z value
     */
    public void setUniform(String name, float x, float y, float z) {
        int location = glGetUniformLocation(programId, name);
        glUniform3f(location, x, y, z);
    }

    /**
     * Sets the uniform.
     * @param name the name value
     * @param values the values value
     */
    public void setUniform(String name, float... values) {
        int location = glGetUniformLocation(programId, name);
        glUniform4f(location, values[0], values[1], values[2], values[3]);
    }

    /**
     * Sets the uniform.
     * @param name the name value
     * @param matrix the matrix value
     */
    public void setUniform(String name, Matrix4f matrix) {
        int location = glGetUniformLocation(programId, name);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(16);
            matrix.get(buffer);
            glUniformMatrix4fv(location, false, buffer);
        }
    }

    /**
     * Sets the uniform.
     * @param name the name value
     * @param value the value value
     */
    public void setUniform(String name, boolean value) {
        int location = glGetUniformLocation(programId, name);
        glUniform1i(location, value ? 1 : 0);
    }
}