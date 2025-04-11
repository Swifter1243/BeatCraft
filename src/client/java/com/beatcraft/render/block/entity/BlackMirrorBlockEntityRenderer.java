package com.beatcraft.render.block.entity;

import com.beatcraft.blocks.entity.BlackMirrorBlockEntity;
import com.beatcraft.blocks.entity.ReflectiveMirrorBlockEntity;
import com.beatcraft.render.effect.MirrorHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class BlackMirrorBlockEntityRenderer implements BlockEntityRenderer<BlackMirrorBlockEntity> {

    private static final Vector3f[] localVertices = new Vector3f[]{
        new Vector3f(-0.5f, -0.5f, -0.5f),
        new Vector3f(-0.5f, -0.5f,  0.5f),
        new Vector3f( 0.5f, -0.5f,  0.5f),
        new Vector3f( 0.5f, -0.5f, -0.5f),

        new Vector3f(-0.5f, 0.5f, -0.5f),
        new Vector3f(-0.5f, 0.5f,  0.5f),
        new Vector3f( 0.5f, 0.5f,  0.5f),
        new Vector3f( 0.5f, 0.5f, -0.5f),
    };

    private static final int[][] faces = new int[][]{
        {1, 0, 3, 2},
        {0, 1, 5, 4},
        {2, 3, 7, 6},
        {3, 0, 4, 7},
        {1, 2, 6, 5},
        {4, 5, 6, 7}
    };


    private void drawQuad(int[] face, BlockPos pos, Vector3f cameraPos) {

        Vector3f center = pos.toCenterPos().toVector3f();

        var v0 = center.add(localVertices[face[0]], new Vector3f()).sub(cameraPos);
        var v1 = center.add(localVertices[face[1]], new Vector3f()).sub(cameraPos);
        var v2 = center.add(localVertices[face[2]], new Vector3f()).sub(cameraPos);
        var v3 = center.add(localVertices[face[3]], new Vector3f()).sub(cameraPos);

        MirrorHandler.recordPlainCall((b, _c) -> _drawQuad(v0, v1, v2, v3, b));

    }

    private void _drawQuad(Vector3f v0, Vector3f v1, Vector3f v2, Vector3f v3, BufferBuilder buffer) {
        buffer.vertex(v0).color(0xFF000000);
        buffer.vertex(v1).color(0xFF000000);
        buffer.vertex(v2).color(0xFF000000);
        buffer.vertex(v3).color(0xFF000000);
    }

    @Override
    public void render(BlackMirrorBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {

        BlockPos pos = entity.getPos();
        Vector3f cameraPos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos().toVector3f();

        for (var face : faces) {
            drawQuad(face, pos, cameraPos);
        }
    }

    @Override
    public boolean isInRenderDistance(BlackMirrorBlockEntity blockEntity, Vec3d pos) {
        return true;
    }
}
