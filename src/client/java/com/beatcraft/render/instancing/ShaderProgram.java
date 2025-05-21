package com.beatcraft.render.instancing;

import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ShaderProgram {
    private final int programId;
    private int vertexShaderId;
    private int fragmentShaderId;
    private final Map<String, Integer> uniforms;

    public ShaderProgram(Identifier shaderId) {
        programId = GL20.glCreateProgram();
        if (programId == 0) {
            throw new RuntimeException("Could not create shader program");
        }
        uniforms = new HashMap<>();

        // Load and compile shaders
        Identifier vertexShaderPath = Identifier.of(shaderId.getNamespace(), "shaders/" + shaderId.getPath() + ".vert");
        Identifier fragmentShaderPath = Identifier.of(shaderId.getNamespace(), "shaders/" + shaderId.getPath() + ".frag");

        vertexShaderId = createShader(loadShaderSource(vertexShaderPath), GL20.GL_VERTEX_SHADER);
        fragmentShaderId = createShader(loadShaderSource(fragmentShaderPath), GL20.GL_FRAGMENT_SHADER);

        link();

        // Create uniforms for commonly used matrices and other values
        createUniform("viewPos");
    }

    private String loadShaderSource(Identifier identifier) {
        StringBuilder shaderSource = new StringBuilder();
        ResourceManager resourceManager = MinecraftClient.getInstance().getResourceManager();
        Optional<Resource> resource = resourceManager.getResource(identifier);

        if (resource.isPresent()) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.get().getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    shaderSource.append(line).append("\n");
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to load shader: " + identifier, e);
            }
        } else {
            throw new RuntimeException("Shader not found: " + identifier);
        }

        return shaderSource.toString();
    }

    private int createShader(String shaderCode, int shaderType) {
        int shaderId = GL20.glCreateShader(shaderType);
        if (shaderId == 0) {
            throw new RuntimeException("Error creating shader. Type: " + shaderType);
        }

        GL20.glShaderSource(shaderId, shaderCode);
        GL20.glCompileShader(shaderId);

        if (GL20.glGetShaderi(shaderId, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            String errorLog = GL20.glGetShaderInfoLog(shaderId);
            throw new RuntimeException("Error compiling shader: " + errorLog);
        }

        GL20.glAttachShader(programId, shaderId);
        return shaderId;
    }

    private void link() {
        GL20.glLinkProgram(programId);
        if (GL20.glGetProgrami(programId, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            String errorLog = GL20.glGetProgramInfoLog(programId);
            throw new RuntimeException("Error linking shader program: " + errorLog);
        }

        // Validate program
        GL20.glValidateProgram(programId);
        if (GL20.glGetProgrami(programId, GL20.GL_VALIDATE_STATUS) == GL11.GL_FALSE) {
            String errorLog = GL20.glGetProgramInfoLog(programId);
            throw new RuntimeException("Error validating shader program: " + errorLog);
        }
    }

    public void createUniform(String uniformName) {
        int uniformLocation = GL20.glGetUniformLocation(programId, uniformName);
        if (uniformLocation < 0) {
            throw new RuntimeException("Could not find uniform: " + uniformName);
        }
        uniforms.put(uniformName, uniformLocation);
    }

    public void setUniform(String uniformName, Vector3f value) {
        if (!uniforms.containsKey(uniformName)) {
            createUniform(uniformName);
        }
        GL20.glUniform3f(uniforms.get(uniformName), value.x, value.y, value.z);
    }

    public void use() {
        GL20.glUseProgram(programId);
    }

    public void cleanup() {
        if (programId != 0) {
            if (vertexShaderId != 0) {
                GL20.glDetachShader(programId, vertexShaderId);
                GL20.glDeleteShader(vertexShaderId);
            }
            if (fragmentShaderId != 0) {
                GL20.glDetachShader(programId, fragmentShaderId);
                GL20.glDeleteShader(fragmentShaderId);
            }
            GL20.glDeleteProgram(programId);
        }
    }
}