package com.beatcraft.render;

import com.beatcraft.BeatCraft;
import com.beatcraft.render.effect.Bloomfog;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;

public class BeatCraftRenderLayers extends RenderLayer {

    private static final Texturing BLOOMFOG_TEXTURING = new Texturing("bloomfog_texturing", () -> {
        RenderSystem.setShader(() -> Bloomfog.bloomfogSolidShader);
        Bloomfog.bloomfogSolidShader.addSampler("Bloomfog", BeatCraftRenderer.bloomfog.getBloomfogColorAttachment());
        BeatCraftRenderer.bloomfog.loadTexSecondary();
    }, () -> {

    });

    private static final RenderLayer BLOOMFOG_SOLID = of(
        "bloomfog_solid",
        VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL,
        VertexFormat.DrawMode.QUADS,
        4194304,
        true,
        false,
        MultiPhaseParameters
            .builder()
            .lightmap(RenderPhase.ENABLE_LIGHTMAP)
            .program(new RenderPhase.ShaderProgram(() -> Bloomfog.bloomfogSolidShader))
            .texture(RenderPhase.MIPMAP_BLOCK_ATLAS_TEXTURE)
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
