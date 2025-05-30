package com.beatcraft.render.gl;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL31;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class GlUtil {

    private static final Map<Identifier, Integer> shaderProgramCache = new HashMap<>();


    public static int createShaderProgram(String vshSource, String fshSource) {

        int program = GL31.glCreateProgram();

        try {
            int vertexShader = compileShader(GL31.GL_VERTEX_SHADER, vshSource);
            int fragmentShader = compileShader(GL31.GL_FRAGMENT_SHADER, fshSource);

            GL31.glAttachShader(program, vertexShader);
            GL31.glAttachShader(program, fragmentShader);

            GL31.glLinkProgram(program);
            if (GL31.glGetProgrami(program, GL31.GL_LINK_STATUS) == GL31.GL_FALSE) {
                String log = GL31.glGetProgramInfoLog(program);
                throw new RuntimeException("Failed to link shader program: " + log);
            }

            GL31.glValidateProgram(program);
            if (GL31.glGetProgrami(program, GL31.GL_VALIDATE_STATUS) == GL31.GL_FALSE) {
                String log = GL31.glGetProgramInfoLog(program);
                throw new RuntimeException("Failed to validate shader program: " + log);
            }

            GL31.glDetachShader(program, vertexShader);
            GL31.glDetachShader(program, fragmentShader);
            GL31.glDeleteShader(vertexShader);
            GL31.glDeleteShader(fragmentShader);

            return program;
        } catch (Exception e) {
            GL31.glDeleteProgram(program);
            throw new RuntimeException("Failed to create shader program", e);
        }
    }

    public static int createShaderProgram(Identifier vertexShaderLoc, Identifier fragmentShaderLoc) {

        int program = GL31.glCreateProgram();

        try {
            int vertexShader = compileShader(GL31.GL_VERTEX_SHADER, vertexShaderLoc);
            int fragmentShader = compileShader(GL31.GL_FRAGMENT_SHADER, fragmentShaderLoc);

            GL31.glAttachShader(program, vertexShader);
            GL31.glAttachShader(program, fragmentShader);

            GL31.glLinkProgram(program);
            if (GL31.glGetProgrami(program, GL31.GL_LINK_STATUS) == GL31.GL_FALSE) {
                String log = GL31.glGetProgramInfoLog(program);
                throw new RuntimeException("Failed to link shader program: " + log);
            }

            GL31.glValidateProgram(program);
            if (GL31.glGetProgrami(program, GL31.GL_VALIDATE_STATUS) == GL31.GL_FALSE) {
                String log = GL31.glGetProgramInfoLog(program);
                throw new RuntimeException("Failed to validate shader program: " + log);
            }

            GL31.glDetachShader(program, vertexShader);
            GL31.glDetachShader(program, fragmentShader);
            GL31.glDeleteShader(vertexShader);
            GL31.glDeleteShader(fragmentShader);

            return program;
        } catch (Exception e) {
            GL31.glDeleteProgram(program);
            throw new RuntimeException("Failed to create shader program", e);
        }
    }

    public static int compileShader(int type, String source) {
        var shader = GL31.glCreateShader(type);

        GL31.glShaderSource(shader, source);
        GL31.glCompileShader(shader);

        if (GL31.glGetShaderi(shader, GL31.GL_COMPILE_STATUS) == GL31.GL_FALSE) {
            String log = GL31.glGetShaderInfoLog(shader);
            GL31.glDeleteShader(shader);
            throw new RuntimeException("Failed to compile shader: " + log);
        }

        return shader;
    }

    public static int compileShader(int type, Identifier shaderLoc) throws IOException {
        var shader = GL31.glCreateShader(type);

        var resourceManager = MinecraftClient.getInstance().getResourceManager();
        var source = resourceManager.getResource(shaderLoc)
            .orElseThrow(() -> new IOException("Could not find shader: " + shaderLoc))
            .getReader()
            .lines()
            .collect(Collectors.joining("\n"));

        GL31.glShaderSource(shader, source);
        GL31.glCompileShader(shader);

        if (GL31.glGetShaderi(shader, GL31.GL_COMPILE_STATUS) == GL31.GL_FALSE) {
            String log = GL31.glGetShaderInfoLog(shader);
            GL31.glDeleteShader(shader);
            throw new RuntimeException("Failed to compile shader: " + log);
        }

        return shader;
    }

    public static int getOrCreateShaderProgram(Identifier vertexShaderLoc, Identifier fragmentShaderLoc) {
        var cacheKey = Identifier.of(
            vertexShaderLoc.getNamespace(),
            vertexShaderLoc.getPath() + "_" + fragmentShaderLoc.getPath()
        );

        return shaderProgramCache.computeIfAbsent(
            cacheKey,
            k -> createShaderProgram(vertexShaderLoc, fragmentShaderLoc)
        );
    }

    public static void destroyShaderProgram(Identifier vertexShaderLoc, Identifier fragmentShaderLoc) {
        var cacheKey = Identifier.of(
            vertexShaderLoc.getNamespace(),
            vertexShaderLoc.getPath() + "_" + fragmentShaderLoc.getPath()
        );

        if (shaderProgramCache.containsKey(cacheKey)) {
            var program = shaderProgramCache.remove(cacheKey);
            GL31.glDeleteProgram(program);
        }

    }

    public static void setMat4f(int shaderProgram, String uni, Matrix4f mat4) {
        int uniLoc = GL20.glGetUniformLocation(shaderProgram, uni);
        GL20.glUniformMatrix4fv(uniLoc, false, mat4.get(new float[16]));
    }

    public static void setTex(int program, String name, int textureSlot, int glId) {
        GL31.glActiveTexture(GL31.GL_TEXTURE0 + textureSlot);
        var loc = GL31.glGetUniformLocation(program, name);
        GL31.glBindTexture(GL31.GL_TEXTURE_2D, glId);
        GL31.glUniform1i(loc, textureSlot);
    }

}
