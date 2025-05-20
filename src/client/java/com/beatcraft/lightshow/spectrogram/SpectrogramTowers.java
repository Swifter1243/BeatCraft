package com.beatcraft.lightshow.spectrogram;

import com.beatcraft.animation.Easing;
import com.beatcraft.audio.SpectrogramAnalyzer;
import com.beatcraft.memory.MemoryPool;
import com.beatcraft.render.BeatCraftRenderer;
import com.beatcraft.render.effect.MirrorHandler;
import net.minecraft.client.render.BufferBuilder;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.File;
import java.util.function.Function;

public class SpectrogramTowers {

    public enum TowerStyle {
        Cuboid
    }

    private SpectrogramAnalyzer spectrogram;
    private Vector3f position;
    private Quaternionf orientation;
    private Vector3f towerOffset;
    private int towerCount;
    private TowerStyle towerStyle;
    public float levelModifier = 1f;
    public Function<Float, Float> levelEasing = Easing::easeLinear;
    private boolean splitHalfway;

    public float baseHeight = 0;
    public float maxHeight = 9;

    private float[] decayHeights;

    private Vector3f upVec = new Vector3f();

    public SpectrogramTowers(Vector3f position, Quaternionf orientation, Vector3f towerOffset, int towerCount, File soundFile, TowerStyle towerStyle, boolean splitHalfway) {
        this.position = position;
        this.orientation = orientation;
        this.towerOffset = towerOffset;
        this.towerCount = towerCount;
        this.towerStyle = towerStyle;
        this.splitHalfway = splitHalfway;

        var count = splitHalfway ? (int) Math.ceil(towerCount/2f) : towerCount;
        decayHeights = new float[count];
        this.spectrogram = new SpectrogramAnalyzer(soundFile, count);

    }

    private SpectrogramTowers(Vector3f position, Quaternionf orientation, Vector3f towerOffset, int towerCount, TowerStyle towerStyle, boolean splitHalfway) {
        this.position = position;
        this.orientation = orientation;
        this.towerOffset = towerOffset;
        this.towerCount = towerCount;
        this.towerStyle = towerStyle;
        this.splitHalfway = splitHalfway;

        var count = splitHalfway ? (int) Math.ceil(towerCount/2f) : towerCount;
        decayHeights = new float[count];
    }

    public SpectrogramTowers copyTo(Vector3f position, Quaternionf orientation) {
        var other = new SpectrogramTowers(position, orientation, towerOffset, towerCount, towerStyle, splitHalfway);
        other.spectrogram = spectrogram;
        return other;
    }

    public void render(float songTime) {
        var pos = MemoryPool.newVector3f(position);
        var ori = MemoryPool.newQuaternionf(orientation);
        MirrorHandler.recordPlainCall((b, c) -> _render(b, pos, ori, songTime, c));
    }

    private void _render(BufferBuilder buffer, Vector3f position, Quaternionf orientation, float songTime, Vector3f cameraPos) {

        var realOffset = towerOffset.rotate(orientation, MemoryPool.newVector3f());
        upVec.set(0, 1, 0).rotate(orientation);

        var heights = spectrogram.getLevels(songTime);

        for (int i = 0; i < towerCount; i++) {
            float y;
            int j = i;
            if (splitHalfway) {
                j = Math.max(1, Math.abs((int)(i-(towerCount/2f))));
            }
            decayHeights[j] = Math.max(0, decayHeights[j] - (0.02f + (decayHeights[j]/90f)));
            y = levelEasing.apply(Math.clamp(heights[j]/30f, 0, 1)) * maxHeight * levelModifier;

            if (j == 1) y *= 0.8f;

            decayHeights[j] = Math.max(decayHeights[j], y);
            y = decayHeights[j];


            var pos = MemoryPool.newVector3f(realOffset).mul(i).add(position);

            // switch (towerStyle) {
            //     case Cuboid -> {

            var v0 = MemoryPool.newVector3f(-0.5f, 0, -0.5f).rotate(orientation).add(pos).sub(cameraPos);
            var v1 = MemoryPool.newVector3f(-0.5f, 0,  0.5f).rotate(orientation).add(pos).sub(cameraPos);
            var v2 = MemoryPool.newVector3f( 0.5f, 0,  0.5f).rotate(orientation).add(pos).sub(cameraPos);
            var v3 = MemoryPool.newVector3f( 0.5f, 0, -0.5f).rotate(orientation).add(pos).sub(cameraPos);

            var v0t = MemoryPool.newVector3f(-0.5f, baseHeight + y, -0.5f).rotate(orientation).add(pos).sub(cameraPos);
            var v1t = MemoryPool.newVector3f(-0.5f, baseHeight + y,  0.5f).rotate(orientation).add(pos).sub(cameraPos);
            var v2t = MemoryPool.newVector3f( 0.5f, baseHeight + y,  0.5f).rotate(orientation).add(pos).sub(cameraPos);
            var v3t = MemoryPool.newVector3f( 0.5f, baseHeight + y, -0.5f).rotate(orientation).add(pos).sub(cameraPos);

            var faces = BeatCraftRenderer.getCubeFaces(
                v0, v1, v2, v3,
                v0t, v1t, v2t, v3t,
                false
            );

            for (var face : faces) {
                buffer.vertex(face[0]).color(0xFF000000);
                buffer.vertex(face[1]).color(0xFF000000);
                buffer.vertex(face[2]).color(0xFF000000);
                buffer.vertex(face[3]).color(0xFF000000);
            }

            MemoryPool.release(v0, v1, v2, v3, v0t, v1t, v2t, v3t);

            //     }
            // }

            MemoryPool.release(pos);
        }


        MemoryPool.release(realOffset);
    }

}
