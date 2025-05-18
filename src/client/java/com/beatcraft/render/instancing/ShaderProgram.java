package com.beatcraft.render.instancing;

import com.beatcraft.BeatCraft;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryStack;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class ShaderProgram {
    private final int programId;

    public ShaderProgram(Identifier basePath) {
        this(
            loadShaderSource(BeatCraft.id(basePath.getPath() + ".vsh")),
            loadShaderSource(BeatCraft.id(basePath.getPath() + ".fsh"))
        );
    }

    private static String loadShaderSource(Identifier id) {
        try {
            Resource resource = MinecraftClient.getInstance()
                .getResourceManager()
                .getResource(id)
                .orElseThrow(() -> new RuntimeException("Shader not found: " + id));

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load shader: " + id, e);
        }
    }

    public ShaderProgram(String vertexSource, String fragmentSource) {
        int vertexShader = compileShader(vertexSource, GL20.GL_VERTEX_SHADER);
        int fragmentShader = compileShader(fragmentSource, GL20.GL_FRAGMENT_SHADER);

        programId = GL20.glCreateProgram();
        GL20.glAttachShader(programId, vertexShader);
        GL20.glAttachShader(programId, fragmentShader);
        GL20.glLinkProgram(programId);

        if (GL20.glGetProgrami(programId, GL20.GL_LINK_STATUS) == 0) {
            throw new RuntimeException("Failed to link shader: " + GL20.glGetProgramInfoLog(programId));
        }

        GL20.glDeleteShader(vertexShader);
        GL20.glDeleteShader(fragmentShader);
    }

    private int compileShader(String source, int type) {
        int shader = GL20.glCreateShader(type);
        GL20.glShaderSource(shader, source);
        GL20.glCompileShader(shader);

        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == 0) {
            throw new RuntimeException("Failed to compile shader: " + GL20.glGetShaderInfoLog(shader));
        }

        return shader;
    }

    public void bind() {
        GL20.glUseProgram(programId);
    }

    public void destroy() {
        GL20.glDeleteProgram(programId);
    }

    public int getId() {
        return programId;
    }

    public int getUniform(String name) {
        return GL20.glGetUniformLocation(programId, name);
    }

    public void setUniformMat4(String name, Matrix4f matrix) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer fb = stack.mallocFloat(16);
            matrix.get(fb);
            GL20.glUniformMatrix4fv(getUniform(name), false, fb);
        }
    }

    public void setUniformInt(String name, int value) {
        GL20.glUniform1i(getUniform(name), value);
    }
}

