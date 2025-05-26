package com.beatcraft.render.gl;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class GlUtil {

    private static final Map<Identifier, Integer> shaderProgramCache = new HashMap<>();


    public static int createShaderProgram(String vshSource, String fshSource) {

        int program = GL20.glCreateProgram();

        try {
            int vertexShader = compileShader(GL20.GL_VERTEX_SHADER, vshSource);
            int fragmentShader = compileShader(GL20.GL_FRAGMENT_SHADER, fshSource);

            GL20.glAttachShader(program, vertexShader);
            GL20.glAttachShader(program, fragmentShader);

            GL20.glLinkProgram(program);
            if (GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
                String log = GL20.glGetProgramInfoLog(program);
                throw new RuntimeException("Failed to link shader program: " + log);
            }

            GL20.glValidateProgram(program);
            if (GL20.glGetProgrami(program, GL20.GL_VALIDATE_STATUS) == GL11.GL_FALSE) {
                String log = GL20.glGetProgramInfoLog(program);
                throw new RuntimeException("Failed to validate shader program: " + log);
            }

            GL20.glDetachShader(program, vertexShader);
            GL20.glDetachShader(program, fragmentShader);
            GL20.glDeleteShader(vertexShader);
            GL20.glDeleteShader(fragmentShader);

            return program;
        } catch (Exception e) {
            GL20.glDeleteProgram(program);
            throw new RuntimeException("Failed to create shader program", e);
        }
    }

    public static int createShaderProgram(Identifier vertexShaderLoc, Identifier fragmentShaderLoc) {

        int program = GL20.glCreateProgram();

        try {
            int vertexShader = compileShader(GL20.GL_VERTEX_SHADER, vertexShaderLoc);
            int fragmentShader = compileShader(GL20.GL_FRAGMENT_SHADER, fragmentShaderLoc);

            GL20.glAttachShader(program, vertexShader);
            GL20.glAttachShader(program, fragmentShader);

            GL20.glLinkProgram(program);
            if (GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
                String log = GL20.glGetProgramInfoLog(program);
                throw new RuntimeException("Failed to link shader program: " + log);
            }

            GL20.glValidateProgram(program);
            if (GL20.glGetProgrami(program, GL20.GL_VALIDATE_STATUS) == GL11.GL_FALSE) {
                String log = GL20.glGetProgramInfoLog(program);
                throw new RuntimeException("Failed to validate shader program: " + log);
            }

            GL20.glDetachShader(program, vertexShader);
            GL20.glDetachShader(program, fragmentShader);
            GL20.glDeleteShader(vertexShader);
            GL20.glDeleteShader(fragmentShader);

            return program;
        } catch (Exception e) {
            GL20.glDeleteProgram(program);
            throw new RuntimeException("Failed to create shader program", e);
        }
    }

    public static int compileShader(int type, String source) {
        var shader = GL20.glCreateShader(type);

        GL20.glShaderSource(shader, source);
        GL20.glCompileShader(shader);

        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            String log = GL20.glGetShaderInfoLog(shader);
            GL20.glDeleteShader(shader);
            throw new RuntimeException("Failed to compile shader: " + log);
        }

        return shader;
    }

    public static int compileShader(int type, Identifier shaderLoc) throws IOException {
        var shader = GL20.glCreateShader(type);

        var resourceManager = MinecraftClient.getInstance().getResourceManager();
        var source = resourceManager.getResource(shaderLoc)
            .orElseThrow(() -> new IOException("Could not find shader: " + shaderLoc))
            .getReader()
            .lines()
            .collect(Collectors.joining("\n"));

        GL20.glShaderSource(shader, source);
        GL20.glCompileShader(shader);

        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            String log = GL20.glGetShaderInfoLog(shader);
            GL20.glDeleteShader(shader);
            throw new RuntimeException("Failed to compile shader: " + log);
        }

        return shader;
    }

    public static int getOrCreateShaderProgram(Identifier vertexShaderLoc, Identifier fragmentShaderLoc) {
        var cacheKey = Identifier.of(
            vertexShaderLoc.getNamespace(),
            vertexShaderLoc.getPath() + "_" + fragmentShaderLoc.getPath()
        );

        return shaderProgramCache.computeIfAbsent(cacheKey,
            k -> createShaderProgram(vertexShaderLoc, fragmentShaderLoc));
    }

    public static void destroyShaderProgram(Identifier vertexShaderLoc, Identifier fragmentShaderLoc) {
        var cacheKey = Identifier.of(
            vertexShaderLoc.getNamespace(),
            vertexShaderLoc.getPath() + "_" + fragmentShaderLoc.getPath()
        );

        if (shaderProgramCache.containsKey(cacheKey)) {
            var program = shaderProgramCache.remove(cacheKey);
            GL20.glDeleteProgram(program);
        }

    }

}
