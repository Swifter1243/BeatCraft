package com.beatcraft.render.shader;

import com.beatcraft.BeatCraft;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

import java.io.IOException;

public class BeatCraftRenderLayers extends RenderLayer {

    private static net.minecraft.client.gl.ShaderProgram bloomfogShader;

    public static Framebuffer bloomfogOutput;

    private static ShaderProgram BLOOMFOG_PROCESSOR = new ShaderProgram(BeatCraftRenderLayers::getBloomfogShader);

    public static RenderPhase.Target BLOOMFOG_TARGET = new Target("beatcraft:bloomfog", () -> {
        if (bloomfogOutput != null) {
            bloomfogOutput.beginWrite(false);
        }
    }, () -> {
        MinecraftClient.getInstance().getFramebuffer().beginWrite(false);
    });

    public static void init(CoreShaderRegistrationCallback.RegistrationContext registrationContext) {
        try {
            registrationContext.register(Identifier.of(BeatCraft.MOD_ID, "rendertype_bloomfog"), VertexFormats.POSITION_TEXTURE, BeatCraftRenderLayers::setBloomfogShader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public BeatCraftRenderLayers(String name, VertexFormat vertexFormat, VertexFormat.DrawMode drawMode, int expectedBufferSize, boolean hasCrumbling, boolean translucent, Runnable startAction, Runnable endAction) {
        super(name, vertexFormat, drawMode, expectedBufferSize, hasCrumbling, translucent, startAction, endAction);
    }

    public static void setBloomfogShader(net.minecraft.client.gl.ShaderProgram shader) {
        bloomfogShader = shader;
    }

    public static net.minecraft.client.gl.ShaderProgram getBloomfogShader() {
        return bloomfogShader;
    }

    public static RenderLayer getBloomfogLayer() {
        return RenderLayer.of(
            "beatcraft:bloomfog",
            VertexFormats.LINES, VertexFormat.DrawMode.LINES,
            256, false, true,
            RenderLayer.MultiPhaseParameters.builder()
                .program(BLOOMFOG_PROCESSOR)
                .layering(RenderPhase.POLYGON_OFFSET_LAYERING)
                .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
                .cull(RenderPhase.DISABLE_CULLING)
                .depthTest(RenderPhase.ALWAYS_DEPTH_TEST)
                .lightmap(Lightmap.DISABLE_LIGHTMAP)
                .target(BLOOMFOG_TARGET)
                .build(false)
        );
    }


}
