package com.beatcraft.render.object;

import com.beatcraft.BeatmapPlayer;
import com.beatcraft.animation.AnimationState;
import com.beatcraft.beatmap.data.object.Obstacle;
import com.beatcraft.logic.Hitbox;
import com.beatcraft.render.DebugRenderer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class PhysicalObstacle extends PhysicalGameplayObject<Obstacle> {

    private final Hitbox bounds = new Hitbox(
        new Vector3f(-0.3f, 0, 0),
        new Vector3f(0.3f, 0, 0)
    );

    public PhysicalObstacle(Obstacle data) {
        super(data);
    }

    @Override
    protected void objectRender(MatrixStack matrices, VertexConsumer vertexConsumer, AnimationState animationState) {
        var localPos = matrices.peek().getPositionMatrix().getTranslation(new Vector3f());
        //localPos.x = localPos.x * 2;
        var camPos = mc.gameRenderer.getCamera().getPos();
        localPos.x = (-data.getX()) * 0.6f + 0.9f;
        localPos.y = (data.getY() * 0.6f);
        updateBounds();
        DebugRenderer.renderHitbox(bounds, localPos.add(0, 0, (float) camPos.z), new Quaternionf(), BeatmapPlayer.currentBeatmap.getSetDifficulty().getColorScheme().getObstacleColor().toARGB(), true);
    }

    private void updateBounds() {
        bounds.min.x = -((data.getWidth() * 0.6f) - 0.3f);
        //bounds.max.x = (data.getWidth() * 0.6f)/2f;
        //bounds.min.y = -0.3f;
        bounds.max.y = (data.getHeight() * 0.6f);

        float length = this.data.getNjs() * (60f / BeatmapPlayer.currentBeatmap.getInfo().getBpm());

        bounds.max.z = data.getDuration() * length;
    }

    @Override
    public float getJumpOutPosition() {
        float length = this.data.getNjs() * (60f / BeatmapPlayer.currentBeatmap.getInfo().getBpm());

        return -(length * data.getDuration());
    }


    @Override
    public float getJumpOutBeat() {
        return data.getBeat() + data.getDuration();
    }

    @Override
    public float getDespawnBeat() {
        return super.getDespawnBeat() + data.getDuration();
    }
}
