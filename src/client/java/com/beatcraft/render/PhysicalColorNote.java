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

public class PhysicalColorNote extends WorldRenderer {
    private static final BakedModel model;
    private static final int overlay = OverlayTexture.getUv(0, false);

    static {
        var modelID = new ModelIdentifier("minecraft", "stone", "inventory");
        model = mc.getBakedModelManager().getModel(modelID);
    }


    public float beat = 3;
    public float njs = 20;
    public float offset = 0;
    public static float JUMP_FAR_Z = 500;

    BeatmapCalculations.Jumps jumps;

    PhysicalColorNote() {
        jumps = BeatmapCalculations.getJumps(njs, offset, BeatmapPlayer.bpm);
    }

    public Vector3f position = new Vector3f();

    public void updateTime(float time) {
        float spawnPosition = jumps.jumpDistance() / 2;
        float despawnPosition = -spawnPosition;

        float spawnBeat = beat - jumps.halfDuration();
        float despawnBeat = beat + jumps.halfDuration();

        if (time < spawnBeat) {
            // jump in
            float percent = (spawnBeat - time) / 2;
            position.z = Math.lerp(spawnPosition, JUMP_FAR_Z, percent);
        } else if (time > despawnBeat) {
            // jump out
            float percent = (time - despawnBeat) / 2;
            position.z = Math.lerp(despawnPosition, -JUMP_FAR_Z, percent);
        } else {
            // in between
            float percent = (time - spawnBeat) / (despawnBeat - spawnBeat);
            position.z = Math.lerp(spawnPosition, despawnPosition, percent);
        }
    }

    @Override
    protected void worldRender(MatrixStack matrices, VertexConsumer vertexConsumer) {
        var localPos = matrices.peek();
        matrices.translate(-0.5, -0.5, -0.5);

        updateTime(BeatmapPlayer.beat);
        matrices.translate(position.x, position.y, position.z);

        mc.getBlockRenderManager().getModelRenderer().render(localPos, vertexConsumer, null, model, 1, 1, 1, 255, overlay);
    }
}
