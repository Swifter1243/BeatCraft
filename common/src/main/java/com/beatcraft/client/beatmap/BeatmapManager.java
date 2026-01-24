package com.beatcraft.client.beatmap;

import com.beatcraft.Beatcraft;
import com.beatcraft.client.BeatcraftClient;
import com.beatcraft.common.data.map.SongData;
import com.beatcraft.common.memory.MemoryPool;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Function;

public class BeatmapManager {

    public static final ArrayList<BeatmapController> beatmaps = new ArrayList<>();
    public static final ArrayList<SongData> songs = new ArrayList<>();

    private static double last = 1.0;
    private static final double STEP = 0.01;
    private static void stepTo(double target) {
        if (last == target) return;
        if (target > last) {
            last = Math.min(target, last+STEP);
        } else {
            last = Math.max(target, last-STEP);
        }
    }

    public static double getSkyFadeFactor() {
        return BeatcraftClient.playerConfig.quality.skyFog() ? last : 1.0;
    }

    private static void updateFadeFactor() {
        assert Minecraft.getInstance().player != null;
        var playerId = Minecraft.getInstance().player.getUUID();
        var player = Minecraft.getInstance().player.position();
        var p = MemoryPool.newVector3f((float) player.x, (float) player.y, (float) player.z);
        // radii for playing: <=75: 0, 75-250: 0-1, >250: 1
        var nearestPlaying = getNearestFiltered(
            p,
            map -> map.isPlaying()
                && (
                    ((map.trackedPlayer.equals(playerId) || BeatcraftClient.wearingHeadset) && map.renderer.renderStyle == BeatmapRenderer.RenderStyle.HEADSET)
                    || map.renderer.renderStyle == BeatmapRenderer.RenderStyle.DISTANCE
                )
        );
        // radii for not playing: <=6: 0, 6-18: 0-1, >18: 1
        var nearestNotPlaying = getNearestFiltered(
            p,
            map -> !map.isPlaying()
                && (
                ((map.trackedPlayer.equals(playerId) || BeatcraftClient.wearingHeadset) && map.renderer.renderStyle == BeatmapRenderer.RenderStyle.HEADSET)
                    || map.renderer.renderStyle == BeatmapRenderer.RenderStyle.DISTANCE
            )
        );

        float fadePlaying = 1.0f;
        if (nearestPlaying != null) {
            float d = nearestPlaying.worldPosition.distance(p);

            if (d <= 75.0f) {
                fadePlaying = 0.0f;
            } else if (d < 250.0f) {
                fadePlaying = (d - 75.0f) / (250.0f - 75.0f);
            }
        }

        float fadeNotPlaying = 1.0f;
        if (nearestNotPlaying != null) {
            float d = nearestNotPlaying.worldPosition.distance(p);

            if (d <= 6.0f) {
                fadeNotPlaying = 0.0f;
            } else if (d < 18.0f) {
                fadeNotPlaying = (d - 6.0f) / (18.0f - 6.0f);
            }
        }

        // use the most restrictive fade
        float fade = Math.min(fadePlaying, fadeNotPlaying);

        stepTo(fade);

        MemoryPool.release(p);
    }

