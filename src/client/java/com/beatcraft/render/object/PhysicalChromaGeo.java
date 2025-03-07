package com.beatcraft.render.object;

import com.beatcraft.BeatCraft;
import com.beatcraft.audio.BeatmapAudioPlayer;
import com.beatcraft.beatmap.data.ChromaGeometry;
import com.beatcraft.render.WorldRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Quaternionf;
import org.joml.Vector3f;


public class PhysicalChromaGeo extends WorldRenderer {

    private final ChromaGeometry chromaGeometry;
    protected BakedModel model;
    private Vector3f worldPos = new Vector3f();
    private Quaternionf worldRot = new Quaternionf();
    protected static final float SIZE_SCALAR = 0.5f;
    public static final ModelIdentifier cubeID = new ModelIdentifier(Identifier.of(BeatCraft.MOD_ID, "geo_cube"), "inventory");
    private static final int overlay = OverlayTexture.getUv(0, false);

    public PhysicalChromaGeo(ChromaGeometry geo){
        chromaGeometry = geo;
        // on instantiation, we should figure out geo, load all the materials etc. so worldRender doesn't have issues

        switch (geo.getType()) {
            // for now, everything is cube.
            default -> model = mc.getBakedModelManager().getModel(cubeID);
        }
    }



    @Override
    protected void worldRender(MatrixStack matrices, VertexConsumer vertexConsumer) {
        //this is literally a combination of every single render function I could find what the fuck am I doing
        var localPos = matrices.peek();
        worldPos = matrices.peek().getPositionMatrix().getTranslation(worldPos)
                .add(mc.gameRenderer.getCamera().getPos().toVector3f());
        worldRot = matrices.peek().getPositionMatrix().getUnnormalizedRotation(worldRot);
        matrices.scale(SIZE_SCALAR, SIZE_SCALAR, SIZE_SCALAR);
        matrices.translate(-0.5, -0.5, -0.5);

        var renderPos = localPos.getPositionMatrix().getTranslation(new Vector3f()).add(MinecraftClient.getInstance().gameRenderer.getCamera().getPos().toVector3f());
        var renderRotation = localPos.getPositionMatrix().getUnnormalizedRotation(new Quaternionf());
        mc.getBlockRenderManager().getModelRenderer().render(localPos, vertexConsumer, null, model, chromaGeometry.getMaterial().getColor().getRed(), chromaGeometry.getMaterial().getColor().getGreen(), chromaGeometry.getMaterial().getColor().getBlue(), 255, overlay);


    }
    
    @Override
    public boolean shouldRender() {
        return BeatmapAudioPlayer.isReady();
    }

}
