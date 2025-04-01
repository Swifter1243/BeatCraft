package com.beatcraft.replay;

import com.beatcraft.BeatCraftClient;
import com.beatcraft.data.components.ModComponents;
import com.beatcraft.data.menu.SongData;
import com.beatcraft.items.ModItems;
import com.beatcraft.logic.GameLogicHandler;
import com.beatcraft.networking.c2s.BeatSyncC2SPayload;
import com.beatcraft.networking.c2s.SaberSyncC2SPayload;
import com.beatcraft.render.HUDRenderer;
import com.beatcraft.render.effect.SaberRenderer;
import com.beatcraft.utils.MathUtil;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.ArrayListDeque;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class Replayer {

    private static final ArrayList<PlayFrame> frames = new ArrayList<>();
    private static final ArrayListDeque<PlayFrame> upcoming = new ArrayListDeque<>();
    private static PlayFrame current = new PlayFrame(0, new Vector3f(), new Quaternionf(), new Vector3f(), new Quaternionf(), new Vector3f(), new Quaternionf());
    public static boolean runReplay = false;
    private static final ItemStack leftSaber = new ItemStack(ModItems.SABER_ITEM, 1);
    private static final ItemStack rightSaber = new ItemStack(ModItems.SABER_ITEM, 1);

    public static void loadReplay(String replayFile) throws IOException {
        frames.clear();
        String path = MinecraftClient.getInstance().runDirectory + "/beatcraft/replay/" + replayFile;
        leftSaber.set(ModComponents.AUTO_SYNC_COLOR, 0);
        leftSaber.set(ModComponents.SABER_COLOR_COMPONENT, 0xc03030);

        rightSaber.set(ModComponents.AUTO_SYNC_COLOR, 1);
        rightSaber.set(ModComponents.SABER_COLOR_COMPONENT, 0x2064a8);

        byte[] rawData = Files.readAllBytes(Path.of(path));

        ByteBuffer buf = ByteBuffer.wrap(rawData);

        int replayVersion = buf.getInt();

        int idSize = buf.getInt();
        byte[] idBytes = new byte[idSize];
        buf.get(idBytes, 0, idSize);
        var id = new String(idBytes, StandardCharsets.UTF_8);

        int setSize = buf.getInt();
        byte[] setBytes = new byte[setSize];
        buf.get(setBytes, 0, setSize);
        var set = new String(setBytes, StandardCharsets.UTF_8);

        int diffSize = buf.getInt();
        byte[] diffBytes = new byte[diffSize];
        buf.get(diffBytes, 0, diffSize);
        var diff = new String(diffBytes, StandardCharsets.UTF_8);

        int frameCount = buf.getInt();

        frames.add(new PlayFrame(0, new Vector3f(), new Quaternionf(), new Vector3f(), new Quaternionf(), new Vector3f(), new Quaternionf()));

        for (int i = 0; i < frameCount; i++) {
            frames.add(PlayFrame.load(buf));
        }

        upcoming.addAll(frames);

        current = upcoming.pollFirst();

        SongData data = BeatCraftClient.songs.getById(id);

        runReplay = true;
        SongData.BeatmapInfo info = data.getBeatMapInfo(set, diff);

        HUDRenderer.songSelectMenuPanel.tryPlayMap(data, info, set, diff);

    }

    public static void reset() {
        frames.clear();
        upcoming.clear();
        runReplay = false;
    }

    public static void update(float beat) {
        if (!runReplay || beat < 0) return;
        if (frames.isEmpty() || upcoming.isEmpty()) return;

        PlayFrame next;
        while (true) {
            next = upcoming.peekFirst();
            if (next == null) return;
            if (next.beat() < beat) {
                current = upcoming.pollFirst();
                if (current == null) return;
            } else {
                break;
            }
        }


        float sb = current.beat();
        float eb = next.beat();

        float f = MathUtil.inverseLerp(sb, eb, beat);

        Vector3f leftSaberPos = MathUtil.lerpVector3(current.leftSaberPosition(), next.leftSaberPosition(), f);
        Vector3f rightSaberPos = MathUtil.lerpVector3(current.rightSaberPosition(), next.rightSaberPosition(), f);
        Vector3f headPos = MathUtil.lerpVector3(current.headPos(), next.headPos(), f);

        Quaternionf leftSaberRot = MathUtil.lerpQuaternion(current.leftSaberRotation(), next.leftSaberRotation(), f);
        Quaternionf rightSaberRot = MathUtil.lerpQuaternion(current.rightSaberRotation(), next.rightSaberRotation(), f);
        Quaternionf headRot = MathUtil.lerpQuaternion(current.headRotation(), next.headRotation(), f);

        GameLogicHandler.updateRightSaber(rightSaberPos, rightSaberRot);
        GameLogicHandler.updateLeftSaber(leftSaberPos, leftSaberRot);
        GameLogicHandler.headPos = headPos;
        GameLogicHandler.headRot = headRot;

        SaberRenderer.renderReplaySaber(leftSaber, leftSaberPos, leftSaberRot);
        SaberRenderer.renderReplaySaber(rightSaber, rightSaberPos, rightSaberRot);

        ClientPlayNetworking.send(new SaberSyncC2SPayload(leftSaberPos, leftSaberRot, rightSaberPos, rightSaberRot, headPos, headRot));
        ClientPlayNetworking.send(new BeatSyncC2SPayload(beat));

    }

    public static void seek(float beat) {
        upcoming.clear();
        upcoming.addAll(frames);
    }


}