    public static void loadBeatmaps() {
        var folderPath = Minecraft.getInstance().gameDirectory.toPath() + "/beatmaps/";

        var folder = new File(folderPath);

        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                Beatcraft.LOGGER.error("Failed to create beatmaps folder");
                return;
            }
        }

        var subFolders = folder.listFiles(File::isDirectory);

        if (subFolders == null) {
            Beatcraft.LOGGER.error("Failed to load beatmaps");
            return;
        }

        songs.clear();

        for (var songFolder : subFolders) {
            try {
                var data = new SongData(songFolder.getAbsolutePath());
                songs.add(data);

                // TODO: convert images to PNG

            } catch (IOException e) {
                Beatcraft.LOGGER.error("Failed to load beatmap", e);
            }
        }

    }


    private static final float[] DEFAULT_FOG_HEIGHTS = new float[]{-50, -30};
    public static float[] getAverageFogHeight(Vector3f position) {

        var nearest = nearestActiveBeatmapToPlayer();
        float[] x;
        if (nearest != null) {
            x = nearest.difficulty.lightShowEnvironment.getFogHeights();
            x = new float[]{x[0] + nearest.worldPosition.y, x[1] + nearest.worldPosition.y};
        } else {
            x = DEFAULT_FOG_HEIGHTS;
        }

        return new float[]{x[0], x[1]};
    }

    public static BeatmapController getByUuid(UUID uuid) {

        for (var map : beatmaps) {
            if (map.mapId.equals(uuid)) {
                return map;
            }
        }

        return null;
    }

    public static BeatmapController place(Level level, Vector3f pos, float angle, BeatmapRenderer.RenderStyle style) {
        var map = new BeatmapController(level, pos, angle, style);
        beatmaps.add(map);
        return map;
    }

    public static BeatmapController nearestActiveBeatmapToPlayer() {
        var pos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().toVector3f();
        return nearestActiveBeatmap(pos);
    }

    public static BeatmapController nearestActiveBeatmap(Vector3f pos) {

        var nearestDist = Float.POSITIVE_INFINITY;
        BeatmapController nearest = null;

        for (var map : beatmaps) {
            if (map.difficulty == null || map.difficulty.lightShowEnvironment == null) {
                continue;
            }
            var dist = map.getRenderOrigin().distance(pos);
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = map;
            }
        }

        return nearest;

    }

    public static boolean hasNearbyBeatmapToPlayer() {
        var playerCamera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().toVector3f();
        var renderDist = Minecraft.getInstance().gameRenderer.getRenderDistance();
        for (var map : beatmaps) {
            var pos = map.getRenderOrigin();

            if (playerCamera.distance(pos) <= renderDist + 64) {
                return true;
            }

        }
        return false;
    }

    public static boolean hasNearbyActiveBeatmapToPlayer() {
        var playerCamera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().toVector3f();
        return hasNearbyActiveBeatmap(playerCamera);
    }
    public static boolean hasNearbyActiveBeatmap(Vector3f position) {
        var renderDist = Minecraft.getInstance().gameRenderer.getRenderDistance();
        for (var map : beatmaps) {
            var pos = map.getRenderOrigin();

            if (position.distance(pos) <= renderDist + 64 && map.difficulty != null && map.difficulty.lightShowEnvironment != null) {
                return true;
            }

        }
        return false;
    }

    public static String getMapsInfo() {

        if (beatmaps.isEmpty()) {
            return "No maps to display";
        } else {
            var info = new StringBuilder();

            info.append(String.format("Info for %s map%s:\n", beatmaps.size(), beatmaps.size() == 1 ? "" : "s"));

            for (var map : beatmaps) {
                info.append(map.getDisplayInfo()).append("\n");
            }

            return info.toString();
        }

    }

    public static void updateMaps() {
        for (var map : beatmaps) {
            map.update();
        }
        updateFadeFactor();
    }

    public static void preRenderMaps() {
        var cam = Minecraft.getInstance().gameRenderer.getMainCamera();
        for (var map : beatmaps) {
            map.pre_render(cam);
        }
    }

    public static void renderMaps() {
        var cam = Minecraft.getInstance().gameRenderer.getMainCamera();
        for (var map : beatmaps) {
            map.render(cam);
        }
    }

    public static void renderHUDs(MultiBufferSource imm) {
        for (var map : beatmaps) {
            map.hudRenderer.render(imm);
        }
    }

    public static boolean isTracked(UUID playerUuid) {
        if (playerUuid == null) return false;
        for (var map : beatmaps) {
            if (playerUuid.equals(map.trackedPlayer)) {
                return true;
            }
        }
        return false;
    }

    public static @Nullable BeatmapController getNearestFiltered(Vector3f pos, Function<BeatmapController, Boolean> filter) {
        BeatmapController nearest = null;
        var nearestDist = Float.POSITIVE_INFINITY;

        for (var map : beatmaps) {
            var dist = map.getRenderOrigin().distance(pos);
            if (dist < nearestDist && filter.apply(map)) {
                nearestDist = dist;
                nearest = map;
            }
        }
        return nearest;
    }

}
