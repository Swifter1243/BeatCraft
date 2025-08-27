package com.beatcraft.client.beatmap;

import com.beatcraft.Beatcraft;
import com.beatcraft.client.audio.Audio;
import com.beatcraft.client.audio.AudioController;
import com.beatcraft.client.beatmap.data.*;
import com.beatcraft.client.beatmap.object.data.GameplayObject;
import com.beatcraft.client.beatmap.object.physical.PhysicalGameplayObject;
import com.beatcraft.client.beatmap.object.physical.PhysicalObstacle;
import com.beatcraft.client.logic.Hitbox;
import com.beatcraft.client.render.HUDRenderer;
import com.beatcraft.common.data.map.SongData;
import com.beatcraft.common.data.types.Color;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import org.apache.commons.compress.archivers.dump.UnrecognizedFormatException;
import org.apache.logging.log4j.util.BiConsumer;
import org.apache.logging.log4j.util.TriConsumer;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

public class BeatmapPlayer {

    public Vector3f worldPosition;
    public float worldAngle;
    private final Level level;

    public final UUID mapId;

    private long elapsedNanoTime = 0;

    public float currentBeat;
    public float currentSeconds;
    public float globalDissolve;
    public float globalArrowDissolve;
    public float firstBeat;
    public Audio audio;
    public Info info;
    public Difficulty difficulty;
    public HUDRenderer.MenuScene scene;
    private boolean playing = false;

