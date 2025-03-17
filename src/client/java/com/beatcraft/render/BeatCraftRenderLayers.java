package com.beatcraft.render;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;

public class BeatCraftRenderLayers {
    private static final RenderLayer BLOOMFOG_SOLID = RenderLayer.of("bloomfog_solid", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS, 4194304, true, false, RenderLayer.MultiPhaseParameters.builder().lightmap(RenderPhase.ENABLE_LIGHTMAP).program(RenderPhase.SOLID_PROGRAM).texture(RenderPhase.MIPMAP_BLOCK_ATLAS_TEXTURE).build(true));


    public static RenderLayer getBloomfogSolid() {
        return BLOOMFOG_SOLID;
    }

}
