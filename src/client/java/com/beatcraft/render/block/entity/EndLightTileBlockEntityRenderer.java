package com.beatcraft.render.block.entity;

import com.beatcraft.blocks.EndLightTileBlock;
import com.beatcraft.blocks.ModBlocks;
import com.beatcraft.blocks.entity.EndLightTileBlockEntity;
import com.beatcraft.render.BeatCraftRenderer;
import com.beatcraft.render.RenderUtil;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class EndLightTileBlockEntityRenderer implements BlockEntityRenderer<EndLightTileBlockEntity> {

    private static final float height = -0.5f + 1/64f;
    private static final float pixelSize = 1f/32f;
    private static final int bright = 0xFFFFFFFF;
    private static final int dark = 0x01FFFFFF;

    private static final Pair<Vector3f, Integer>[] meshData = new Pair[]{

        // front
        // solid color
        new Pair<>(new Vector3f(-0.5f, height, -0.5f), bright),
        new Pair<>(new Vector3f(-0.5f+pixelSize, height, -0.5f+pixelSize), bright),
        new Pair<>(new Vector3f(0.5f-pixelSize, height, -0.5f+pixelSize), bright),
        new Pair<>(new Vector3f(0.5f, height, -0.5f), bright),

        // outer gradient
        new Pair<>(new Vector3f(-0.5f, height, -0.5f), bright),
        new Pair<>(new Vector3f(-0.5f-pixelSize, height, -0.5f-pixelSize), dark),
        new Pair<>(new Vector3f(0.5f+pixelSize, height, -0.5f-pixelSize), dark),
        new Pair<>(new Vector3f(0.5f, height, -0.5f), bright),

        // inner gradient
        new Pair<>(new Vector3f(-0.5f+(2*pixelSize), height, -0.5f+(2*pixelSize)), dark),
        new Pair<>(new Vector3f(-0.5f+pixelSize, height, -0.5f+pixelSize), bright),
        new Pair<>(new Vector3f(0.5f-pixelSize, height, -0.5f+pixelSize), bright),
        new Pair<>(new Vector3f(0.5f-(2*pixelSize), height, -0.5f+(2*pixelSize)), dark),

        // right side probably
        // solid color
        new Pair<>(new Vector3f(-0.5f, height, -0.5f), bright),
        new Pair<>(new Vector3f(-0.5f + pixelSize, height, -0.5f + pixelSize), bright),
        new Pair<>(new Vector3f(-0.5f + pixelSize, height, 0.5f), bright),
        new Pair<>(new Vector3f(-0.5f, height, 0.5f), bright),

        // outer gradient
        new Pair<>(new Vector3f(-0.5f, height, -0.5f), bright),
        new Pair<>(new Vector3f(-0.5f - pixelSize, height, -0.5f - pixelSize), dark),
        new Pair<>(new Vector3f(-0.5f - pixelSize, height, 0.5f), dark),
        new Pair<>(new Vector3f(-0.5f, height, 0.5f), bright),

        // inner gradient
        new Pair<>(new Vector3f(-0.5f + (2 * pixelSize), height, -0.5f + (2 * pixelSize)), dark),
        new Pair<>(new Vector3f(-0.5f + pixelSize, height, -0.5f + pixelSize), bright),
        new Pair<>(new Vector3f(-0.5f + pixelSize, height, 0.5f), bright),
        new Pair<>(new Vector3f(-0.5f + (2 * pixelSize), height, 0.5f), dark),

        // left side probably
        // solid color
        new Pair<>(new Vector3f(0.5f, height, -0.5f), bright),
        new Pair<>(new Vector3f(0.5f - pixelSize, height, -0.5f + pixelSize), bright),
        new Pair<>(new Vector3f(0.5f - pixelSize, height, 0.5f), bright),
        new Pair<>(new Vector3f(0.5f, height, 0.5f), bright),

        // outer gradient
        new Pair<>(new Vector3f(0.5f, height, -0.5f), bright),
        new Pair<>(new Vector3f(0.5f + pixelSize, height, -0.5f - pixelSize), dark),
        new Pair<>(new Vector3f(0.5f + pixelSize, height, 0.5f), dark),
        new Pair<>(new Vector3f(0.5f, height, 0.5f), bright),

        // inner gradient
        new Pair<>(new Vector3f(0.5f - (2 * pixelSize), height, -0.5f + (2 * pixelSize)), dark),
        new Pair<>(new Vector3f(0.5f - pixelSize, height, -0.5f + pixelSize), bright),
        new Pair<>(new Vector3f(0.5f - pixelSize, height, 0.5f), bright),
        new Pair<>(new Vector3f(0.5f - (2 * pixelSize), height, 0.5f), dark),

    };

    @Override
    public void render(EndLightTileBlockEntity blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {

        World world = blockEntity.getWorld();
        if (world == null) return;
        BlockState state = world.getBlockState(blockEntity.getPos());
        if (!state.isOf(ModBlocks.END_LIGHT_TILE_BLOCK)) return;

        Quaternionf orientation = RenderUtil.getBlockRenderOrientation(state.get(EndLightTileBlock.FACE), state.get(EndLightTileBlock.ROTATION));

        BeatCraftRenderer.recordLaserRenderCall((buffer, cameraPos) -> {
            for (var vertex : meshData) {
                Vector3f pos = vertex.getLeft().rotate(orientation, new Vector3f()).add(blockEntity.getPos().toCenterPos().toVector3f()).sub(cameraPos);
                int color = vertex.getRight();
                buffer.vertex(pos.x, pos.y, pos.z).color(color);
            }

        });

    }

    @Override
    public boolean isInRenderDistance(EndLightTileBlockEntity blockEntity, Vec3d pos) {
        return true;
    }
}
