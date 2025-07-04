package com.beatcraft.render.particle;

import com.beatcraft.animation.Easing;
import com.beatcraft.render.HUDRenderer;
import com.beatcraft.utils.MathUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.text.Text;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.function.Function;

public class ScoreDisplay implements Particle {

    private final int score;
    private final double spawnTime;
    private final Vector3f position;
    private final Vector3f endPoint;
    private final Quaternionf orientation;

    public ScoreDisplay(int score, Vector3f position, Vector3f endPoint, Quaternionf orientation) {
        this.score = score;
        this.position = position;
        this.endPoint = endPoint;
        this.spawnTime = System.nanoTime() / 1_000_000_000d;
        this.orientation = orientation;
    }

    private static final Function<Float, Float> easing = Easing.getEasing("easeOutExpo");

    private static final Text MISS = Text.translatable("hud.beatcraft.miss");

    @Override
    public void update(float deltaTime, BufferBuilder buffer, Vector3f cameraPos) {

        float f = (float) MathUtil.inverseLerp(spawnTime, spawnTime+1.25d, System.nanoTime() / 1_000_000_000d);
        f = Math.clamp(f, 0, 1);
        Vector3f currentPos = MathUtil.lerpVector3(position, endPoint, easing.apply(f)).sub(cameraPos);

        Matrix4f matrix = new Matrix4f();
        matrix.translate(currentPos);

        matrix.scale(1/64f);
        matrix.rotate(new Quaternionf().rotateZ((float) Math.PI));
        matrix.rotate(orientation);

        if (HUDRenderer.vertexConsumerProvider != null) {
            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

            String display = String.valueOf(this.score == 0 ? "x" : this.score == -1 ? MISS.getString() : this.score);

            int color = this.score > 100 ? 0xFFFFFFFF : 0xFF909090;

            int w = textRenderer.getWidth(display);

            textRenderer.draw(
                    Text.literal(display),
                    -w/2f, 0, color, false,
                    matrix, HUDRenderer.vertexConsumerProvider, TextRenderer.TextLayerType.NORMAL,
                    0, HUDRenderer.TEXT_LIGHT
            );

        }


    }

    @Override
    public boolean shouldRemove() {
        float f = (float) MathUtil.inverseLerp(spawnTime, spawnTime+1.25d, System.nanoTime() / 1_000_000_000d);
        return f >= 1.25;
    }
}
