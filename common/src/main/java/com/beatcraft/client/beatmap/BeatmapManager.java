package com.beatcraft.client.beatmap;

import com.beatcraft.Beatcraft;
import com.beatcraft.common.data.map.SongData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class BeatmapManager {

    public static final ArrayList<BeatmapController> beatmaps = new ArrayList<>();
    public static final ArrayList<SongData> songs = new ArrayList<>();

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
        } else {
            x = DEFAULT_FOG_HEIGHTS;
        }

        return new float[]{x[0] + position.y, x[1] + position.y};
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

}
