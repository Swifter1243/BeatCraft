package com.beatcraft.client.beatmap;

import com.beatcraft.client.beatmap.data.Difficulty;
import com.beatcraft.client.beatmap.data.Info;
import com.beatcraft.client.beatmap.object.data.GameplayObject;
import com.beatcraft.client.beatmap.object.physical.PhysicalGameplayObject;
import com.beatcraft.client.beatmap.object.physical.PhysicalObstacle;
import com.beatcraft.client.logic.Hitbox;
import com.beatcraft.client.render.HUDRenderer;
import com.beatcraft.common.data.types.Color;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import org.apache.logging.log4j.util.BiConsumer;
import org.apache.logging.log4j.util.TriConsumer;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.UUID;

public class BeatmapPlayer {



    public Vector3f worldPosition;
    public float worldAngle;
    private Level level;

    public final UUID mapId;

    public float currentBeat;
    public float currentSeconds;
    public float globalDissolve;
    public float globalArrowDissolve;
    public float firstBeat;
    public Info info;
    public Difficulty difficulty;
    public HUDRenderer.MenuScene scene;

    private final ArrayList<String> activeModifiers = new ArrayList<>();

    public final BeatmapRenderer renderer;

    private final Quaternionf ori = new Quaternionf();

    public Vector3f getRenderOrigin() {
        return worldPosition;
    }

    public void recordObstacleRenderCall(TriConsumer<BufferBuilder, Vector3f, Integer> call) {
        renderer.recordObstacleRenderCall(call);
    }

    public void recordMirroredObstacleRenderCall(TriConsumer<BufferBuilder, Vector3f, Integer> call) {
        renderer.recordMirroredObstacleRenderCall(call);
    }

    public void recordRenderCall(Runnable call) {
        renderer.recordRenderCall(call);
    }

    public void recordArcRenderCall(BiConsumer<BufferBuilder, Vector3f> call) {
        renderer.recordArcRenderCall(call);
    }

    public void recordLaserRenderCall(BiConsumer<BufferBuilder, Vector3f> call) {
        renderer.recordLaserRenderCall(call);
    }

    public void recordLaserPreRenderCall(BiConsumer<BufferBuilder, Vector3f> call) {
        renderer.recordLaserPreRenderCall(call);
    }

    public void recordLightRenderCall(BiConsumer<BufferBuilder, Vector3f> call) {
        renderer.recordLightRenderCall(call);
    }

    public void recordBloomfogPosColCall(BiConsumer<BufferBuilder, Vector3f> call) {
        renderer.recordBloomfogPosColCall(call);
    }

    public void recordPlainMirrorCall(BiConsumer<BufferBuilder, Vector3f> call) {
        renderer.recordPlainMirrorCall(call);
    }

    public BeatmapPlayer(Level level, Vector3f position, float rotation, BeatmapRenderer.RenderStyle style) {
        mapId = UUID.randomUUID();
        worldPosition = position;
        worldAngle = rotation;
        renderer = new BeatmapRenderer(this, style);
        this.level = level;
    }

    public float getBpm(float beat) {
        return info.getBpm(beat);
    }

    public void checkNote(PhysicalGameplayObject<? extends GameplayObject> obj) {

    }

    public void checkObstacle(PhysicalObstacle obstacle, Vector3f localPos, Quaternionf rotation) {

    }

    public boolean isPlaying() {
        return true;
    }

    public void setModifier(String modifier, boolean state) {
        if (state && !activeModifiers.contains(modifier)) {
            activeModifiers.add(modifier);
        } else if (!state) {
            activeModifiers.remove(modifier);
        }
    }

    public boolean isModifierActive(String modifier) {
        return activeModifiers.contains(modifier);
    }

    public void renderObstacle(Vector3f pos, Quaternionf rot, Hitbox bounds, int color) {
        renderer.renderObstacle(pos, rot, bounds, color);
    }

    public void renderMirroredObstacle(Vector3f pos, Quaternionf rot, Hitbox bounds, int color) {
        renderer.renderMirroredObstacle(pos, rot, bounds, color);
    }


    public String getDisplayInfo() {
        return "Info for map " + mapId +
            ":\n  Position: " + worldPosition +
            "\n  Rotation: " + worldAngle;
    }

    public void render(Camera camera) {

        if (camera.getEntity().level() != level) {
            return;
        }

        var dist = camera.getPosition().toVector3f().distance(worldPosition);

        var matrices = new PoseStack();

        matrices.translate(worldPosition.x, worldPosition.y, worldPosition.z);

        matrices.mulPose(ori.rotationY(worldAngle));

        renderer.render(matrices, difficulty, camera, dist);
    }


}
