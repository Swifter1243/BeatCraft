package com.beatcraft.render.object;

import com.beatcraft.animation.AnimationState;
import com.beatcraft.beatmap.data.object.Arc;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;

public class PhysicalArc extends PhysicalGameplayObject<Arc> {
    public PhysicalArc(Arc data) {
        super(data);
    }

    @Override
    protected void objectRender(MatrixStack matrices, VertexConsumer vertexConsumer, AnimationState animationState) {

    }
}
