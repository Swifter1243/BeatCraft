package com.beatcraft.render;

import com.beatcraft.beatmap.BeatmapCalculations;
import com.beatcraft.beatmap.BeatmapPlayer;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Math;
import org.joml.Vector3f;

public class PhysicalColorNote extends PhysicalBeatmapObject {
    private static final BakedModel model;
    private static final int overlay = OverlayTexture.getUv(0, false);

    static {
        var modelID = new ModelIdentifier("minecraft", "stone", "inventory");
        model = mc.getBakedModelManager().getModel(modelID);
    }

    PhysicalColorNote(float beat, float njs, float offset) {
        super(beat, njs, offset);
    }


    @Override
    protected void objectRender(MatrixStack matrices, VertexConsumer vertexConsumer) {
        var localPos = matrices.peek();
        mc.getBlockRenderManager().getModelRenderer().render(localPos, vertexConsumer, null, model, 1, 1, 1, 255, overlay);
    }
}
