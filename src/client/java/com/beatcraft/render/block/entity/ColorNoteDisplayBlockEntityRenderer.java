package com.beatcraft.render.block.entity;

import com.beatcraft.BeatCraft;
import com.beatcraft.beatmap.data.CutDirection;
import com.beatcraft.blocks.entity.ColorNoteDisplayBlockEntity;
import com.beatcraft.data.types.Color;
import com.beatcraft.memory.MemoryPool;
import com.beatcraft.render.instancing.ArrowInstanceData;
import com.beatcraft.render.instancing.ColorNoteInstanceData;
import com.beatcraft.render.mesh.MeshLoader;
import com.beatcraft.utils.NoteMath;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class ColorNoteDisplayBlockEntityRenderer implements BlockEntityRenderer<ColorNoteDisplayBlockEntity> {

    public static int hash3D(int x, int y, int z) {
        int h = x * 73856093 ^ y * 19349663 ^ z * 83492791;
        h ^= (h >>> 13);
        h *= 0x5bd1e995;
        h ^= (h >>> 15);
        return h;
    }

    @Override
    public void render(ColorNoteDisplayBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();

        var q = MemoryPool.newQuaternionf();

        var cutAngle = entity.cutAngle;//.getComponents().getOrDefault(ColorNoteDisplayBlockEntity.CUT_ANGLE, 0);
        var rotationAngle = entity.rotationAngle;//.getComponents().getOrDefault(ColorNoteDisplayBlockEntity.ROTATION_ANGLE, 0);
        var color = entity.color;//new Color(entity.getComponents().getOrDefault(ColorNoteDisplayBlockEntity.COLOR, -1));
        q.rotationYXZ((float) (rotationAngle + 180) * MathHelper.RADIANS_PER_DEGREE, 0, NoteMath.degreesFromCut(CutDirection.values()[cutAngle]) * MathHelper.RADIANS_PER_DEGREE);

        matrices.translate(0.5, 0.5, 0.5);
        matrices.scale(-0.5f, -0.5f, -0.5f);
        matrices.multiply(q);
        matrices.translate(-0.5, -0.5, -0.5);
        MemoryPool.releaseSafe(q);

        var mat = matrices.peek().getPositionMatrix();

        var pos = entity.getPos();
        var n = hash3D(pos.getX(), pos.getY(), pos.getZ());
        MeshLoader.COLOR_NOTE_INSTANCED_MESH.draw(ColorNoteInstanceData.create(mat, color, 0, n));
        if (cutAngle == 8) {
            MeshLoader.NOTE_DOT_INSTANCED_MESH.draw(ArrowInstanceData.create(mat, new Color(-1), 0, n));
            MeshLoader.NOTE_DOT_INSTANCED_MESH.copyDrawToBloom();
        } else {
            MeshLoader.NOTE_ARROW_INSTANCED_MESH.draw(ArrowInstanceData.create(mat, new Color(-1), 0, n));
            MeshLoader.NOTE_ARROW_INSTANCED_MESH.copyDrawToBloom();
        }

        matrices.pop();

    }

    @Override
    public boolean isInRenderDistance(ColorNoteDisplayBlockEntity blockEntity, Vec3d pos) {
        return true;
    }
}
