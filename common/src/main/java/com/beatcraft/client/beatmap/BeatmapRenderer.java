package com.beatcraft.client.beatmap;

import com.beatcraft.client.BeatcraftClient;
import com.beatcraft.client.beatmap.data.Difficulty;
import com.beatcraft.client.logic.Hitbox;
import com.beatcraft.client.render.instancing.debug.TransformationWidgetInstanceData;
import com.beatcraft.client.render.mesh.MeshLoader;
import com.beatcraft.common.utils.MathUtil;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import org.apache.logging.log4j.util.BiConsumer;
import org.apache.logging.log4j.util.TriConsumer;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;

public class BeatmapRenderer {

    public enum RenderStyle {
        HEADSET,
        DISTANCE,
    }

    private final BeatmapPlayer mapController;

    public RenderStyle renderStyle;
    public boolean doSkyEffects = true;
    public boolean skipWorldRender = false;

    public final ArrayList<BiConsumer<BufferBuilder, Vector3f>> bloomfogPosColCalls = new ArrayList<>();
    public final ArrayList<Runnable> renderCalls = new ArrayList<>();
    public final ArrayList<TriConsumer<BufferBuilder, Vector3f, Integer>> obstacleRenderCalls = new ArrayList<>();
    public final ArrayList<BiConsumer<BufferBuilder, Vector3f>> laserRenderCalls = new ArrayList<>();
    public final ArrayList<BiConsumer<BufferBuilder, Vector3f>> laserPreRenderCalls = new ArrayList<>();
    public final ArrayList<BiConsumer<BufferBuilder, Vector3f>> lightRenderCalls = new ArrayList<>();
    public final ArrayList<BiConsumer<BufferBuilder, Vector3f>> arcRenderCalls = new ArrayList<>();


    public BeatmapRenderer(BeatmapPlayer map, RenderStyle style) {
        mapController = map;
        renderStyle = style;
    }


    public void recordObstacleRenderCall(TriConsumer<BufferBuilder, Vector3f, Integer> call) {
        obstacleRenderCalls.add(call);
    }

    public void recordMirroredObstacleRenderCall(TriConsumer<BufferBuilder, Vector3f, Integer> call) {
        //obstacleRenderCalls.add(call);
    }

    public void recordRenderCall(Runnable call) {
        renderCalls.add(call);
    }

    public void recordArcRenderCall(BiConsumer<BufferBuilder, Vector3f> call) {
        arcRenderCalls.add(call);
    }

    public void recordLaserRenderCall(BiConsumer<BufferBuilder, Vector3f> call) {
        laserRenderCalls.add(call);
    }

    public void recordLaserPreRenderCall(BiConsumer<BufferBuilder, Vector3f> call) {
        laserPreRenderCalls.add(call);
    }

    public void recordLightRenderCall(BiConsumer<BufferBuilder, Vector3f> call) {
        lightRenderCalls.add(call);
    }

    public void recordBloomfogPosColCall(BiConsumer<BufferBuilder, Vector3f> call) {
        bloomfogPosColCalls.add(call);
    }

    public void recordPlainMirrorCall(BiConsumer<BufferBuilder, Vector3f> call) {

    }


    public void renderObstacle(Vector3f pos, Quaternionf rot, Hitbox bounds, int color) {

    }

    public void renderMirroredObstacle(Vector3f pos, Quaternionf rot, Hitbox bounds, int color) {

    }

    public void render(PoseStack matrices, Difficulty difficulty, Camera camera, float distance) {
        float alpha = 0;

        switch (renderStyle) {
            case DISTANCE -> {
                if (distance <= 10) {
                    alpha = 1;
                } else {
                    alpha = Math.clamp(MathUtil.inverseLerp(300, 0, (distance-10)), 0, 1);
                }
            }
            case HEADSET -> {
                alpha = BeatcraftClient.wearingHeadset ? 1 : 0;
            }
        }

        if (difficulty != null) {
            difficulty.render(matrices, camera, alpha);
        }

        if (BeatcraftClient.playerConfig.debug.beatmap.renderBeatmapPosition) {
            MeshLoader.MATRIX_LOCATOR_MESH.draw(TransformationWidgetInstanceData.create(matrices.last().pose()));
        }

    }


}
