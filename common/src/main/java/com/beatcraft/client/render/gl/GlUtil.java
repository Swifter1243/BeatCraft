package com.beatcraft.client.render.gl;

import com.beatcraft.common.data.types.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GlUtil {

    private static final Map<String, Integer> shaderProgramCache = new HashMap<>();

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

    public static int createShaderProgram(ResourceLocation vertexShaderLoc, ResourceLocation fragmentShaderLoc) {
        return createShaderProgram(vertexShaderLoc, fragmentShaderLoc, GlUtil::reProcess);
    }

    public static int createShaderProgram(ResourceLocation vertexShaderLoc, ResourceLocation fragmentShaderLoc, Function<String, String> shaderProcessor) {

        int program = GL31.glCreateProgram();

        try {
            int vertexShader = compileShader(GL31.GL_VERTEX_SHADER, vertexShaderLoc, shaderProcessor);
            int fragmentShader = compileShader(GL31.GL_FRAGMENT_SHADER, fragmentShaderLoc, shaderProcessor);

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

    private static final Pattern qcBlock = Pattern.compile("#QUEST.*?#ENDQUEST", Pattern.DOTALL);
    private static final Pattern pcBlock = Pattern.compile("#PC.*?#ENDPC", Pattern.DOTALL);
    public static String reProcess(String shader) {
        var vendor = GL31.glGetString(GL31.GL_VENDOR);

        if (vendor != null && vendor.contains("QuestCraft")) {
            var m = pcBlock.matcher(shader);
            shader = m.replaceAll("").replace("#QUEST", "").replace("#ENDQUEST", "").trim();
        } else {
            var m = qcBlock.matcher(shader);
            shader = m.replaceAll("").replace("#PC", "").replace("#ENDPC", "").trim();
        }

        return shader;

    }

    public static int compileShader(int type, String source) {
        source = reProcess(source);
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

    public static int compileShader(int type, ResourceLocation shaderLoc) throws IOException {
        return compileShader(type, shaderLoc, GlUtil::reProcess);
    }

    public static int compileShader(int type, ResourceLocation shaderLoc, Function<String, String> preProcessor) throws IOException {
        var shader = GL31.glCreateShader(type);

        var resourceManager = Minecraft.getInstance().getResourceManager();
        var source = resourceManager.getResource(shaderLoc)
            .orElseThrow(() -> new IOException("Could not find shader: " + shaderLoc))
            .openAsReader()
            .lines()
            .collect(Collectors.joining("\n"));

        source = preProcessor.apply(source);

        GL31.glShaderSource(shader, source);
        GL31.glCompileShader(shader);

        if (GL31.glGetShaderi(shader, GL31.GL_COMPILE_STATUS) == GL31.GL_FALSE) {
            String log = GL31.glGetShaderInfoLog(shader);
            GL31.glDeleteShader(shader);
            throw new RuntimeException("Failed to compile shader: " + log);
        }

        return shader;
    }

    public static int getOrCreateShaderProgram(ResourceLocation vertexShaderLoc, ResourceLocation fragmentShaderLoc) {
        var cacheKey = vertexShaderLoc.getNamespace() + ":" + vertexShaderLoc.getPath() + "_" + fragmentShaderLoc.getPath();

        return shaderProgramCache.computeIfAbsent(
            cacheKey,
            k -> createShaderProgram(vertexShaderLoc, fragmentShaderLoc)
        );
    }

    public static void destroyShaderProgram(ResourceLocation vertexShaderLoc, ResourceLocation fragmentShaderLoc) {
        var cacheKey = vertexShaderLoc.getNamespace() + ":" + vertexShaderLoc.getPath() + "_" + fragmentShaderLoc.getPath();

        if (shaderProgramCache.containsKey(cacheKey)) {
            var program = shaderProgramCache.remove(cacheKey);
            GL31.glDeleteProgram(program);
            programUniformsCache.remove(program);
            if (currentProgram == program) {
                currentProgram = 0;
            }
        }

    }

    public static void destroyShaderProgram(int program) {
        AtomicReference<String> toRemove = new AtomicReference<>();

        shaderProgramCache.forEach((key, val) -> {
            if (val == program) {
                toRemove.set(key);
            }
        });

        var remove = toRemove.get();
        if (remove != null) {
            shaderProgramCache.remove(remove);
            GL30.glDeleteProgram(program);
        }
    }

    public static void clear() {
        shaderProgramCache.clear();
        programUniformsCache.clear();
    }


    public static void setMat4f(int shaderProgram, String uni, Matrix4f mat4) {
        int uniLoc = GL31.glGetUniformLocation(shaderProgram, uni);
        GL31.glUniformMatrix4fv(uniLoc, false, mat4.get(new float[16]));
    }

    public static void setTex(int program, String name, int textureSlot, int glId) {
        GL31.glActiveTexture(GL31.GL_TEXTURE0 + textureSlot);
        var loc = GL31.glGetUniformLocation(program, name);
        GL31.glBindTexture(GL31.GL_TEXTURE_2D, glId);
        GL31.glUniform1i(loc, textureSlot);
    }


    private static final HashMap<Integer, HashMap<String, Object>> programUniformsCache = new HashMap<>();
    private static int currentProgram;

    public static void useProgram(int program) {
        currentProgram = program;
        GL31.glUseProgram(program);
    }

    public static boolean cacheUni(int program, String name, Object value) {
        if (!programUniformsCache.containsKey(program)) {
            programUniformsCache.put(program, new HashMap<>());
        }

        var uniCache = programUniformsCache.get(program);

        if (uniCache.containsKey(name)) {
            if (Objects.deepEquals(uniCache.get(name), value)) {
                return false;
            }
        }
        uniCache.put(name, value);
        return true;
    }

    public static void uniformTex(String name, int textureSlot, int glId) {
        setTex(currentProgram, name, textureSlot, glId);
    }

    public static void uniform1f(String name, float value) {
        if (cacheUni(currentProgram, name, value)) {
            var loc = GL31.glGetUniformLocation(currentProgram, name);
            GL31.glUniform1f(loc, value);
        }
    }

    public static void uniform1i(String name, int value) {
        if (cacheUni(currentProgram, name, value)) {
            var loc = GL31.glGetUniformLocation(currentProgram, name);
            GL31.glUniform1i(loc, value);
        }
    }

    public static void uniform2f(String name, float f0, float f1) {
        if (cacheUni(currentProgram, name, new float[]{f0, f1})) {
            var loc = GL31.glGetUniformLocation(currentProgram, name);
            GL31.glUniform2f(loc, f0, f1);
        }
    }

    public static void uniform3f(String name, float f0, float f1, float f2) {
        if (cacheUni(currentProgram, name, new float[]{f0, f1, f2})) {
            var loc = GL31.glGetUniformLocation(currentProgram, name);
            GL31.glUniform3f(loc, f0, f1, f2);
        }
    }

    public static void uniform4f(String name, float f0, float f1, float f2, float f3) {
        if (cacheUni(currentProgram, name, new float[]{f0, f1, f2, f3})) {
            var loc = GL31.glGetUniformLocation(currentProgram, name);
            GL31.glUniform4f(loc, f0, f1, f2, f3);
        }
    }

    public static void uniformMat4f(String name, Matrix4f mat4) {
        if (cacheUni(currentProgram, name, mat4)) {
            // Matrix4f is complex so put a copy of it instead of a direct reference
            programUniformsCache.get(currentProgram).put(name, new Matrix4f(mat4));
            setMat4f(currentProgram, name, mat4);
        }
    }

    public static void uniformColor(String name, Color color) {
        uniform4f(name, color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

}