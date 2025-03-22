package com.beatcraft.render;

import com.beatcraft.render.effect.Bloomfog;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.block.BlockModelRenderer;

public class BeatCraftRenderLayers extends RenderLayer {

    private static final Texturing BLOOMFOG_TEXTURING = new Texturing("bloomfog_texturing", () -> {
        RenderSystem.getShader().addSampler("Bloomfog", BeatCraftRenderer.bloomfog.blurredBuffer.getColorAttachment());
        BeatCraftRenderer.bloomfog.loadTexSecondary();
    }, () -> {

    });

    private static final RenderLayer BLOOMFOG_SOLID = of(
        "bloomfogsolid",
        VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL,
        VertexFormat.DrawMode.QUADS,
        4194304,
        true,
        false,
        MultiPhaseParameters
            .builder()
            .lightmap(RenderPhase.ENABLE_LIGHTMAP)
            .program(new RenderPhase.ShaderProgram(() -> Bloomfog.bloomfog_solid_shader))
            .texture(RenderPhase.MIPMAP_BLOCK_ATLAS_TEXTURE)
            .writeMaskState(RenderPhase.ALL_MASK)
            .depthTest(RenderPhase.LEQUAL_DEPTH_TEST)
            .cull(RenderPhase.ENABLE_CULLING)
            .target(RenderPhase.MAIN_TARGET)
            .layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
            .texturing(BLOOMFOG_TEXTURING)
            .build(true)
    );

    public BeatCraftRenderLayers(String name, VertexFormat vertexFormat, VertexFormat.DrawMode drawMode, int expectedBufferSize, boolean hasCrumbling, boolean translucent, Runnable startAction, Runnable endAction) {
        super(name, vertexFormat, drawMode, expectedBufferSize, hasCrumbling, translucent, startAction, endAction);
    }

    public static void init() {

    }

    public static RenderLayer getBloomfogSolid() {

        return BLOOMFOG_SOLID;
    }

}
