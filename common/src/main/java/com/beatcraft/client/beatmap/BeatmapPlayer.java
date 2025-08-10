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
import net.minecraft.core.BlockPos;
import org.apache.logging.log4j.util.BiConsumer;
import org.apache.logging.log4j.util.TriConsumer;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.UUID;

public class BeatmapPlayer {
    public BlockPos worldPosition;
    public float worldAngle;

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

    private final ArrayList<BiConsumer<BufferBuilder, Vector3f>> bloomfogPosColCalls = new ArrayList<>();
    private final ArrayList<Runnable> renderCalls = new ArrayList<>();
    private final ArrayList<TriConsumer<BufferBuilder, Vector3f, Integer>> obstacleRenderCalls = new ArrayList<>();
    private final ArrayList<BiConsumer<BufferBuilder, Vector3f>> laserRenderCalls = new ArrayList<>();
    private final ArrayList<BiConsumer<BufferBuilder, Vector3f>> laserPreRenderCalls = new ArrayList<>();
    private final ArrayList<BiConsumer<BufferBuilder, Vector3f>> lightRenderCalls = new ArrayList<>();
    private final ArrayList<BiConsumer<BufferBuilder, Vector3f>> arcRenderCalls = new ArrayList<>();

    public Vector3f getRenderOrigin() {
        return worldPosition.getCenter().toVector3f().add(0f, 0.5f, 0f);
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

    public BeatmapPlayer() {
        mapId = UUID.randomUUID();
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

    }

    public void renderMirroredObstacle(Vector3f pos, Quaternionf rot, Hitbox bounds, int color) {

    }


    public String getDisplayInfo() {
        return "Info for map " + mapId +
            ":\n  Position: " + worldPosition +
            "\n  Rotation: " + worldAngle;
    }

}
