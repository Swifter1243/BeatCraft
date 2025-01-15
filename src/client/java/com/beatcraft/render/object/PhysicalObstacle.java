package com.beatcraft.render.object;

import com.beatcraft.animation.AnimationState;
import com.beatcraft.beatmap.data.object.Obstacle;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;

public class PhysicalObstacle extends PhysicalGameplayObject<Obstacle> {
    public PhysicalObstacle(Obstacle data) {
        super(data);
    }

    @Override
    protected void objectRender(MatrixStack matrices, VertexConsumer vertexConsumer, AnimationState animationState) {

    }
}
