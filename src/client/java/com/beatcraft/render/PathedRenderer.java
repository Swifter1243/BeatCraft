package com.beatcraft.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public abstract class PathedRenderer extends WorldRenderer {

    protected abstract void renderPositioned(MatrixStack matrices, VertexConsumer vertexConsumer, float lifeProgress);
    protected abstract float getLifeProgress();
    protected abstract Matrix4f getMatrixInLife(float lifeProgress);

    @Override
    protected void worldRender(MatrixStack matrices, VertexConsumer vertexConsumer) {
        float lifeProgress = getLifeProgress();
        Matrix4f lifeMatrix = getMatrixInLife(lifeProgress);
        matrices.multiplyPositionMatrix(lifeMatrix);
        renderPositioned(matrices, vertexConsumer, lifeProgress);
    }
}
