package com.beatcraft.render.block.entity;

import com.beatcraft.blocks.ModBlocks;
import com.beatcraft.blocks.ReflectiveMirrorStripBlock;
import com.beatcraft.blocks.entity.ReflectiveMirrorStripBlockEntity;
import com.beatcraft.memory.MemoryPool;
import com.beatcraft.render.effect.MirrorHandler;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class ReflectiveMirrorStripBlockEntityRenderer implements BlockEntityRenderer<ReflectiveMirrorStripBlockEntity> {

    private static final Vector3f[] localVertices = new Vector3f[]{
        new Vector3f(-(6f/16f), 0.375f, -0.5f),
        new Vector3f(-(6f/16f), 0.375f,  0.5f),
        new Vector3f( (6f/16f), 0.375f,  0.5f),
        new Vector3f( (6f/16f), 0.375f, -0.5f),

        new Vector3f(-(6f/16f), 0.5f, -0.5f),
        new Vector3f(-(6f/16f), 0.5f,  0.5f),
        new Vector3f( (6f/16f), 0.5f,  0.5f),
        new Vector3f( (6f/16f), 0.5f, -0.5f),
    };

    private static final int[][] faces = new int[][]{
        {1, 0, 3, 2},
        {0, 1, 5, 4},
        {2, 3, 7, 6},
        {3, 0, 4, 7},
        {1, 2, 6, 5},
    };

    // top face
    private static final int[] topFace = new int[]{4, 5, 6, 7};


    private void drawQuad(int[] face, BlockPos pos, Vector3f cameraPos, boolean facingEast) {

        Vector3f center = pos.toCenterPos().toVector3f();

        var q = MemoryPool.newQuaternionf().rotationY(facingEast ? 90 * MathHelper.RADIANS_PER_DEGREE : 0);

        var f0 = MemoryPool.newVector3f(localVertices[face[0]]).rotate(q);
        var f1 = MemoryPool.newVector3f(localVertices[face[1]]).rotate(q);
        var f2 = MemoryPool.newVector3f(localVertices[face[2]]).rotate(q);
        var f3 = MemoryPool.newVector3f(localVertices[face[3]]).rotate(q);

        MemoryPool.release(q);

        var v0 = center.add(f0, MemoryPool.newVector3f()).sub(cameraPos);
        var v1 = center.add(f1, MemoryPool.newVector3f()).sub(cameraPos);
        var v2 = center.add(f2, MemoryPool.newVector3f()).sub(cameraPos);
        var v3 = center.add(f3, MemoryPool.newVector3f()).sub(cameraPos);

        MemoryPool.release(f0, f1, f2, f3);

        MirrorHandler.recordPlainCall((b, _c) -> _drawQuad(v0, v1, v2, v3, b));

    }

    private void _drawQuad(Vector3f v0, Vector3f v1, Vector3f v2, Vector3f v3, BufferBuilder buffer) {
        buffer.vertex(v0).color(0xFF000000);
        buffer.vertex(v1).color(0xFF000000);
        buffer.vertex(v2).color(0xFF000000);
        buffer.vertex(v3).color(0xFF000000);
    }

    @Override
    public void render(ReflectiveMirrorStripBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {


        World world = entity.getWorld();
        if (world == null) return;

        BlockPos pos = entity.getPos();
        Vector3f cameraPos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos().toVector3f();
        BlockState state = world.getBlockState(entity.getPos());
        if (!state.isOf(ModBlocks.REFLECTIVE_MIRROR_STRIP_BLOCK)) return;

        boolean facesEast = state.get(ReflectiveMirrorStripBlock.ROTATION) == Direction.EAST;

        for (var face : faces) {
            drawQuad(face, pos, cameraPos, facesEast);
        }

        if (pos.getY() == -1) {
            MirrorHandler.recordCall((b, v, q) -> drawMirror(b, v, pos.toCenterPos().toVector3f(), q, facesEast));
        } else {
            drawQuad(topFace, pos, cameraPos, facesEast);
        }


    }


    @Override
    public boolean isInRenderDistance(ReflectiveMirrorStripBlockEntity blockEntity, Vec3d pos) {
        return true;
    }

    private void drawMirror(BufferBuilder buffer, Vector3f cameraPos, Vector3f center, Quaternionf invCameraRotation, boolean facingEast) {



        var q = MemoryPool.newQuaternionf().rotationY(facingEast ? 90 * MathHelper.RADIANS_PER_DEGREE : 0);

        var f0 = MemoryPool.newVector3f(localVertices[topFace[0]]).rotate(q);
        var f1 = MemoryPool.newVector3f(localVertices[topFace[1]]).rotate(q);
        var f2 = MemoryPool.newVector3f(localVertices[topFace[2]]).rotate(q);
        var f3 = MemoryPool.newVector3f(localVertices[topFace[3]]).rotate(q);

        MemoryPool.release(q);

        var v0 = center.add(f0, MemoryPool.newVector3f()).sub(cameraPos);
        var v1 = center.add(f1, MemoryPool.newVector3f()).sub(cameraPos);
        var v2 = center.add(f2, MemoryPool.newVector3f()).sub(cameraPos);
        var v3 = center.add(f3, MemoryPool.newVector3f()).sub(cameraPos);

        buffer.vertex(v0).color(0xFF000000);
        buffer.vertex(v1).color(0xFF000000);
        buffer.vertex(v2).color(0xFF000000);
        buffer.vertex(v3).color(0xFF000000);

    }


}