    public float playbackSpeed = 1.0f;
    public boolean isInWall = false;

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
        this(UUID.randomUUID(), level, position, rotation, style);
    }

    public BeatmapPlayer(UUID uuid, Level level, Vector3f position, float rotation, BeatmapRenderer.RenderStyle style) {
        mapId = uuid;
        worldPosition = position;
        worldAngle = rotation;
        renderer = new BeatmapRenderer(this, style);
        this.level = level;
    }

    public void playSong(SongData.BeatmapInfo info) {
        try {
            setupDifficulty(info);
            scene = HUDRenderer.MenuScene.InGame;

            if (audio != null) {
                audio.close();
            }

            audio = AudioController.playMapSong(this.info.getSongFilename());

            setDifficultyFromFile(info.getBeatmapLocation().toString(), this.info);
            elapsedNanoTime = 0;
            playing = true;

        } catch (IOException e) {
            Beatcraft.LOGGER.error("Failed to load map", e);
        }

    }

    private void setupDifficulty(SongData.BeatmapInfo beatmapInfo) throws IOException {
        var p = beatmapInfo.getBeatmapLocation();

        var infoPath = p.getParent().toString() + "/Info.dat";
        Info info;
        try {
            info = getInfoFromFile(infoPath);
        } catch (NoSuchFileException e) {
            infoPath = p.getParent().toString() + "/info.dat";
            info = getInfoFromFile(infoPath);
        }
        this.info = info;

    }

    public float getBpm(float beat) {
        return info.getBpm(beat);
    }

    public void checkNote(PhysicalGameplayObject<? extends GameplayObject> obj) {

    }

    public void checkObstacle(PhysicalObstacle obstacle, Vector3f localPos, Quaternionf rotation) {

    }

    public boolean isPlaying() {
        return playing;
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


    public String getDisplayInfo() {
        return "Info for map " + mapId +
            ":\n  Position: " + worldPosition +
            " (world: " + level +
            ")\n  Rotation: " + worldAngle;
    }

    private long lastNanoTime = 0;



    private long getNanoDeltaTime() {
        var n = System.nanoTime();
        var ndt = n - lastNanoTime;
        lastNanoTime = n;
        return ndt;
    }

    public void render(Camera camera) {

        long deltaNanoSeconds = getNanoDeltaTime();


        if (camera.getEntity().level() != level) {
            return;
        }

        boolean shouldPlay = this.isPlaying() && !Minecraft.getInstance().isPaused();

        if (shouldPlay) {
            elapsedNanoTime += (long) (deltaNanoSeconds * playbackSpeed);

            if (difficulty != null) {
                currentSeconds = elapsedNanoTime / 1_000_000_000f;
                currentBeat = info.getBeat(currentSeconds);
                difficulty.update(currentBeat, (double) deltaNanoSeconds / 1_000_000_000d);
            }

        }

        var dist = camera.getPosition().toVector3f().distance(worldPosition);

        var matrices = new PoseStack();

        matrices.translate(worldPosition.x, worldPosition.y, worldPosition.z);

        matrices.mulPose(ori.rotationY(worldAngle));

        renderer.render(matrices, difficulty, camera, dist);
        if (audio != null) {
            audio.update(currentBeat, (double) deltaNanoSeconds / 1_000_000_000d, this);
        }
    }


    public static Info getInfoFromFile(String path) throws IOException {
        String jsonString = Files.readString(Paths.get(path));
        JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();

        Info info = Info.from(json, path);

        if (json.has("difficultyBeatmaps")) {
            JsonArray styleSetsRaw = json.get("difficultyBeatmaps").getAsJsonArray();
            styleSetsRaw.forEach(styleSetRaw -> {
                JsonObject styleSetObject = styleSetRaw.getAsJsonObject();

                String styleKey = styleSetObject.get("characteristic").getAsString();
                if (!info.getStyleSets().containsKey(styleKey)) {
                    info.getStyleSets().put(styleKey, new Info.StyleSet());
                }

                Info.StyleSet styleSet = info.getStyleSets().get(styleKey);

                Info.SetDifficulty setDifficulty = Info.SetDifficulty.from(styleSetObject, info);
                String fileName = styleSetObject.get("beatmapDataFilename").getAsString();
                styleSet.difficulties.put(fileName, setDifficulty);

            });
        }
        else {
            JsonArray styleSetsRaw = json.get("_difficultyBeatmapSets").getAsJsonArray();
            styleSetsRaw.forEach(styleSetRaw -> {
                JsonObject styleSetObject = styleSetRaw.getAsJsonObject();
                Info.StyleSet styleSet = new Info.StyleSet();

                String styleKey = styleSetObject.get("_beatmapCharacteristicName").getAsString();
                info.getStyleSets().put(styleKey, styleSet);

                JsonArray difficultiesRaw = styleSetObject.get("_difficultyBeatmaps").getAsJsonArray();
                difficultiesRaw.forEach(difficultyRaw -> {
                    JsonObject difficultyObject = difficultyRaw.getAsJsonObject();
                    Info.SetDifficulty setDifficulty = Info.SetDifficulty.from(difficultyObject, info);
                    String fileName = difficultyObject.get("_beatmapFilename").getAsString();
                    styleSet.difficulties.put(fileName, setDifficulty);
                });
            });
        }
        return info;
    }

    public static String getPathFileName(String path) {
        return Paths.get(path).getFileName().toString();
    }

    public static Info.SetDifficulty getSetDifficulty(String fileName, Info info) {
        for (Info.StyleSet styleSet : info.getStyleSets().values()) {
            for (var entry : styleSet.difficulties.entrySet()) {
                if (Objects.equals(entry.getKey(), fileName)) {
                    return entry.getValue();
                }
            }
        }

        return null;
    }

    private static int getMajorVersion(JsonObject json) {
        String version;
        if (json.has("version")) {
            version = json.get("version").getAsString();
        } else {
            version = json.get("_version").getAsString();
        }
        return Integer.parseInt(version.substring(0, 1));
    }

    public void setDifficultyFromFile(String path, Info info) throws IOException {
        String fileName = getPathFileName(path);
        Info.SetDifficulty setDifficulty = getSetDifficulty(fileName, info);
        setDifficultyFromFile(path, setDifficulty, info);
    }
    public void setDifficultyFromFile(String path, Info.SetDifficulty setDifficulty, Info info) throws IOException {
        String jsonString = Files.readString(Paths.get(path));
        JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();

        int majorVersion = getMajorVersion(json);
        switch (majorVersion) {
            case 2 -> {
                this.difficulty = new DifficultyV2(this, info, setDifficulty);
                ((DifficultyV2) this.difficulty).load(json);
            }
            case 3 -> {
                this.difficulty = new DifficultyV3(this, info, setDifficulty);
                ((DifficultyV3) this.difficulty).load(json);
            }
            case 4 -> {
                this.difficulty = new DifficultyV4(this, info, setDifficulty);
                ((DifficultyV4) this.difficulty).load(json);
            }
            default -> throw new UnrecognizedFormatException();
        }
    }


}
